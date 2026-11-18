package co.com.franchise.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsWebFilter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";

    private final CorsWebFilter filter = new CorsConfig().corsWebFilter(List.of(ALLOWED_ORIGIN));

    @Test
    void shouldAcceptPreflightFromAllowedOriginForEveryApiMethod() {
        MockServerWebExchange exchange = preflight(ALLOWED_ORIGIN, "DELETE");

        StepVerifier.create(filter.filter(exchange, filteredExchange -> Mono.empty())).verifyComplete();

        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
        assertThat(headers.getAccessControlAllowOrigin()).isEqualTo(ALLOWED_ORIGIN);
        assertThat(headers.getAccessControlAllowMethods())
                .extracting(Object::toString)
                .containsExactly("GET", "POST", "PATCH", "DELETE");
        assertThat(headers.getAccessControlAllowHeaders()).containsExactly("Content-Type");
        assertThat(headers.getAccessControlAllowCredentials()).isTrue();
    }

    @Test
    void shouldRejectPreflightFromOtherOrigin() {
        MockServerWebExchange exchange = preflight("https://evil.example", "PATCH");

        StepVerifier.create(filter.filter(exchange, filteredExchange -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static MockServerWebExchange preflight(String origin, String method) {
        return MockServerWebExchange.from(MockServerHttpRequest
                .options("http://localhost:8080/api/v1/franchises/1")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"));
    }
}
