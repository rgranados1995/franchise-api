package co.com.franchise.usecase.createfranchise;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.Franchise;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.franchise.model.rule.NameRule;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Creates a franchise. Fails with {@link BusinessError#INVALID_NAME} or
 * {@link BusinessError#FRANCHISE_NAME_TAKEN}.
 */
@RequiredArgsConstructor
public class CreateFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> execute(String name) {
        return NameRule.validate(name)
                .flatMap(validName -> ensureNameAvailable(validName)
                        .thenReturn(Franchise.builder().name(validName).build()))
                .flatMap(franchiseRepository::save);
    }

    private Mono<Void> ensureNameAvailable(String name) {
        return franchiseRepository.findByName(name)
                .flatMap(existing -> Mono.error(() -> new BusinessException(BusinessError.FRANCHISE_NAME_TAKEN)));
    }
}
