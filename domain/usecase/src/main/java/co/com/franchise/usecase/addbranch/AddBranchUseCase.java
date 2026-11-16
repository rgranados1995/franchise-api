package co.com.franchise.usecase.addbranch;

import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.franchise.model.rule.NameRule;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Adds a branch to a franchise. Fails with {@link BusinessError#INVALID_NAME},
 * {@link BusinessError#FRANCHISE_NOT_FOUND} or {@link BusinessError#BRANCH_NAME_TAKEN}.
 */
@RequiredArgsConstructor
public class AddBranchUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    public Mono<Branch> execute(Long franchiseId, String name) {
        return NameRule.validate(name)
                .flatMap(validName -> franchiseRepository.findById(franchiseId)
                        .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)))
                        .flatMap(franchise -> ensureNameAvailable(franchiseId, validName))
                        .thenReturn(Branch.builder().franchiseId(franchiseId).name(validName).build()))
                .flatMap(branchRepository::save);
    }

    private Mono<Void> ensureNameAvailable(Long franchiseId, String name) {
        return branchRepository.findByFranchiseIdAndName(franchiseId, name)
                .flatMap(existing -> Mono.error(() -> new BusinessException(BusinessError.BRANCH_NAME_TAKEN)));
    }
}
