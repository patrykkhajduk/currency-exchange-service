package io.hydev.currency.exchange.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

@NoArgsConstructor
public class NoUnsupportedSpecialCharactersValidator implements ConstraintValidator<NoUnsupportedSpecialCharacters, String> {

    private static final Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("[a-zA-Z]*");

    private Pattern allowedSpecialCharactersPattern;

    public NoUnsupportedSpecialCharactersValidator(String allowedSpecialCharactersRegexp) {
        this.allowedSpecialCharactersPattern = Pattern.compile(allowedSpecialCharactersRegexp);
    }

    @Override
    public void initialize(NoUnsupportedSpecialCharacters constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        allowedSpecialCharactersPattern = Pattern.compile(constraintAnnotation.allowedSpecialCharactersRegexp());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        } else {
            String withoutAllowedSpecialCharacters = allowedSpecialCharactersPattern.matcher(value).replaceAll("");
            String canonicalWithoutAllowedSpecialCharacters = StringUtils.stripAccents(withoutAllowedSpecialCharacters);
            return ALLOWED_CHARACTERS_PATTERN.matcher(canonicalWithoutAllowedSpecialCharacters).matches();
        }
    }
}
