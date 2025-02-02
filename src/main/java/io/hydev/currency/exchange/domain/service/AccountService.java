package io.hydev.currency.exchange.domain.service;

import io.hydev.currency.exchange.domain.controller.model.CreateAccountRequest;
import io.hydev.currency.exchange.domain.controller.model.ExchangeCurrenciesRequest;
import io.hydev.currency.exchange.domain.exception.CurrencyExchangeException;
import io.hydev.currency.exchange.domain.model.Account;
import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.domain.model.repository.AccountRepository;
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate;
import io.hydev.currency.exchange.rate.domain.model.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository currencyExchangeRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public Page<Account> findAllAccounts(int pageNumber, int pageSize) {
        return currencyExchangeRepository.findAll(
                PageRequest.of(pageNumber, pageSize).withSort(Direction.ASC, "createdDate"));
    }

    public Optional<Account> findAccount(String accountId) {
        return currencyExchangeRepository.findById(accountId);
    }

    public Account createAccount(CreateAccountRequest request) {
        Account account = currencyExchangeRepository.save(request.toAccount());
        log.info("Created account {}", account.getId());
        return account;
    }

    public Account exchange(String accountId, ExchangeCurrenciesRequest request) {
        log.info("Exchanging {} {} to {} in account {}",
                request.getAmount(), request.getFromCurrency(), request.getToCurrency(), accountId);
        Account account = currencyExchangeRepository.getById(accountId);
        ExchangeRate exchangeRate = resolveExchangeRate(request.getFromCurrency(), request.getToCurrency());
        account.exchange(request.getFromCurrency(), request.getToCurrency(), request.getAmount(), exchangeRate);
        return currencyExchangeRepository.save(account);
    }

    private ExchangeRate resolveExchangeRate(Currency fromCurrency, Currency toCurrency) {
        return exchangeRateRepository.findLatestForCurrency(fromCurrency, toCurrency)
                .orElseThrow(() -> new CurrencyExchangeException(
                        "Exchange rate not available for %s to %s".formatted(fromCurrency, toCurrency)));
    }
}
