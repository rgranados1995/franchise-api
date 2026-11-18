package co.com.franchise.api.error;

import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Turns every error that reaches the web layer into an RFC 9457 problem detail. Business errors
 * keep their code and message, request errors keep the status Spring assigned, and anything else
 * becomes a 500 whose body never exposes the original message. Ordered before the Spring Boot
 * default handler (-1) so it always wins.
 */
@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalErrorHandler implements WebExceptionHandler {

    public static final String CODE_PROPERTY = "code";
    static final String UNEXPECTED_ERROR_DETAIL = "An unexpected error occurred";

    private static final Map<ErrorType, HttpStatus> STATUS_BY_ERROR_TYPE = new EnumMap<>(Map.of(
            ErrorType.INVALID, HttpStatus.BAD_REQUEST,
            ErrorType.NOT_FOUND, HttpStatus.NOT_FOUND,
            ErrorType.CONFLICT, HttpStatus.CONFLICT));

    private final ServerCodecConfigurer codecConfigurer;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
        URI instance = URI.create(exchange.getRequest().getPath().value());
        return Mono.<ProblemDetail>error(error)
                .onErrorResume(BusinessException.class, businessError -> Mono.just(fromBusiness(businessError)))
                .onErrorResume(ResponseStatusException.class, requestError -> Mono.just(fromRequest(requestError)))
                .onErrorResume(unexpectedError -> Mono.fromSupplier(() -> fromUnexpected(exchange, unexpectedError)))
                .map(problem -> withInstance(problem, instance))
                .flatMap(problem -> ServerResponse.status(problem.getStatus())
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .bodyValue(problem))
                .flatMap(response -> response.writeTo(exchange, new ResponseContext(codecConfigurer.getWriters())));
    }

    private static ProblemDetail fromBusiness(BusinessException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                STATUS_BY_ERROR_TYPE.get(exception.getError().getType()), exception.getMessage());
        problem.setProperty(CODE_PROPERTY, exception.getError().name());
        return problem;
    }

    private static ProblemDetail fromRequest(ResponseStatusException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                exception.getStatusCode(), exception.getBody().getDetail());
        problem.setProperty(CODE_PROPERTY, HttpStatus.valueOf(exception.getStatusCode().value()).name());
        return problem;
    }

    private static ProblemDetail fromUnexpected(ServerWebExchange exchange, Throwable exception) {
        log.error("Unexpected error on {} {}", exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(), exception);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_DETAIL);
        problem.setProperty(CODE_PROPERTY, HttpStatus.INTERNAL_SERVER_ERROR.name());
        return problem;
    }

    private static ProblemDetail withInstance(ProblemDetail problem, URI instance) {
        problem.setInstance(instance);
        return problem;
    }

    private record ResponseContext(List<HttpMessageWriter<?>> messageWriters) implements ServerResponse.Context {

        @Override
        public List<ViewResolver> viewResolvers() {
            return List.of();
        }
    }
}
