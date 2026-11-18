package co.com.franchise.api.handler;

import co.com.franchise.api.ApiTestSupport;
import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.Franchise;
import co.com.franchise.model.product.BranchTopProduct;
import co.com.franchise.model.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FranchiseHandlerTest extends ApiTestSupport {

    private static final Franchise FRANCHISE = Franchise.builder().id(1L).name("Burger Co").build();

    @Test
    void shouldCreateFranchiseWhenNameIsValid() {
        when(createFranchiseUseCase.execute("Burger Co")).thenReturn(Mono.just(FRANCHISE));

        webTestClient.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger Co\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/franchises/1$")
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Burger Co");
    }

    @Test
    void shouldReturnConflictWhenFranchiseNameIsTaken() {
        when(createFranchiseUseCase.execute("Burger Co"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NAME_TAKEN)));

        expectProblem(webTestClient.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger Co\"}")
                .exchange(), 409, "FRANCHISE_NAME_TAKEN")
                .jsonPath("$.detail").isEqualTo(BusinessError.FRANCHISE_NAME_TAKEN.getMessage())
                .jsonPath("$.instance").isEqualTo(FRANCHISES);
    }

    @Test
    void shouldRejectCreateWhenBodyIsMissing() {
        expectProblem(webTestClient.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange(), 400, "BAD_REQUEST")
                .jsonPath("$.detail").isEqualTo("Request body is required");

        verifyNoInteractions(createFranchiseUseCase);
    }

    @Test
    void shouldRejectCreateWhenJsonIsMalformed() {
        expectProblem(webTestClient.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":")
                .exchange(), 400, "BAD_REQUEST");

        verifyNoInteractions(createFranchiseUseCase);
    }

    @Test
    void shouldRenameFranchiseWhenItExists() {
        when(renameFranchiseUseCase.execute(1L, "Burger House"))
                .thenReturn(Mono.just(FRANCHISE.toBuilder().name("Burger House").build()));

        webTestClient.patch().uri(FRANCHISES + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger House\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Burger House");
    }

    @Test
    void shouldReturnNotFoundWhenRenamingMissingFranchise() {
        when(renameFranchiseUseCase.execute(9L, "Burger House"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)));

        expectProblem(webTestClient.patch().uri(FRANCHISES + "/9")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger House\"}")
                .exchange(), 404, "FRANCHISE_NOT_FOUND");
    }

    @Test
    void shouldRejectRenameWhenFranchiseIdIsNotNumeric() {
        expectProblem(webTestClient.patch().uri(FRANCHISES + "/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger House\"}")
                .exchange(), 400, "BAD_REQUEST")
                .jsonPath("$.detail").isEqualTo("Path variable 'franchiseId' must be a whole number");

        verifyNoInteractions(renameFranchiseUseCase);
    }

    @Test
    void shouldListTopStockProductPerBranchInUseCaseOrder() {
        when(getTopStockProductsUseCase.execute(1L)).thenReturn(Flux.just(
                topProduct(10L, "North", 100L, "Fries", 40),
                topProduct(11L, "South", 101L, "Soda", 25)));

        webTestClient.get().uri(FRANCHISES + "/1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].branchId").isEqualTo(10)
                .jsonPath("$[0].branchName").isEqualTo("North")
                .jsonPath("$[0].productId").isEqualTo(100)
                .jsonPath("$[0].productName").isEqualTo("Fries")
                .jsonPath("$[0].stock").isEqualTo(40)
                .jsonPath("$[1].branchName").isEqualTo("South");

        verify(getTopStockProductsUseCase).execute(1L);
    }

    @Test
    void shouldReturnEmptyListWhenFranchiseHasNoProducts() {
        when(getTopStockProductsUseCase.execute(1L)).thenReturn(Flux.empty());

        webTestClient.get().uri(FRANCHISES + "/1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
    }

    @Test
    void shouldReturnNotFoundWhenListingTopStockOfMissingFranchise() {
        when(getTopStockProductsUseCase.execute(9L))
                .thenReturn(Flux.error(new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)));

        expectProblem(webTestClient.get().uri(FRANCHISES + "/9/top-stock-products")
                .exchange(), 404, "FRANCHISE_NOT_FOUND");
    }

    private static BranchTopProduct topProduct(Long branchId, String branchName, Long productId,
                                               String productName, Integer stock) {
        return BranchTopProduct.builder()
                .branch(Branch.builder().id(branchId).franchiseId(1L).name(branchName).build())
                .product(Product.builder().id(productId).branchId(branchId).name(productName).stock(stock).build())
                .build();
    }
}
