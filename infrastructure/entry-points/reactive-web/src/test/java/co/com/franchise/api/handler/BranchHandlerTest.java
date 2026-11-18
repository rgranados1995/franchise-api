package co.com.franchise.api.handler;

import co.com.franchise.api.ApiTestSupport;
import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

class BranchHandlerTest extends ApiTestSupport {

    private static final Branch BRANCH = Branch.builder().id(10L).franchiseId(1L).name("North").build();

    @Test
    void shouldAddBranchWhenFranchiseExists() {
        when(addBranchUseCase.execute(1L, "North")).thenReturn(Mono.just(BRANCH));

        webTestClient.post().uri(FRANCHISES + "/1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"North\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/franchises/1/branches/10$")
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.franchiseId").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("North");
    }

    @Test
    void shouldReturnNotFoundWhenAddingBranchToMissingFranchise() {
        when(addBranchUseCase.execute(9L, "North"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)));

        expectProblem(webTestClient.post().uri(FRANCHISES + "/9/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"North\"}")
                .exchange(), 404, "FRANCHISE_NOT_FOUND");
    }

    @Test
    void shouldRenameBranchWhenItBelongsToFranchise() {
        when(renameBranchUseCase.execute(1L, 10L, "North Mall"))
                .thenReturn(Mono.just(BRANCH.toBuilder().name("North Mall").build()));

        webTestClient.patch().uri(FRANCHISES + "/1/branches/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"North Mall\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.name").isEqualTo("North Mall");
    }

    @Test
    void shouldReturnBadRequestWhenBranchNameIsInvalid() {
        when(renameBranchUseCase.execute(1L, 10L, null))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_NAME)));

        expectProblem(webTestClient.patch().uri(FRANCHISES + "/1/branches/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange(), 400, "INVALID_NAME");
    }
}
