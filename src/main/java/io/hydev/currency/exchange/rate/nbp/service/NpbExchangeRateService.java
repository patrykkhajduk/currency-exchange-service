package io.hydev.currency.exchange.rate.nbp.service;

import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate.ExchangeRateId;
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
    public void upsertExchangeRate(Currency toCurrency, NbpExchangeRatesResponse rates) {
        if (isExchangeRateActual(toCurrency, rates)) {
            log.info("Skipping upsert of exchange rate from {} to {}, rate is up to date", Currency.PLN, toCurrency);
        } else {
            upsertExchangeRates(toCurrency, rates);
        }
    }

    private boolean isExchangeRateActual(Currency toCurrency, NbpExchangeRatesResponse rates) {
        return exchangeRateRepository.existsByIdAndForDate(
                new ExchangeRateId(Currency.PLN, toCurrency), rates.getEffectiveDate());
    }

    private void upsertExchangeRates(Currency toCurrency, NbpExchangeRatesResponse rates) {
        BigDecimal fromPlnRate = rates.getForCurrency(toCurrency);
        BigDecimal toCurrencyRate = BigDecimal.ONE.divide(fromPlnRate, NumberUtils.getMathContextForCalculations());

        upsertExchangeRate(Currency.PLN, toCurrency, fromPlnRate, rates.getEffectiveDate());
        upsertExchangeRate(toCurrency, Currency.PLN, toCurrencyRate, rates.getEffectiveDate());
    }

    private void upsertExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, LocalDate forDate) {
        exchangeRateRepository.findById(new ExchangeRateId(fromCurrency, toCurrency))
                .ifPresentOrElse(
                        exchangeRate -> updateExistingExchangeRate(exchangeRate, rate, forDate),
                        () -> createExchangeRate(fromCurrency, toCurrency, rate, forDate)
                );
    }

    private void updateExistingExchangeRate(ExchangeRate exchangeRate, BigDecimal rate, LocalDate forDate) {
        exchangeRate.updateRate(rate, forDate);
        exchangeRateRepository.save(exchangeRate);
        log.info("Updated exchange rate {} from {} to {} for date {}",
                rate, exchangeRate.getId().getFromCurrency(), exchangeRate.getId().getToCurrency(), forDate);
    }

    private void createExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, LocalDate forDate) {
        exchangeRateRepository.save(new ExchangeRate(fromCurrency, toCurrency, rate, forDate));
        log.info("Created exchange rate {} from {} to {} for date {}", rate, fromCurrency, toCurrency, forDate);
    }
}
