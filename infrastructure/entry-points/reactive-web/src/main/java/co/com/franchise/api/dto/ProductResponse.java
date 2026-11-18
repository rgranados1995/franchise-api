package co.com.franchise.api.dto;

import co.com.franchise.model.product.Product;

public record ProductResponse(Long id, Long branchId, String name, Integer stock) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }
}
