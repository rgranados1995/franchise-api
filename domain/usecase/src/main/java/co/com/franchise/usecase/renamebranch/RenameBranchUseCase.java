package co.com.franchise.usecase.renamebranch;

import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.rule.NameRule;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Renames a branch of a franchise. Keeping the same name (or changing only its case) is allowed.
 * Fails with {@link BusinessError#INVALID_NAME}, {@link BusinessError#BRANCH_NOT_FOUND} or
 * {@link BusinessError#BRANCH_NAME_TAKEN}.
 */
@RequiredArgsConstructor
public class RenameBranchUseCase {

    private final BranchRepository branchRepository;

    public Mono<Branch> execute(Long franchiseId, Long branchId, String newName) {
        return NameRule.validate(newName)
                .flatMap(validName -> branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                        .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.BRANCH_NOT_FOUND)))
                        .flatMap(branch -> ensureNameAvailable(franchiseId, branchId, validName)
                                .thenReturn(branch.toBuilder().name(validName).build())))
                .flatMap(branchRepository::save);
    }

    private Mono<Void> ensureNameAvailable(Long franchiseId, Long branchId, String name) {
        return branchRepository.findByFranchiseIdAndName(franchiseId, name)
                .filter(existing -> !existing.getId().equals(branchId))
                .flatMap(existing -> Mono.error(() -> new BusinessException(BusinessError.BRANCH_NAME_TAKEN)));
    }
}
