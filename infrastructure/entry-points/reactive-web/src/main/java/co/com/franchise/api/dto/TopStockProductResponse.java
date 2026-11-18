package co.com.franchise.api.dto;

import co.com.franchise.model.product.BranchTopProduct;

/**
 * Flattens a branch and its highest-stock product so the client gets one row per branch.
 */
public record TopStockProductResponse(Long branchId, String branchName, Long productId, String productName,
                                      Integer stock) {

    public static TopStockProductResponse from(BranchTopProduct topProduct) {
        return new TopStockProductResponse(
                topProduct.getBranch().getId(),
                topProduct.getBranch().getName(),
                topProduct.getProduct().getId(),
                topProduct.getProduct().getName(),
                topProduct.getProduct().getStock());
    }
}
