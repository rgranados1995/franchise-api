package co.com.franchise.usecase.renamefranchise;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.Franchise;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.franchise.model.rule.NameRule;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Renames a franchise. Keeping the same name (or changing only its case) is allowed. Fails with
 * {@link BusinessError#INVALID_NAME}, {@link BusinessError#FRANCHISE_NOT_FOUND} or
 * {@link BusinessError#FRANCHISE_NAME_TAKEN}.
 */
@RequiredArgsConstructor
public class RenameFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> execute(Long franchiseId, String newName) {
        return NameRule.validate(newName)
                .flatMap(validName -> franchiseRepository.findById(franchiseId)
                        .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)))
                        .flatMap(franchise -> ensureNameAvailable(validName, franchiseId)
                                .thenReturn(franchise.toBuilder().name(validName).build())))
                .flatMap(franchiseRepository::save);
    }

    private Mono<Void> ensureNameAvailable(String name, Long franchiseId) {
        return franchiseRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(franchiseId))
                .flatMap(existing -> Mono.error(() -> new BusinessException(BusinessError.FRANCHISE_NAME_TAKEN)));
    }
}
