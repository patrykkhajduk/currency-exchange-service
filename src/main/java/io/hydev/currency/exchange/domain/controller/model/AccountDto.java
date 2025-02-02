package io.hydev.currency.exchange.domain.controller.model;

import io.hydev.currency.exchange.domain.model.Account;
import io.hydev.currency.exchange.domain.model.Account.SubAccount;
import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.utils.NumberUtils;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Builder
public record AccountDto(String id,
                         String ownerFirstName,
                         String ownerLastName,
                         List<SubAccountBalanceDto> subAccounts,
                         LocalDateTime createdDate,
                         LocalDateTime lastModifiedDate) {

    public static AccountDto from(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .ownerFirstName(account.getOwnerFirstName())
                .ownerLastName(account.getOwnerLastName())
                .subAccounts(account.getSubAccounts()
                        .stream()
                        .map(SubAccountBalanceDto::from)
                        .sorted(Comparator.comparing(SubAccountBalanceDto::currency))
                        .toList())
                .createdDate(account.getCreatedDate())
                .lastModifiedDate(account.getLastModifiedDate())
                .build();
    }

    @Builder
    public record SubAccountBalanceDto(String id,
                                       Currency currency,
                                       BigDecimal amount,
                                       LocalDateTime createdDate,
                                       LocalDateTime lastModifiedDate) {

        public static SubAccountBalanceDto from(SubAccount balance) {
            return SubAccountBalanceDto.builder()
                    .id(balance.getId())
                    .currency(balance.getCurrency())
                    .amount(NumberUtils.scale(balance.getCurrency(), balance.getAmount()))
                    .createdDate(balance.getCreatedDate())
                    .lastModifiedDate(balance.getLastModifiedDate())
                    .build();
        }
    }
}


