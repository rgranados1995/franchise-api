package co.com.franchise.api.handler;

import co.com.franchise.api.dto.BranchResponse;
import co.com.franchise.api.dto.NameRequest;
import co.com.franchise.usecase.addbranch.AddBranchUseCase;
import co.com.franchise.usecase.renamebranch.RenameBranchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static co.com.franchise.api.request.RequestReader.BRANCH_ID;
import static co.com.franchise.api.request.RequestReader.FRANCHISE_ID;
import static co.com.franchise.api.request.RequestReader.body;
import static co.com.franchise.api.request.RequestReader.pathId;
import static reactor.function.TupleUtils.function;

@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final AddBranchUseCase addBranchUseCase;
    private final RenameBranchUseCase renameBranchUseCase;

    public Mono<ServerResponse> add(ServerRequest request) {
        return Mono.zip(pathId(request, FRANCHISE_ID), body(request, NameRequest.class))
                .flatMap(function((franchiseId, nameRequest) ->
                        addBranchUseCase.execute(franchiseId, nameRequest.name())))
                .flatMap(branch -> ServerResponse
                        .created(request.uriBuilder().path("/{id}").build(branch.getId()))
                        .bodyValue(BranchResponse.from(branch)));
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        return Mono.zip(pathId(request, FRANCHISE_ID), pathId(request, BRANCH_ID), body(request, NameRequest.class))
                .flatMap(function((franchiseId, branchId, nameRequest) ->
                        renameBranchUseCase.execute(franchiseId, branchId, nameRequest.name())))
                .map(BranchResponse::from)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
