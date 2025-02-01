package io.hydev.currency.exchange.controller.model.validator;

import io.hydev.currency.exchange.controller.model.CreateCurrencyExchangeAccountRequest;
import io.hydev.currency.exchange.domain.model.repository.CurrencyExchangeAccountRepository;
import io.hydev.currency.exchange.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCurrencyExchangeAccountRequestValidator {

    private final CurrencyExchangeAccountRepository currencyExchangeAccountRepository;

    public void validate(CreateCurrencyExchangeAccountRequest request) {
        if (isOwnerDuplication(request)) {
            throw new ValidationException("Account with this owner already exists");
        }
    }

    private boolean isOwnerDuplication(CreateCurrencyExchangeAccountRequest request) {
        return currencyExchangeAccountRepository.existsByOwnerFirstNameAndOwnerLastName(
                request.getOwnerFirstName(), request.getOwnerLastName());
    }
}
