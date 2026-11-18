package co.com.franchise.api;

import co.com.franchise.api.handler.BranchHandler;
import co.com.franchise.api.handler.FranchiseHandler;
import co.com.franchise.api.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterRest {

    public static final String FRANCHISES_PATH = "/api/v1/franchises";

    private static final String FRANCHISE = "/{franchiseId}";
    private static final String BRANCHES = FRANCHISE + "/branches";
    private static final String BRANCH = BRANCHES + "/{branchId}";
    private static final String PRODUCTS = BRANCH + "/products";
    private static final String PRODUCT = PRODUCTS + "/{productId}";

    @Bean
    public RouterFunction<ServerResponse> routerFunction(FranchiseHandler franchiseHandler,
                                                         BranchHandler branchHandler,
                                                         ProductHandler productHandler) {
        return RouterFunctions.route()
                .path(FRANCHISES_PATH, builder -> builder
                        .POST("", franchiseHandler::create)
                        .PATCH(FRANCHISE, franchiseHandler::rename)
                        .GET(FRANCHISE + "/top-stock-products", franchiseHandler::getTopStockProducts)
                        .POST(BRANCHES, branchHandler::add)
                        .PATCH(BRANCH, branchHandler::rename)
                        .POST(PRODUCTS, productHandler::add)
                        .DELETE(PRODUCT, productHandler::remove)
                        .PATCH(PRODUCT + "/stock", productHandler::updateStock)
                        .PATCH(PRODUCT, productHandler::rename))
                .build();
    }
}
