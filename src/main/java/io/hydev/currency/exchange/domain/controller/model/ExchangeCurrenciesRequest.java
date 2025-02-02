package io.hydev.currency.exchange.domain.controller.model;

import io.hydev.currency.exchange.domain.model.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Builder
@Getter
public class ExchangeCurrenciesRequest {

    @NotNull(message = "From currency is mandatory")
    private Currency fromCurrency;

    @NotNull(message = "To currency is mandatory")
    private Currency toCurrency;

    @NotNull(message = "Amount is mandatory")
    @Positive(message = "Amount must be positive number")
    @Digits(integer = 9, fraction = 2, message = "Amount must be less than trillion and have at most 2 decimal places")
    private BigDecimal amount;
}
