package co.com.franchise.api;

import co.com.franchise.api.handler.BranchHandler;
import co.com.franchise.api.handler.FranchiseHandler;
import co.com.franchise.api.handler.ProductHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.mockito.Mockito.mock;

/**
 * Checks the routing table alone: each mocked handler answers with the name of the method that
 * was invoked, so a route wired to the wrong method fails.
 */
class RouterRestTest {

    private static final String ROUTE_HEADER = "X-Handled-By";

    private static final Answer<Object> NAME_OF_INVOKED_METHOD = invocation -> ServerResponse.ok()
            .header(ROUTE_HEADER, invocation.getMethod().getDeclaringClass().getSimpleName()
                    + "." + invocation.getMethod().getName())
            .build();

    private final WebTestClient client = WebTestClient.bindToRouterFunction(new RouterRest().routerFunction(
                    mock(FranchiseHandler.class, NAME_OF_INVOKED_METHOD),
                    mock(BranchHandler.class, NAME_OF_INVOKED_METHOD),
                    mock(ProductHandler.class, NAME_OF_INVOKED_METHOD)))
            .build();

    @ParameterizedTest(name = "{0} {1} -> {2}")
    @CsvSource({
            "POST, /api/v1/franchises, FranchiseHandler.create",
            "PATCH, /api/v1/franchises/1, FranchiseHandler.rename",
            "GET, /api/v1/franchises/1/top-stock-products, FranchiseHandler.getTopStockProducts",
            "POST, /api/v1/franchises/1/branches, BranchHandler.add",
            "PATCH, /api/v1/franchises/1/branches/2, BranchHandler.rename",
            "POST, /api/v1/franchises/1/branches/2/products, ProductHandler.add",
            "DELETE, /api/v1/franchises/1/branches/2/products/3, ProductHandler.remove",
            "PATCH, /api/v1/franchises/1/branches/2/products/3/stock, ProductHandler.updateStock",
            "PATCH, /api/v1/franchises/1/branches/2/products/3, ProductHandler.rename"
    })
    void shouldRouteEachOperationToItsHandlerMethod(String method, String path, String expectedHandler) {
        client.method(HttpMethod.valueOf(method)).uri(path)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(ROUTE_HEADER, expectedHandler);
    }
}
