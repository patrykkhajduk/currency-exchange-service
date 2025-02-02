package io.hydev.currency.exchange.domain.controller.model;

import io.hydev.currency.exchange.domain.model.Account;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AccountSearchResultDto(String id,
                                     String ownerFirstName,
                                     String ownerLastName,
                                     LocalDateTime createdDate,
                                     LocalDateTime lastModifiedDate) {
    public static AccountSearchResultDto from(Account account) {
        return AccountSearchResultDto.builder()
                .id(account.getId())
                .ownerFirstName(account.getOwnerFirstName())
                .ownerLastName(account.getOwnerLastName())
                .createdDate(account.getCreatedDate())
                .lastModifiedDate(account.getLastModifiedDate())
                .build();
    }
}

