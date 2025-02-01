package io.hydev.currency.exchange.domain.model;

import io.hydev.currency.exchange.utils.NumberUtils;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Entity
@Table
@EntityListeners(AuditingEntityListener.class)
@Audited
public class CurrencyExchangeAccount {

    @Id
    @UuidGenerator
    private String id;

    private String ownerFirstName;

    private String ownerLastName;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private List<CurrencyExchangeAccountBalance> balances;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public CurrencyExchangeAccount(String ownerFirstName,
                                   String ownerLastName,
                                   Currency initialCurrency,
                                   BigDecimal initialAmount) {
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.balances = Arrays.stream(Currency.values())
                .map(currency -> new CurrencyExchangeAccountBalance(
                        this,
                        currency,
                        currency == initialCurrency ? initialAmount : NumberUtils.getScaledDecimal()))
                .toList();
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    @Entity(name = "currency_exchange_account_balance")
    @Table
    @EntityListeners(AuditingEntityListener.class)
    @Audited
    public static class CurrencyExchangeAccountBalance {

        @Id
        @UuidGenerator
        private String id;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "currency_exchange_account_id")
        private CurrencyExchangeAccount account;

        @Enumerated(EnumType.STRING)
        private Currency currency;

        private BigDecimal amount;

        @CreatedDate
        private LocalDateTime createdDate;

        @LastModifiedDate
        private LocalDateTime lastModifiedDate;

        public CurrencyExchangeAccountBalance(CurrencyExchangeAccount account, Currency currency, BigDecimal amount) {
            this.account = account;
            this.currency = currency;
            this.amount = amount;
        }
    }
}
