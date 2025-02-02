package io.hydev.currency.exchange.rate.domain.service;

import com.google.common.base.Preconditions;
import io.hydev.currency.exchange.domain.exception.CurrencyExchangeException;
import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate;
import io.hydev.currency.exchange.rate.domain.model.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ExchangeRateProvider {

    private final ExchangeRateRepository exchangeRateRepository;

    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE;
        }
        return exchangeRateRepository.findById(new ExchangeRate.ExchangeRateId(fromCurrency, toCurrency))
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new CurrencyExchangeException(
                        "Exchange rate not available for %s to %s".formatted(fromCurrency, toCurrency)));
    }
}
