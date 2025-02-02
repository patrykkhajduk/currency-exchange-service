package io.hydev.currency.exchange.domain.controller.model.validator;

import io.hydev.currency.exchange.domain.controller.model.CreateAccountRequest;
import io.hydev.currency.exchange.domain.model.Account;
import io.hydev.currency.exchange.domain.model.repository.AccountRepository;
import io.hydev.currency.exchange.domain.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCurrencyExchangeAccountRequestValidator {

    private final AccountRepository accountRepository;

    public void validate(CreateAccountRequest request) {
        if (isOwnerDuplication(request)) {
            throw new ValidationException("Account with this owner already exists");
        }
    }

    private boolean isOwnerDuplication(CreateAccountRequest request) {
        Account account = request.toAccount();
        return accountRepository.existsByOwnerFirstNameAndOwnerLastName(account.getOwnerFirstName(), account.getOwnerLastName());
    }
}
