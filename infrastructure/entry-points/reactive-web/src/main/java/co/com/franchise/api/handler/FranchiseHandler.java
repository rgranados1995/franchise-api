package co.com.franchise.api.handler;

import co.com.franchise.api.dto.FranchiseResponse;
import co.com.franchise.api.dto.NameRequest;
import co.com.franchise.api.dto.TopStockProductResponse;
import co.com.franchise.usecase.createfranchise.CreateFranchiseUseCase;
import co.com.franchise.usecase.gettopstockproducts.GetTopStockProductsUseCase;
import co.com.franchise.usecase.renamefranchise.RenameFranchiseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static co.com.franchise.api.request.RequestReader.FRANCHISE_ID;
import static co.com.franchise.api.request.RequestReader.body;
import static co.com.franchise.api.request.RequestReader.pathId;
import static reactor.function.TupleUtils.function;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final RenameFranchiseUseCase renameFranchiseUseCase;
    private final GetTopStockProductsUseCase getTopStockProductsUseCase;

    public Mono<ServerResponse> create(ServerRequest request) {
        return body(request, NameRequest.class)
                .flatMap(nameRequest -> createFranchiseUseCase.execute(nameRequest.name()))
                .flatMap(franchise -> ServerResponse
                        .created(request.uriBuilder().path("/{id}").build(franchise.getId()))
                        .bodyValue(FranchiseResponse.from(franchise)));
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        return Mono.zip(pathId(request, FRANCHISE_ID), body(request, NameRequest.class))
                .flatMap(function((franchiseId, nameRequest) ->
                        renameFranchiseUseCase.execute(franchiseId, nameRequest.name())))
                .map(FranchiseResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    /**
     * Collects the rows before answering because the status depends on whether the franchise
     * exists, and it must be decided before the first byte of the body is written. The list is
     * bounded by the number of branches of one franchise.
     */
    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        return pathId(request, FRANCHISE_ID)
                .flatMapMany(getTopStockProductsUseCase::execute)
                .map(TopStockProductResponse::from)
                .collectList()
                .flatMap(rows -> ServerResponse.ok().bodyValue(rows));
    }
}
