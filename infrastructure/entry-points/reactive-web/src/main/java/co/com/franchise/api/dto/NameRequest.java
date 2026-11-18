package co.com.franchise.api.dto;

/**
 * Body for creating or renaming a franchise or a branch, and for renaming a product. The name
 * rules live in the domain, so no constraints are declared here.
 */
public record NameRequest(String name) {
}
