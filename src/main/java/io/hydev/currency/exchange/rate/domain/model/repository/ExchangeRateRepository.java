package io.hydev.currency.exchange.rate.domain.model.repository;

import io.hydev.currency.exchange.rate.domain.model.ExchangeRate;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate.ExchangeRateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, ExchangeRateId> {

    boolean existsByIdAndForDate(ExchangeRateId id, LocalDate forDate);
}
