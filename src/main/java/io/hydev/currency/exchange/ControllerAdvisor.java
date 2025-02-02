package io.hydev.currency.exchange;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import io.hydev.currency.exchange.domain.exception.AccountNotFoundException;
import io.hydev.currency.exchange.domain.exception.CurrencyExchangeException;
import io.hydev.currency.exchange.domain.exception.ValidationException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(getErrorsResponseBody(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Map<String, List<String>>>> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, List<String>> fieldErrors = resolveFieldErrors(e);
        return new ResponseEntity<>(getFieldErrorsResponseBody(fieldErrors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Map<String, List<String>>>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, List<String>> fieldErrors = resolveFieldErrors(e);
        return new ResponseEntity<>(getFieldErrorsResponseBody(fieldErrors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFoundException(AccountNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(CurrencyExchangeException.class)
    public ResponseEntity<Map<String, List<String>>> handleCurrencyExchangeException(CurrencyExchangeException e) {
        return new ResponseEntity<>(getErrorsResponseBody(e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private Map<String, List<String>> resolveFieldErrors(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()),
                        fieldError -> Lists.newArrayList(fieldError.getDefaultMessage()),
                        (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        }));
    }

    private Map<String, List<String>> resolveFieldErrors(ConstraintViolationException e) {
        return e.getConstraintViolations()
                .stream()
                .collect(Collectors.groupingBy(v -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, v.getPropertyPath().toString())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .map(constraintViolation -> Optional.ofNullable(constraintViolation.getMessage()).orElse("Invalid value"))
                                .toList()));
    }

    private Map<String, List<String>> getErrorsResponseBody(String... errors) {
        return Map.of("errors", List.of(errors));
    }

    private Map<String, Map<String, List<String>>> getFieldErrorsResponseBody(Map<String, List<String>> fieldErrors) {
        return Map.of("field_errors", fieldErrors);
    }
}
