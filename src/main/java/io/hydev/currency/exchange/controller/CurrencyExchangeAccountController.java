package io.hydev.currency.exchange.controller;

import io.hydev.currency.exchange.controller.model.CreateCurrencyExchangeAccountRequest;
import io.hydev.currency.exchange.controller.model.CurrencyExchangeAccountDto;
import io.hydev.currency.exchange.controller.model.validator.CreateCurrencyExchangeAccountRequestValidator;
import io.hydev.currency.exchange.service.CurrencyExchangeAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CurrencyExchangeAccountController.BASE_PATH)
public class CurrencyExchangeAccountController {

    public static final String BASE_PATH = "/api/v1/currency-exchange/account";

    private final CreateCurrencyExchangeAccountRequestValidator createCurrencyExchangeAccountRequestValidator;
    private final CurrencyExchangeAccountService currencyExchangeAccountService;

    @GetMapping("/{account_id}")
    public ResponseEntity<CurrencyExchangeAccountDto> getAccount(@PathVariable("account_id") String accountId) {
        return ResponseEntity.of(
                currencyExchangeAccountService.findAccount(accountId)
                        .map(CurrencyExchangeAccountDto::from));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CurrencyExchangeAccountDto createAccount(@RequestBody @Valid CreateCurrencyExchangeAccountRequest request) {
        createCurrencyExchangeAccountRequestValidator.validate(request);
        return CurrencyExchangeAccountDto.from(currencyExchangeAccountService.createAccount(request));
    }
}
