package io.hydev.currency.exchange.rate.nbp.client;

import io.hydev.currency.exchange.rate.nbp.client.exception.NbpCommunicationException;
import io.hydev.currency.exchange.rate.nbp.client.model.NbpExchangeRatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@RequiredArgsConstructor
public class NbpClient {

    public static final String EXCHANGE_RATES_TABLE_A_URL = "/api/exchangerates/tables/a";

    private final RestTemplate restTemplate;

    public NbpExchangeRatesResponse getExchangeRates() {
        try {
            return Arrays.stream(restTemplate.getForObject(EXCHANGE_RATES_TABLE_A_URL, NbpExchangeRatesResponse[].class))
                    .findFirst()
                    .orElseThrow(() -> new NbpCommunicationException("No exchange rates found"));
        } catch (HttpStatusCodeException e) {
            throw new NbpCommunicationException("Error when executing %s:%s got status: %s with body: %s"
                    .formatted(HttpMethod.GET, EXCHANGE_RATES_TABLE_A_URL, e.getStatusCode(), e.getResponseBodyAsString()), e);
        }
    }
}
