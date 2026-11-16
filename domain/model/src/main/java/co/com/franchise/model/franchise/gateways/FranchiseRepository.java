package co.com.franchise.model.franchise.gateways;

import co.com.franchise.model.franchise.Franchise;
import reactor.core.publisher.Mono;

/**
 * Persistence port for franchises. Lookups complete empty instead of failing when nothing matches,
 * so use cases decide which business error applies.
 */
public interface FranchiseRepository {

    /**
     * Inserts the franchise when it has no id, otherwise updates it.
     */
    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(Long id);

    /**
     * Looks up a franchise by name ignoring case; completes empty when none matches.
     */
    Mono<Franchise> findByName(String name);
}
