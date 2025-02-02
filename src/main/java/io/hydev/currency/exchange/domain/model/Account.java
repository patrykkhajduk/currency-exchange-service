package io.hydev.currency.exchange.domain.model;

import io.hydev.currency.exchange.domain.exception.CurrencyExchangeException;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate;
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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.envers.Audited;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Entity
@Table
@EntityListeners(AuditingEntityListener.class)
@Audited
public class Account {

    @Id
    @UuidGenerator
    private String id;

    @Version
    private Integer lockVersion;

    private String ownerFirstName;

    private String ownerLastName;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "mainAccount", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private List<SubAccount> subAccounts;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public Account(String ownerFirstName,
                   String ownerLastName,
                   Currency initialCurrency,
                   BigDecimal initialAmount) {
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.subAccounts = Arrays.stream(Currency.values())
                .map(currency -> new SubAccount(
                        this,
                        currency,
                        currency == initialCurrency ? initialAmount : NumberUtils.getScaledZero(initialCurrency)))
                .toList();
    }

    public void exchange(Currency fromCurrency, Currency toCurrency, BigDecimal amount, ExchangeRate exchangeRate) {
        SubAccount fromSubAccount = getSubAccount(fromCurrency);
        SubAccount toSubAccount = getSubAccount(toCurrency);
        if (fromSubAccount.getAmount().compareTo(amount) < 0) {
            throw new CurrencyExchangeException("Not enough funds to exchange");
        }
        BigDecimal minimumExchangeAmount = NumberUtils.getMinimumExchangeAmount(toCurrency)
                .multiply(exchangeRate.getRate(), NumberUtils.getMathContextForCalculations());
        if (amount.compareTo(minimumExchangeAmount) < 0) {
            throw new CurrencyExchangeException("Amount is less than required for minimum exchange");
        }
        BigDecimal obtainedAmount = amount.divide(exchangeRate.getRate(), NumberUtils.getMathContextForCalculations());
        BigDecimal toSubAccountAmountBeforeExchange = toSubAccount.getAmount();
        toSubAccount.addAmount(obtainedAmount);

        BigDecimal exchangedAmount = obtainedAmount.multiply(exchangeRate.getRate(), NumberUtils.getMathContextForCalculations());
        BigDecimal fromSubAccountAmountBeforeExchange = fromSubAccount.getAmount();
        fromSubAccount.subtractAmount(exchangedAmount);

        //For now just logging the operation but this would be a good place to send a message with the exchange details
        log.info("Exchanged {} {} (sub account balance: {} -> {}) for {} {} (sub account balance: {} -> {}), used rate: {}",
                exchangedAmount, fromCurrency, fromSubAccountAmountBeforeExchange, fromSubAccount.getAmount(),
                obtainedAmount, toCurrency, toSubAccountAmountBeforeExchange, toSubAccount.getAmount(),
                exchangeRate.getId());
    }

    private SubAccount getSubAccount(Currency currency) {
        return subAccounts.stream()
                .filter(balance -> balance.getCurrency() == currency)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account does not have balance for currency: " + currency));
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    @Entity(name = "sub_account")
    @Table
    @EntityListeners(AuditingEntityListener.class)
    @Audited
    public static class SubAccount {

        @Id
        @UuidGenerator
        private String id;

        @Version
        private Integer lockVersion;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "main_account_id")
        private Account mainAccount;

        @Enumerated(EnumType.STRING)
        private Currency currency;

        private BigDecimal amount;

        @CreatedDate
        private LocalDateTime createdDate;

        @LastModifiedDate
        private LocalDateTime lastModifiedDate;

        private SubAccount(Account mainAccount, Currency currency, BigDecimal amount) {
            this.mainAccount = mainAccount;
            this.currency = currency;
            this.amount = amount;
        }

        private void addAmount(BigDecimal amount) {
            this.amount = this.amount.add(amount);
        }

        private void subtractAmount(BigDecimal amount) {
            this.amount = this.amount.subtract(amount);
            Preconditions.checkArgument(this.amount.compareTo(BigDecimal.ZERO) >= 0, "Balance cannot be negative");
        }
    }
}
