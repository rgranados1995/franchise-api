package co.com.franchise.api.handler;

import co.com.franchise.api.dto.CreateProductRequest;
import co.com.franchise.api.dto.NameRequest;
import co.com.franchise.api.dto.ProductResponse;
import co.com.franchise.api.dto.StockRequest;
import co.com.franchise.usecase.addproduct.AddProductUseCase;
import co.com.franchise.usecase.removeproduct.RemoveProductUseCase;
import co.com.franchise.usecase.renameproduct.RenameProductUseCase;
import co.com.franchise.usecase.updateproductstock.UpdateProductStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static co.com.franchise.api.request.RequestReader.BRANCH_ID;
import static co.com.franchise.api.request.RequestReader.FRANCHISE_ID;
import static co.com.franchise.api.request.RequestReader.PRODUCT_ID;
import static co.com.franchise.api.request.RequestReader.body;
import static co.com.franchise.api.request.RequestReader.pathId;
import static reactor.function.TupleUtils.function;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final AddProductUseCase addProductUseCase;
    private final RemoveProductUseCase removeProductUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final RenameProductUseCase renameProductUseCase;

    public Mono<ServerResponse> add(ServerRequest request) {
        return Mono.zip(pathId(request, FRANCHISE_ID), pathId(request, BRANCH_ID),
                        body(request, CreateProductRequest.class))
                .flatMap(function((franchiseId, branchId, productRequest) -> addProductUseCase
                        .execute(franchiseId, branchId, productRequest.name(), productRequest.stock())))
                .flatMap(product -> ServerResponse
                        .created(request.uriBuilder().path("/{id}").build(product.getId()))
                        .bodyValue(ProductResponse.from(product)));
    }

    public Mono<ServerResponse> remove(ServerRequest request) {
        return Mono.zip(pathId(request, FRANCHISE_ID), pathId(request, BRANCH_ID), pathId(request, PRODUCT_ID))
                .flatMap(function(removeProductUseCase::execute))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        return Mono.zip(pathId(request, FRANCHISE_ID), pathId(request, BRANCH_ID), pathId(request, PRODUCT_ID),
                        body(request, StockRequest.class))
                .flatMap(function((franchiseId, branchId, productId, stockRequest) -> updateProductStockUseCase
                        .execute(franchiseId, branchId, productId, stockRequest.stock())))
                .map(ProductResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        return Mono.zip(pathId(request, FRANCHISE_ID), pathId(request, BRANCH_ID), pathId(request, PRODUCT_ID),
                        body(request, NameRequest.class))
                .flatMap(function((franchiseId, branchId, productId, nameRequest) -> renameProductUseCase
                        .execute(franchiseId, branchId, productId, nameRequest.name())))
                .map(ProductResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
