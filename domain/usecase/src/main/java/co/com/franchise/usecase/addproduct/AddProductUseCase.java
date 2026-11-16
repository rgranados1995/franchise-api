package co.com.franchise.usecase.addproduct;

import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.product.Product;
import co.com.franchise.model.product.gateways.ProductRepository;
import co.com.franchise.model.rule.NameRule;
import co.com.franchise.model.rule.StockRule;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Adds a product to a branch of a franchise. Fails with {@link BusinessError#INVALID_NAME},
 * {@link BusinessError#INVALID_STOCK}, {@link BusinessError#BRANCH_NOT_FOUND} or
 * {@link BusinessError#PRODUCT_NAME_TAKEN}.
 */
@RequiredArgsConstructor
public class AddProductUseCase {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Mono<Product> execute(Long franchiseId, Long branchId, String name, Integer stock) {
        return NameRule.validate(name)
                .zipWith(StockRule.validate(stock))
                .flatMap(validInput -> branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                        .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.BRANCH_NOT_FOUND)))
                        .flatMap(branch -> ensureNameAvailable(branchId, validInput.getT1()))
                        .thenReturn(Product.builder()
                                .branchId(branchId)
                                .name(validInput.getT1())
                                .stock(validInput.getT2())
                                .build()))
                .flatMap(productRepository::save);
    }

    private Mono<Void> ensureNameAvailable(Long branchId, String name) {
        return productRepository.findByBranchIdAndName(branchId, name)
                .flatMap(existing -> Mono.error(() -> new BusinessException(BusinessError.PRODUCT_NAME_TAKEN)));
    }
}
