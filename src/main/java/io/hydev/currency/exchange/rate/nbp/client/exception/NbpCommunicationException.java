package io.hydev.currency.exchange.rate.nbp.client.exception;

public class NbpCommunicationException extends RuntimeException {

    public NbpCommunicationException(String message) {
        super(message);
    }

    public NbpCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
