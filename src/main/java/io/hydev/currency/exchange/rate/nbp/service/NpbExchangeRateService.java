package io.hydev.currency.exchange.rate.nbp.service;

import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate;
import io.hydev.currency.exchange.rate.domain.model.repository.ExchangeRateRepository;
import io.hydev.currency.exchange.rate.nbp.client.model.NbpExchangeRatesResponse;
import io.hydev.currency.exchange.utils.NumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NpbExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    //Transactional to ensure that both sides are updated or none in case of error
    public void updateExchangeRate(Currency toCurrency, NbpExchangeRatesResponse rates) {
        if (isExchangeRateActual(toCurrency, rates)) {
            log.info("Skipping update of exchange rate from {} to {}, rate is up to date", Currency.PLN, toCurrency);
        } else {
            storeExchangeRates(toCurrency, rates);
        }
    }

    private boolean isExchangeRateActual(Currency toCurrency, NbpExchangeRatesResponse rates) {
        return exchangeRateRepository.existsByFromCurrencyAndToCurrencyAndForDate(
                Currency.PLN, toCurrency, rates.effectiveDate());
    }

    private void storeExchangeRates(Currency toCurrency, NbpExchangeRatesResponse rates) {
        BigDecimal fromPlnRate = rates.getForCurrency(toCurrency);
        createExchangeRate(Currency.PLN, toCurrency, fromPlnRate, rates.effectiveDate());

        BigDecimal toCurrencyRate = BigDecimal.ONE.divide(fromPlnRate, NumberUtils.getMathContextForCalculations());
        createExchangeRate(toCurrency, Currency.PLN, toCurrencyRate, rates.effectiveDate());
    }

    private void createExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, LocalDate forDate) {
        ExchangeRate exchangeRate = exchangeRateRepository.save(new ExchangeRate(fromCurrency, toCurrency, rate, forDate));
        log.info("Created exchange rate {} with value {} from {} to {} for date {}",
                exchangeRate, rate, fromCurrency, toCurrency, forDate);
    }
}
