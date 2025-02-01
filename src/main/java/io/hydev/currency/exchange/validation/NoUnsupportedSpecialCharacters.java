package io.hydev.currency.exchange.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = NoUnsupportedSpecialCharactersValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoUnsupportedSpecialCharacters {

    String message() default "Value has invalid characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
