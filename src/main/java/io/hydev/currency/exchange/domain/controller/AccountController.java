package io.hydev.currency.exchange.domain.controller;

import io.hydev.currency.exchange.domain.controller.model.AccountDto;
import io.hydev.currency.exchange.domain.controller.model.AccountSearchResultDto;
import io.hydev.currency.exchange.domain.controller.model.CreateAccountRequest;
import io.hydev.currency.exchange.domain.controller.model.ExchangeCurrenciesRequest;
import io.hydev.currency.exchange.domain.controller.model.validator.CreateCurrencyExchangeAccountRequestValidator;
import io.hydev.currency.exchange.domain.service.AccountService;
import io.hydev.currency.exchange.validation.Uuid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(AccountController.BASE_PATH)
@Validated
public class AccountController {

    public static final String BASE_PATH = "/api/v1/currency-exchange/account";

    private final CreateCurrencyExchangeAccountRequestValidator createCurrencyExchangeAccountRequestValidator;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<Page<AccountSearchResultDto>> findAllAccounts(
            @RequestParam(value = "pageNumber", defaultValue = "0")
            @PositiveOrZero(message = "Page number must be positive or zero")
            int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10")
            @Positive(message = "Page size must be positive number")
            int pageSize) {
        return ResponseEntity.ok(
                accountService.findAllAccounts(pageNumber, pageSize)
                        .map(AccountSearchResultDto::from));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> findAccount(
            @PathVariable("accountId") @Uuid(message = "Account id is not UUID") String accountId) {
        return ResponseEntity.of(
                accountService.findAccount(accountId)
                        .map(AccountDto::from));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto createAccount(@RequestBody @Valid CreateAccountRequest request) {
        createCurrencyExchangeAccountRequestValidator.validate(request);
        return AccountDto.from(accountService.createAccount(request));
    }

    @PostMapping("/{accountId}/exchange")
    public AccountDto exchange(@PathVariable("accountId") @Uuid(message = "Account id is not UUID") String accountId,
                               @RequestBody @Valid ExchangeCurrenciesRequest request) {
        return AccountDto.from(accountService.exchange(accountId, request));
    }
}
