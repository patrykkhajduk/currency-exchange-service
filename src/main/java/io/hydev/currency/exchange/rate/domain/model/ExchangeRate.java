package io.hydev.currency.exchange.rate.domain.model;

import io.hydev.currency.exchange.domain.model.Currency;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Entity
@Table
@EntityListeners(AuditingEntityListener.class)
@Audited
public class ExchangeRate {

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class ExchangeRateId implements Serializable {

        @Enumerated(EnumType.STRING)
        private Currency fromCurrency;

        @Enumerated(EnumType.STRING)
        private Currency toCurrency;
    }

    @EmbeddedId
    private ExchangeRateId id;

    @Version
    private Integer lockVersion;

    private BigDecimal rate;

    private LocalDate forDate;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public ExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, LocalDate forDate) {
        this.id = new ExchangeRateId(fromCurrency, toCurrency);
        this.rate = rate;
        this.forDate = forDate;
    }

    public void updateRate(BigDecimal rate, LocalDate forDate) {
        this.rate = rate;
        this.forDate = forDate;
    }
}
