package co.com.franchise.model.rule;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class StockRuleTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 500})
    void shouldAcceptStockWhenItIsZeroOrGreater(int stock) {
        StepVerifier.create(StockRule.validate(stock))
                .expectNext(stock)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenStockIsNegative() {
        StepVerifier.create(StockRule.validate(-1))
                .expectErrorSatisfies(StockRuleTest::assertInvalidStock)
                .verify();
    }

    @Test
    void shouldFailWhenStockIsMissing() {
        StepVerifier.create(StockRule.validate(null))
                .expectErrorSatisfies(StockRuleTest::assertInvalidStock)
                .verify();
    }

    private static void assertInvalidStock(Throwable error) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(BusinessError.INVALID_STOCK));
    }
}
