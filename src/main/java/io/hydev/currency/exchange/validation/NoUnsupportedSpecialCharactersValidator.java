package io.hydev.currency.exchange.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class NoUnsupportedSpecialCharactersValidator implements ConstraintValidator<NoUnsupportedSpecialCharacters, String> {

    //Letters and double surname join character '-'
    private static final Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("[a-zA-Z-]*");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        } else {
            String valueWithoutAccents = StringUtils.stripAccents(value);
            return ALLOWED_CHARACTERS_PATTERN.matcher(valueWithoutAccents).matches();
        }
    }
}
