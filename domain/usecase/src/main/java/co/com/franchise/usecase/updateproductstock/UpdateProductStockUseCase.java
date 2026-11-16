package co.com.franchise.usecase.updateproductstock;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.product.Product;
import co.com.franchise.model.product.gateways.ProductRepository;
import co.com.franchise.model.rule.StockRule;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Replaces the stock of a product. Fails with {@link BusinessError#INVALID_STOCK} or
 * {@link BusinessError#PRODUCT_NOT_FOUND}.
 */
@RequiredArgsConstructor
public class UpdateProductStockUseCase {

    private final ProductRepository productRepository;

    public Mono<Product> execute(Long franchiseId, Long branchId, Long productId, Integer newStock) {
        return StockRule.validate(newStock)
                .flatMap(validStock -> productRepository.findByIdAndBranchIdAndFranchiseId(productId, branchId, franchiseId)
                        .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND)))
                        .map(product -> product.toBuilder().stock(validStock).build()))
                .flatMap(productRepository::save);
    }
}
