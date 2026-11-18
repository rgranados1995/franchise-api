package co.com.franchise.api;

import co.com.franchise.api.config.CorsConfig;
import co.com.franchise.api.config.SecurityHeadersConfig;
import co.com.franchise.api.error.GlobalErrorHandler;
import co.com.franchise.api.handler.BranchHandler;
import co.com.franchise.api.handler.FranchiseHandler;
import co.com.franchise.api.handler.ProductHandler;
import co.com.franchise.usecase.addbranch.AddBranchUseCase;
import co.com.franchise.usecase.addproduct.AddProductUseCase;
import co.com.franchise.usecase.createfranchise.CreateFranchiseUseCase;
import co.com.franchise.usecase.gettopstockproducts.GetTopStockProductsUseCase;
import co.com.franchise.usecase.removeproduct.RemoveProductUseCase;
import co.com.franchise.usecase.renamebranch.RenameBranchUseCase;
import co.com.franchise.usecase.renamefranchise.RenameFranchiseUseCase;
import co.com.franchise.usecase.renameproduct.RenameProductUseCase;
import co.com.franchise.usecase.updateproductstock.UpdateProductStockUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Web slice with the real router, handlers, filters and error handler, and the use cases mocked.
 * Every test class shares this context, so Spring builds it once.
 */
@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, FranchiseHandler.class, BranchHandler.class,
        ProductHandler.class, GlobalErrorHandler.class, SecurityHeadersConfig.class, CorsConfig.class})
@TestPropertySource(properties = "cors.allowed-origins=" + ApiTestSupport.ALLOWED_ORIGIN)
public abstract class ApiTestSupport {

    protected static final String ALLOWED_ORIGIN = "http://localhost:4200";
    protected static final String FRANCHISES = RouterRest.FRANCHISES_PATH;
    /**
     * CORS checks compare the Origin header with the request host, so CORS tests need an absolute
     * URL; a relative one has no host and Spring rejects it as malformed.
     */
    protected static final String API_HOST = "http://localhost:8080";

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected CreateFranchiseUseCase createFranchiseUseCase;
    @MockitoBean
    protected RenameFranchiseUseCase renameFranchiseUseCase;
    @MockitoBean
    protected GetTopStockProductsUseCase getTopStockProductsUseCase;
    @MockitoBean
    protected AddBranchUseCase addBranchUseCase;
    @MockitoBean
    protected RenameBranchUseCase renameBranchUseCase;
    @MockitoBean
    protected AddProductUseCase addProductUseCase;
    @MockitoBean
    protected RemoveProductUseCase removeProductUseCase;
    @MockitoBean
    protected UpdateProductStockUseCase updateProductStockUseCase;
    @MockitoBean
    protected RenameProductUseCase renameProductUseCase;

    protected static WebTestClient.BodyContentSpec expectProblem(WebTestClient.ResponseSpec response,
                                                                 int status, String code) {
        return response.expectStatus().isEqualTo(status)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(status)
                .jsonPath("$.code").isEqualTo(code);
    }
}
