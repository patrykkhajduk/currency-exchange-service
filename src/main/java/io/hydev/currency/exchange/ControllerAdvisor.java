package io.hydev.currency.exchange;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import io.hydev.currency.exchange.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(getErrorsResponseBody(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Map<String, List<String>>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()),
                        fieldError -> Lists.newArrayList(fieldError.getDefaultMessage()),
                        (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        }));
        return new ResponseEntity<>(getFieldErrorsResponseBody(fieldErrors), HttpStatus.BAD_REQUEST);
    }

    private Map<String, List<String>> getErrorsResponseBody(String... errors) {
        return Map.of("errors", List.of(errors));
    }

    private Map<String, Map<String, List<String>>> getFieldErrorsResponseBody(Map<String, List<String>> fieldErrors) {
        return Map.of("field_errors", fieldErrors);
    }
}
