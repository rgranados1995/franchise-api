package co.com.franchise.model.product;

import lombok.Builder;
import lombok.Value;

/**
 * Item stocked by one branch. Its name is unique within that branch, ignoring case, and its stock
 * is never negative.
 */
@Value
@Builder(toBuilder = true)
public class Product {
    Long id;
    Long branchId;
    String name;
    Integer stock;
}
