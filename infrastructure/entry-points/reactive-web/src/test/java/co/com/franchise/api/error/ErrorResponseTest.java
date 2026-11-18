package co.com.franchise.api.error;

import co.com.franchise.api.ApiTestSupport;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Checks the HTTP contract of errors and filters through the real web stack.
 */
class ErrorResponseTest extends ApiTestSupport {

    @ParameterizedTest
    @CsvSource({
            "INVALID_NAME, 400",
            "INVALID_STOCK, 400",
            "FRANCHISE_NOT_FOUND, 404",
            "BRANCH_NOT_FOUND, 404",
            "PRODUCT_NOT_FOUND, 404",
            "FRANCHISE_NAME_TAKEN, 409",
            "BRANCH_NAME_TAKEN, 409",
            "PRODUCT_NAME_TAKEN, 409"
    })
    void shouldMapEveryBusinessErrorToItsStatus(BusinessError error, int status) {
        when(createFranchiseUseCase.execute("Burger Co")).thenReturn(Mono.error(new BusinessException(error)));

        expectProblem(webTestClient.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger Co\"}")
                .exchange(), status, error.name())
                .jsonPath("$.detail").isEqualTo(error.getMessage());
    }

    @Test
    void shouldHideInternalDetailsWhenErrorIsUnexpected() {
        when(createFranchiseUseCase.execute("Burger Co"))
                .thenReturn(Mono.error(new IllegalStateException("Connection refused: db.internal:5432")));

        byte[] body = expectProblem(webTestClient.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger Co\"}")
                .exchange(), 500, "INTERNAL_SERVER_ERROR")
                .jsonPath("$.detail").isEqualTo(GlobalErrorHandler.UNEXPECTED_ERROR_DETAIL)
                .returnResult().getResponseBodyContent();

        assertThat(new String(body, StandardCharsets.UTF_8)).doesNotContain("db.internal");
    }

    @Test
    void shouldReturnProblemWhenRouteDoesNotExist() {
        expectProblem(webTestClient.get().uri("/api/v1/unknown").exchange(), 404, "NOT_FOUND");
    }

    @Test
    void shouldReturnProblemWhenContentTypeIsNotSupported() {
        expectProblem(webTestClient.post().uri(FRANCHISES)
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Burger Co")
                .exchange(), 415, "UNSUPPORTED_MEDIA_TYPE");
    }

    @Test
    void shouldAddSecurityHeadersToEveryResponse() {
        webTestClient.get().uri("/api/v1/unknown")
                .exchange()
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().exists("Strict-Transport-Security");
    }

    @Test
    void shouldAllowPatchFromConfiguredOriginOnPreflight() {
        webTestClient.options().uri(API_HOST + FRANCHISES + "/1")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
                .expectHeader().value(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        methods -> assertThat(methods).contains("PATCH").contains("DELETE"));
    }

    @Test
    void shouldRejectPreflightFromUnknownOrigin() {
        webTestClient.options().uri(API_HOST + FRANCHISES + "/1")
                .header(HttpHeaders.ORIGIN, "https://evil.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
                .exchange()
                .expectStatus().isForbidden();
    }
}
