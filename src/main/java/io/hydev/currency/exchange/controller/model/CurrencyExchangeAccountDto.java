package io.hydev.currency.exchange.controller.model;

import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.domain.model.CurrencyExchangeAccount;
import io.hydev.currency.exchange.domain.model.CurrencyExchangeAccount.CurrencyExchangeAccountBalance;
import io.hydev.currency.exchange.utils.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class CurrencyExchangeAccountDto {

    public static CurrencyExchangeAccountDto from(CurrencyExchangeAccount account) {
        return CurrencyExchangeAccountDto.builder()
                .id(account.getId())
                .ownerFirstName(account.getOwnerFirstName())
                .ownerLastName(account.getOwnerLastName())
                .balances(account.getBalances()
                        .stream()
                        .map(CurrencyExchangeAccountBalanceDto::from)
                        .sorted(Comparator.comparing(CurrencyExchangeAccountBalanceDto::getCurrency))
                        .toList())
                .createdDate(account.getCreatedDate())
                .lastModifiedDate(account.getLastModifiedDate())
                .build();
    }

    private String id;
    private String ownerFirstName;
    private String ownerLastName;
    private List<CurrencyExchangeAccountBalanceDto> balances;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    @AllArgsConstructor
    @Builder
    @Getter
    public static class CurrencyExchangeAccountBalanceDto {

        public static CurrencyExchangeAccountBalanceDto from(CurrencyExchangeAccountBalance balance) {
            return CurrencyExchangeAccountBalanceDto.builder()
                    .id(balance.getId())
                    .currency(balance.getCurrency())
                    .amount(NumberUtils.scale(balance.getAmount()))
                    .createdDate(balance.getCreatedDate())
                    .lastModifiedDate(balance.getLastModifiedDate())
                    .build();
        }

        private String id;
        private Currency currency;
        private BigDecimal amount;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
    }
}
