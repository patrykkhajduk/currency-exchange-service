package io.hydev.currency.exchange.domain.controller.model;

import io.hydev.currency.exchange.domain.model.Account;
import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.utils.NumberUtils;
import io.hydev.currency.exchange.validation.NoUnsupportedSpecialCharacters;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.apache.commons.text.WordUtils;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.Locale;

@Builder
public record CreateAccountRequest(
        @NotBlank(message = "Owner first name is mandatory")
        @Length(max = 64, message = "Owner first name must have at most {value} characters")
        @NoUnsupportedSpecialCharacters(message = "Owner first name contains invalid characters")
        String ownerFirstName,
        @NotBlank(message = "Owner last name is mandatory")
        @Length(max = 64, message = "Owner last name must have at most {value} characters")
        @NoUnsupportedSpecialCharacters(allowedSpecialCharactersRegexp = "[-]*", message = "Owner last name contains invalid characters")
        String ownerLastName,
        @NotNull(message = "Initial balance in PLN is mandatory")
        @PositiveOrZero(message = "Initial balance in PLN must be positive or zero")
        @Digits(integer = 9, fraction = 2, message = "Initial balance in PLN must be less than trillion and have at most 2 decimal places")
        BigDecimal initialBalanceInPln) {

    public Account toAccount() {
        return new Account(
                WordUtils.capitalize(ownerFirstName.toLowerCase(Locale.ROOT)),
                WordUtils.capitalize(ownerLastName.toLowerCase(Locale.ROOT), ' ', '-'),
                Currency.PLN,
                NumberUtils.scale(Currency.PLN, initialBalanceInPln));
    }
}
