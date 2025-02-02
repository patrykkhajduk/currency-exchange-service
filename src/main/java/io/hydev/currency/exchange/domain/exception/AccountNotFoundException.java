package io.hydev.currency.exchange.domain.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String id) {
        super("Could not find currency exchange account with id: " + id);
    }
}
