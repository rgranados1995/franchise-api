package co.com.franchise.usecase.renameproduct;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.product.Product;
import co.com.franchise.model.product.gateways.ProductRepository;
import co.com.franchise.model.rule.NameRule;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Renames a product of a branch. Keeping the same name (or changing only its case) is allowed.
 * Fails with {@link BusinessError#INVALID_NAME}, {@link BusinessError#PRODUCT_NOT_FOUND} or
 * {@link BusinessError#PRODUCT_NAME_TAKEN}.
 */
@RequiredArgsConstructor
public class RenameProductUseCase {

    private final ProductRepository productRepository;

    public Mono<Product> execute(Long franchiseId, Long branchId, Long productId, String newName) {
        return NameRule.validate(newName)
                .flatMap(validName -> productRepository.findByIdAndBranchIdAndFranchiseId(productId, branchId, franchiseId)
                        .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND)))
                        .flatMap(product -> ensureNameAvailable(branchId, productId, validName)
                                .thenReturn(product.toBuilder().name(validName).build())))
                .flatMap(productRepository::save);
    }

    private Mono<Void> ensureNameAvailable(Long branchId, Long productId, String name) {
        return productRepository.findByBranchIdAndName(branchId, name)
                .filter(existing -> !existing.getId().equals(productId))
                .flatMap(existing -> Mono.error(() -> new BusinessException(BusinessError.PRODUCT_NAME_TAKEN)));
    }
}
