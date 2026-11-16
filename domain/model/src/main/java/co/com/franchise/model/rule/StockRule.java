package co.com.franchise.model.rule;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import reactor.core.publisher.Mono;

/**
 * Stock rule: a product can be out of stock but never below zero.
 */
public final class StockRule {

    private static final int MIN_STOCK = 0;

    private StockRule() {
    }

    /**
     * Emits the stock, or {@link BusinessError#INVALID_STOCK} when it is missing or negative.
     */
    public static Mono<Integer> validate(Integer candidate) {
        return Mono.justOrEmpty(candidate)
                .filter(stock -> stock >= MIN_STOCK)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.INVALID_STOCK)));
    }
}
