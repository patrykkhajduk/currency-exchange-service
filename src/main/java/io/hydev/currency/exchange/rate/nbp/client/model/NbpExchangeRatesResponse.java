package io.hydev.currency.exchange.rate.nbp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.hydev.currency.exchange.domain.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

public record NbpExchangeRatesResponse(
        @JsonProperty("effectiveDate")
        LocalDate effectiveDate,
        @JsonProperty("rates")
        Collection<Rate> rates) {

    public BigDecimal getForCurrency(Currency currency) {
        return rates.stream()
                .filter(rate -> rate.code().equalsIgnoreCase(currency.name()))
                .findFirst()
                .map(Rate::mid)
                .orElseThrow(() -> new IllegalStateException("No rates available for: " + currency));
    }

    public record Rate(
            @JsonProperty("code")
            String code,
            @JsonProperty("mid")
            BigDecimal mid) {
    }
}
