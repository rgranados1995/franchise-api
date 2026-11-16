package co.com.franchise.usecase.removeproduct;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Removes a product from a branch of a franchise. Fails with
 * {@link BusinessError#PRODUCT_NOT_FOUND} when the product is not part of that branch and franchise.
 */
@RequiredArgsConstructor
public class RemoveProductUseCase {

    private final ProductRepository productRepository;

    public Mono<Void> execute(Long franchiseId, Long branchId, Long productId) {
        return productRepository.findByIdAndBranchIdAndFranchiseId(productId, branchId, franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND)))
                .flatMap(product -> productRepository.deleteById(product.getId()));
    }
}
