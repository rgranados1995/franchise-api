package co.com.franchise.api.dto;

import co.com.franchise.model.franchise.Franchise;

public record FranchiseResponse(Long id, String name) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(franchise.getId(), franchise.getName());
    }
}
