package co.com.franchise.model.exception;

import co.com.franchise.model.rule.NameRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catalog of business rule violations. Each entry carries its category and an English message that
 * is safe to return to API clients.
 */
@Getter
@RequiredArgsConstructor
public enum BusinessError {
    INVALID_NAME(ErrorType.INVALID,
            "Name must not be blank and must have at most " + NameRule.MAX_LENGTH + " characters"),
    INVALID_STOCK(ErrorType.INVALID, "Stock must be zero or greater"),
    FRANCHISE_NOT_FOUND(ErrorType.NOT_FOUND, "Franchise not found"),
    BRANCH_NOT_FOUND(ErrorType.NOT_FOUND, "Branch not found in the given franchise"),
    PRODUCT_NOT_FOUND(ErrorType.NOT_FOUND, "Product not found in the given branch"),
    FRANCHISE_NAME_TAKEN(ErrorType.CONFLICT, "A franchise with this name already exists"),
    BRANCH_NAME_TAKEN(ErrorType.CONFLICT, "The franchise already has a branch with this name"),
    PRODUCT_NAME_TAKEN(ErrorType.CONFLICT, "The branch already has a product with this name");

    private final ErrorType type;
    private final String message;
}
