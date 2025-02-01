package io.hydev.currency.exchange.utils;

import lombok.experimental.UtilityClass;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public final class NumberUtils {

    private static final BigDecimal SCALED_ZERO = new BigDecimal("0.00");

    public static BigDecimal getScaledDecimal() {
        // Ready for future extension when currency will be taken into account
        // Yen currency has 3 digis after decimal point
        return SCALED_ZERO;
    }

    public static BigDecimal scale(BigDecimal value) {
        Preconditions.castNonNull(value);
        Preconditions.checkArgument(value.scale() <= 2, "Value scale is greater than 2");

        return value.setScale(2, RoundingMode.HALF_EVEN);
    }
}
