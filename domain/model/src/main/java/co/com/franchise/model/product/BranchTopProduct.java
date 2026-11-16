package co.com.franchise.model.product;

import co.com.franchise.model.branch.Branch;
import lombok.Builder;
import lombok.Value;

/**
 * Read model that pairs a branch with the product it holds the most stock of.
 */
@Value
@Builder
public class BranchTopProduct {
    Branch branch;
    Product product;
}
