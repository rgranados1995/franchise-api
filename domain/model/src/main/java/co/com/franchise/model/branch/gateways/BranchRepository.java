package co.com.franchise.model.branch.gateways;

import co.com.franchise.model.branch.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Persistence port for branches. Lookups complete empty instead of failing when nothing matches,
 * so use cases decide which business error applies.
 */
public interface BranchRepository {

    /**
     * Inserts the branch when it has no id, otherwise updates it.
     */
    Mono<Branch> save(Branch branch);

    /**
     * Completes empty when the branch does not exist or belongs to another franchise.
     */
    Mono<Branch> findByIdAndFranchiseId(Long id, Long franchiseId);

    /**
     * Looks up a branch of the franchise by name ignoring case; completes empty when none matches.
     */
    Mono<Branch> findByFranchiseIdAndName(Long franchiseId, String name);

    Flux<Branch> findByFranchiseId(Long franchiseId);
}
