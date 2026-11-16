package co.com.franchise.model.exception;

import lombok.Getter;

/**
 * Signals a {@link BusinessError}. It is unchecked so it can travel as an error signal through
 * reactive pipelines and be translated once at the entry point.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final BusinessError error;

    public BusinessException(BusinessError error) {
        super(error.getMessage());
        this.error = error;
    }
}
