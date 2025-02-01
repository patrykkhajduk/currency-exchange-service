package io.hydev.currency.exchange.service;

import io.hydev.currency.exchange.controller.model.CreateCurrencyExchangeAccountRequest;
import io.hydev.currency.exchange.domain.model.CurrencyExchangeAccount;
import io.hydev.currency.exchange.domain.model.repository.CurrencyExchangeAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeAccountService {

    private final CurrencyExchangeAccountRepository currencyExchangeRepository;

    public Optional<CurrencyExchangeAccount> findAccount(String accountId) {
        return currencyExchangeRepository.findById(accountId);
    }

    public CurrencyExchangeAccount createAccount(CreateCurrencyExchangeAccountRequest request) {
        return currencyExchangeRepository.save(request.toAccount());
    }
}
