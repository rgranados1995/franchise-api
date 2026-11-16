package co.com.franchise.model.exception;

/**
 * Business category of an error. Entry points translate each category to their own protocol
 * (for example, an HTTP status) so the domain stays unaware of transport details.
 */
public enum ErrorType {
    INVALID,
    NOT_FOUND,
    CONFLICT
}
