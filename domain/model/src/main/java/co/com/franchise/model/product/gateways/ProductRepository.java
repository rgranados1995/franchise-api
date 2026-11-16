package co.com.franchise.model.product.gateways;

import co.com.franchise.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Persistence port for products. Lookups complete empty instead of failing when nothing matches,
 * so use cases decide which business error applies.
 */
public interface ProductRepository {

    /**
     * Inserts the product when it has no id, otherwise updates it.
     */
    Mono<Product> save(Product product);

    /**
     * Completes empty unless the product belongs to the branch and the branch belongs to the
     * franchise.
     */
    Mono<Product> findByIdAndBranchIdAndFranchiseId(Long id, Long branchId, Long franchiseId);

    /**
     * Looks up a product of the branch by name ignoring case; completes empty when none matches.
     */
    Mono<Product> findByBranchIdAndName(Long branchId, String name);

    Flux<Product> findByBranchId(Long branchId);

    Mono<Void> deleteById(Long id);
}
