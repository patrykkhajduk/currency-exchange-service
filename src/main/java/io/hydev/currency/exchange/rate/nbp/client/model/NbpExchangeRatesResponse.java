package io.hydev.currency.exchange.rate.nbp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.hydev.currency.exchange.domain.model.Currency;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Getter
public class NbpExchangeRatesResponse {

    @JsonProperty("effectiveDate")
    private LocalDate effectiveDate;

    @JsonProperty("rates")
    private Collection<Rate> rates;

    public BigDecimal getForCurrency(Currency currency) {
        return rates.stream()
                .filter(rate -> rate.getCode().equalsIgnoreCase(currency.name()))
                .findFirst()
                .map(Rate::getMid)
                .orElseThrow(() -> new IllegalStateException("No rates available for: " + currency));
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    @AllArgsConstructor
    @Getter
    public static class Rate {

        @JsonProperty("code")
        private String code;

        @JsonProperty("mid")
        private BigDecimal mid;
    }
}
