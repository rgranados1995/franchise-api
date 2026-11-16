package co.com.franchise.model.rule;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class NameRuleTest {

    @Test
    void shouldStripSurroundingWhitespaceWhenNameIsValid() {
        StepVerifier.create(NameRule.validate("  Coffee House  "))
                .expectNext("Coffee House")
                .verifyComplete();
    }

    @Test
    void shouldAcceptNameWhenLengthIsExactlyTheMaximum() {
        String longestName = "a".repeat(NameRule.MAX_LENGTH);

        StepVerifier.create(NameRule.validate(longestName))
                .expectNext(longestName)
                .verifyComplete();
    }

    @Test
    void shouldIgnoreSurroundingWhitespaceWhenMeasuringLength() {
        String longestName = "a".repeat(NameRule.MAX_LENGTH);

        StepVerifier.create(NameRule.validate(" " + longestName + " "))
                .expectNext(longestName)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenNameExceedsTheMaximumLength() {
        StepVerifier.create(NameRule.validate("a".repeat(NameRule.MAX_LENGTH + 1)))
                .expectErrorSatisfies(NameRuleTest::assertInvalidName)
                .verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void shouldFailWhenNameIsMissingOrBlank(String candidate) {
        StepVerifier.create(NameRule.validate(candidate))
                .expectErrorSatisfies(NameRuleTest::assertInvalidName)
                .verify();
    }

    private static void assertInvalidName(Throwable error) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(BusinessError.INVALID_NAME));
    }
}
