package co.com.franchise.usecase.gettopstockproducts;

import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.franchise.model.product.BranchTopProduct;
import co.com.franchise.model.product.Product;
import co.com.franchise.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.function.BinaryOperator;

/**
 * Finds, for every branch of a franchise, the product with the highest stock. On a tie the product
 * with the lowest id wins, so the answer is deterministic. Branches without products are left out,
 * and results keep the order in which the branches are read. Fails with
 * {@link BusinessError#FRANCHISE_NOT_FOUND}.
 */
@RequiredArgsConstructor
public class GetTopStockProductsUseCase {

    private static final Comparator<Product> STOCK_RANKING = Comparator
            .comparing(Product::getStock)
            .thenComparing(Product::getId, Comparator.reverseOrder());

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Flux<BranchTopProduct> execute(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)))
                .flatMapMany(franchise -> branchRepository.findByFranchiseId(franchise.getId()))
                .flatMapSequential(branch -> productRepository.findByBranchId(branch.getId())
                        .reduce(BinaryOperator.maxBy(STOCK_RANKING))
                        .map(topProduct -> BranchTopProduct.builder().branch(branch).product(topProduct).build()));
    }
}
