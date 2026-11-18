package co.com.franchise.api.dto;

import co.com.franchise.model.branch.Branch;

public record BranchResponse(Long id, Long franchiseId, String name) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getFranchiseId(), branch.getName());
    }
}
