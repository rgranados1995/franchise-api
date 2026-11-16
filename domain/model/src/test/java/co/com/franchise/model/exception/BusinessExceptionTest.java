package co.com.franchise.model.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void shouldExposeTheErrorAndUseItsMessage() {
        BusinessException exception = new BusinessException(BusinessError.PRODUCT_NOT_FOUND);

        assertThat(exception.getError()).isEqualTo(BusinessError.PRODUCT_NOT_FOUND);
        assertThat(exception.getError().getType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Product not found in the given branch");
    }

    @Test
    void shouldMentionTheMaximumLengthInTheInvalidNameMessage() {
        assertThat(BusinessError.INVALID_NAME.getMessage()).contains("100");
    }
}
