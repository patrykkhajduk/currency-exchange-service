package io.hydev.currency.exchange.utils;

import io.hydev.currency.exchange.domain.model.Currency;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@UtilityClass
// Ready for future extension since Yen currency has 3 digis after decimal point
public final class NumberUtils {

    private static final BigDecimal SCALED_ZERO = new BigDecimal("0.00");
    private static final BigDecimal MINIMUM_EXCHANGE_AMOUNT = new BigDecimal("0.01");

    public static MathContext getMathContextForCalculations() {
        return MathContext.DECIMAL32;
    }

    public static int getAmountScale(Currency currency) {
        return switch (currency) {
            default -> 2;
        };
    }

    public static BigDecimal getScaledZero(Currency currency) {
        return switch (currency) {
            default -> SCALED_ZERO;
        };
    }

    public BigDecimal getMinimumExchangeAmount(Currency currency) {
        return switch (currency) {
            default -> MINIMUM_EXCHANGE_AMOUNT;
        };
    }

    public static BigDecimal scale(Currency currency, BigDecimal value) {
        return value.setScale(getAmountScale(currency), RoundingMode.HALF_EVEN);
    }
}
