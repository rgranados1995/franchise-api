package co.com.franchise.api.request;

import co.com.franchise.api.dto.NameRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RequestReaderTest {

    @Test
    void shouldParsePathIdWhenItIsAWholeNumber() {
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable(RequestReader.FRANCHISE_ID, "42")
                .build();

        StepVerifier.create(RequestReader.pathId(request, RequestReader.FRANCHISE_ID))
                .expectNext(42L)
                .verifyComplete();
    }

    @Test
    void shouldSignalBadInputWhenPathIdIsNotANumber() {
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable(RequestReader.PRODUCT_ID, "12abc")
                .build();

        StepVerifier.create(RequestReader.pathId(request, RequestReader.PRODUCT_ID))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(ServerWebInputException.class)
                        .hasMessageContaining("Path variable 'productId' must be a whole number"))
                .verify();
    }

    @Test
    void shouldSignalBadInputWhenPathIdOverflowsLong() {
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable(RequestReader.BRANCH_ID, "99999999999999999999")
                .build();

        StepVerifier.create(RequestReader.pathId(request, RequestReader.BRANCH_ID))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldEmitBodyWhenPresent() {
        MockServerRequest request = MockServerRequest.builder().body(Mono.just(new NameRequest("North")));

        StepVerifier.create(RequestReader.body(request, NameRequest.class))
                .expectNext(new NameRequest("North"))
                .verifyComplete();
    }

    @Test
    void shouldSignalBadInputWhenBodyIsMissing() {
        MockServerRequest request = MockServerRequest.builder().body(Mono.empty());

        StepVerifier.create(RequestReader.body(request, NameRequest.class))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(ServerWebInputException.class)
                        .hasMessageContaining(RequestReader.BODY_REQUIRED))
                .verify();
    }
}
