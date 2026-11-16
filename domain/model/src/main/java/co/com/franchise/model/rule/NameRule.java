package co.com.franchise.model.rule;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import reactor.core.publisher.Mono;

/**
 * Naming rule shared by franchises, branches and products: surrounding whitespace is removed and
 * the result must be non-empty and at most {@value #MAX_LENGTH} characters long.
 */
public final class NameRule {

    public static final int MAX_LENGTH = 100;

    private NameRule() {
    }

    /**
     * Emits the normalized name, or {@link BusinessError#INVALID_NAME} when the rule is not met.
     */
    public static Mono<String> validate(String candidate) {
        return Mono.justOrEmpty(candidate)
                .map(String::strip)
                .filter(name -> !name.isEmpty() && name.length() <= MAX_LENGTH)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.INVALID_NAME)));
    }
}
