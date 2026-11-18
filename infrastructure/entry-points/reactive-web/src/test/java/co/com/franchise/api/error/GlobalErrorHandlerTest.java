package co.com.franchise.api.error;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler(ServerCodecConfigurer.create());

    @Test
    void shouldWriteProblemDetailForBusinessError() {
        MockServerWebExchange exchange = exchange("/api/v1/franchises/9");

        StepVerifier.create(handler.handle(exchange, new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        StepVerifier.create(exchange.getResponse().getBodyAsString())
                .assertNext(body -> assertThat(body)
                        .contains("\"code\":\"FRANCHISE_NOT_FOUND\"")
                        .contains("\"detail\":\"Franchise not found\"")
                        .contains("\"instance\":\"/api/v1/franchises/9\""))
                .verifyComplete();
    }

    @Test
    void shouldNotLeakMessageOfUnexpectedError() {
        MockServerWebExchange exchange = exchange("/api/v1/franchises");

        StepVerifier.create(handler.handle(exchange, new IllegalStateException("password=placeholder")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        StepVerifier.create(exchange.getResponse().getBodyAsString())
                .assertNext(body -> assertThat(body)
                        .contains(GlobalErrorHandler.UNEXPECTED_ERROR_DETAIL)
                        .doesNotContain("placeholder"))
                .verifyComplete();
    }

    private static MockServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }
}
