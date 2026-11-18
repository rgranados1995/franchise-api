package co.com.franchise.api.handler;

import co.com.franchise.api.ApiTestSupport;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProductHandlerTest extends ApiTestSupport {

    private static final String BRANCH_PRODUCTS = FRANCHISES + "/1/branches/10/products";
    private static final Product PRODUCT = Product.builder().id(100L).branchId(10L).name("Fries").stock(40).build();

    @Test
    void shouldAddProductWhenBranchExists() {
        when(addProductUseCase.execute(1L, 10L, "Fries", 40)).thenReturn(Mono.just(PRODUCT));

        webTestClient.post().uri(BRANCH_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Fries\",\"stock\":40}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/franchises/1/branches/10/products/100$")
                .expectBody()
                .jsonPath("$.id").isEqualTo(100)
                .jsonPath("$.branchId").isEqualTo(10)
                .jsonPath("$.name").isEqualTo("Fries")
                .jsonPath("$.stock").isEqualTo(40);
    }

    @Test
    void shouldReturnBadRequestWhenStockIsNegative() {
        when(addProductUseCase.execute(1L, 10L, "Fries", -1))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_STOCK)));

        expectProblem(webTestClient.post().uri(BRANCH_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Fries\",\"stock\":-1}")
                .exchange(), 400, "INVALID_STOCK");
    }

    @Test
    void shouldRemoveProductAndReturnNoContent() {
        when(removeProductUseCase.execute(1L, 10L, 100L)).thenReturn(Mono.empty());

        webTestClient.delete().uri(BRANCH_PRODUCTS + "/100")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(removeProductUseCase).execute(1L, 10L, 100L);
    }

    @Test
    void shouldReturnNotFoundWhenRemovingMissingProduct() {
        when(removeProductUseCase.execute(1L, 10L, 999L))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NOT_FOUND)));

        expectProblem(webTestClient.delete().uri(BRANCH_PRODUCTS + "/999")
                .exchange(), 404, "PRODUCT_NOT_FOUND");
    }

    @Test
    void shouldUpdateStockWhenProductExists() {
        when(updateProductStockUseCase.execute(1L, 10L, 100L, 55))
                .thenReturn(Mono.just(PRODUCT.toBuilder().stock(55).build()));

        webTestClient.patch().uri(BRANCH_PRODUCTS + "/100/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":55}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(100)
                .jsonPath("$.stock").isEqualTo(55);
    }

    @Test
    void shouldRejectStockUpdateWhenStockIsNotANumber() {
        expectProblem(webTestClient.patch().uri(BRANCH_PRODUCTS + "/100/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":\"ten\"}")
                .exchange(), 400, "BAD_REQUEST");

        verifyNoInteractions(updateProductStockUseCase);
    }

    @Test
    void shouldRenameProductWhenItBelongsToBranch() {
        when(renameProductUseCase.execute(1L, 10L, 100L, "Curly Fries"))
                .thenReturn(Mono.just(PRODUCT.toBuilder().name("Curly Fries").build()));

        webTestClient.patch().uri(BRANCH_PRODUCTS + "/100")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Curly Fries\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Curly Fries")
                .jsonPath("$.stock").isEqualTo(40);
    }

    @Test
    void shouldReturnConflictWhenProductNameIsTaken() {
        when(renameProductUseCase.execute(1L, 10L, 100L, "Soda"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NAME_TAKEN)));

        expectProblem(webTestClient.patch().uri(BRANCH_PRODUCTS + "/100")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Soda\"}")
                .exchange(), 409, "PRODUCT_NAME_TAKEN");
    }
}
