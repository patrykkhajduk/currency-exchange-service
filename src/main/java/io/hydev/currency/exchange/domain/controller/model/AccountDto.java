package io.hydev.currency.exchange.domain.controller.model;

import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.domain.model.Account;
import io.hydev.currency.exchange.domain.model.Account.SubAccount;
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
public class AccountDto {

    public static AccountDto from(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .ownerFirstName(account.getOwnerFirstName())
                .ownerLastName(account.getOwnerLastName())
                .subAccounts(account.getSubAccounts()
                        .stream()
                        .map(SubAccountBalanceDto::from)
                        .sorted(Comparator.comparing(SubAccountBalanceDto::getCurrency))
                        .toList())
                .createdDate(account.getCreatedDate())
                .lastModifiedDate(account.getLastModifiedDate())
                .build();
    }

    private String id;
    private String ownerFirstName;
    private String ownerLastName;
    private List<SubAccountBalanceDto> subAccounts;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    @AllArgsConstructor
    @Builder
    @Getter
    public static class SubAccountBalanceDto {

        public static SubAccountBalanceDto from(SubAccount balance) {
            return SubAccountBalanceDto.builder()
                    .id(balance.getId())
                    .currency(balance.getCurrency())
                    .amount(NumberUtils.scale(balance.getCurrency(), balance.getAmount()))
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
