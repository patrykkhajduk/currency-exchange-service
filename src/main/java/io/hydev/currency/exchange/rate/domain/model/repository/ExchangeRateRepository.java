package io.hydev.currency.exchange.rate.domain.model.repository;

import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {

    boolean existsByFromCurrencyAndToCurrencyAndForDate(Currency fromCurrency, Currency toCurrency, LocalDate forDate);

    default Optional<ExchangeRate> findLatestForCurrency(Currency fromCurrency, Currency toCurrency) {
        return findTopByFromCurrencyAndToCurrencyOrderByForDateDesc(fromCurrency, toCurrency);
    }

    Optional<ExchangeRate> findTopByFromCurrencyAndToCurrencyOrderByForDateDesc(Currency fromCurrency, Currency toCurrency);
}
