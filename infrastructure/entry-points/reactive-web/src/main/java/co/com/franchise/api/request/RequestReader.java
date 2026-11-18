package co.com.franchise.api.request;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * Reads path ids and bodies as reactive signals, so malformed input reaches the error handler as
 * a {@link ServerWebInputException} (400) instead of being thrown from the handler.
 */
public final class RequestReader {

    public static final String FRANCHISE_ID = "franchiseId";
    public static final String BRANCH_ID = "branchId";
    public static final String PRODUCT_ID = "productId";

    static final String BODY_REQUIRED = "Request body is required";

    private RequestReader() {
    }

    public static Mono<Long> pathId(ServerRequest request, String name) {
        return Mono.fromCallable(() -> Long.valueOf(request.pathVariable(name)))
                .onErrorMap(NumberFormatException.class,
                        error -> new ServerWebInputException("Path variable '" + name + "' must be a whole number"));
    }

    public static <T> Mono<T> body(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(() -> new ServerWebInputException(BODY_REQUIRED)));
    }
}
