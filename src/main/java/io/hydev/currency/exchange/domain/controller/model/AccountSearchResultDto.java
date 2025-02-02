package io.hydev.currency.exchange.domain.controller.model;

import io.hydev.currency.exchange.domain.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
public class AccountSearchResultDto {

    public static AccountSearchResultDto from(Account account) {
        return AccountSearchResultDto.builder()
                .id(account.getId())
                .ownerFirstName(account.getOwnerFirstName())
                .ownerLastName(account.getOwnerLastName())
                .createdDate(account.getCreatedDate())
                .lastModifiedDate(account.getLastModifiedDate())
                .build();
    }

    private String id;
    private String ownerFirstName;
    private String ownerLastName;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
