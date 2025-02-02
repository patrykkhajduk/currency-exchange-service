package io.hydev.currency.exchange.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import io.hydev.currency.exchange.domain.model.Account
import io.hydev.currency.exchange.domain.model.Currency
import io.hydev.currency.exchange.domain.model.repository.AccountRepository
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate
import io.hydev.currency.exchange.rate.domain.model.repository.ExchangeRateRepository
import io.hydev.currency.exchange.rate.nbp.client.NbpClient
import io.hydev.currency.exchange.rate.nbp.client.model.NbpExchangeRatesResponse
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

import java.time.LocalDate

@Component
class TestHelper {

    private final ObjectMapper objectMapper
    private final AccountRepository currencyExchangeAccountRepository
    private final ExchangeRateRepository exchangeRateRepository

    TestHelper(ObjectMapper objectMapper,
               AccountRepository currencyExchangeAccountRepository,
               ExchangeRateRepository exchangeRateRepository) {
        this.objectMapper = objectMapper
        this.currencyExchangeAccountRepository = currencyExchangeAccountRepository
        this.exchangeRateRepository = exchangeRateRepository
    }

    void clearData() {
        currencyExchangeAccountRepository.deleteAll()
        exchangeRateRepository.deleteAll()
    }

    Account storeAccount(BigDecimal initialBalanceInPln = 1.23, String ownerFirstName = "Joe", String ownerLastName = "Doe") {
        return currencyExchangeAccountRepository.save(
                new Account(ownerFirstName, ownerLastName, Currency.PLN, initialBalanceInPln))
    }

    List<Account> findAllCurrencyExchangeAccounts() {
        return currencyExchangeAccountRepository.findAll()
    }

    Account findCurrencyExchangeAccount(String id) {
        return currencyExchangeAccountRepository.findById(id).get()
    }

    ExchangeRate storeExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, LocalDate forDate = LocalDate.now()) {
        return exchangeRateRepository.save(new ExchangeRate(fromCurrency, toCurrency, rate, forDate))
    }

    List<ExchangeRate> findAllExchangeRates() {
        return exchangeRateRepository.findAll()
    }

    void stubGetNbpExchangeRatesSuccessResponse(NbpExchangeRatesResponse response) {
        stubGetNbpExchangeRatesResponse([response])
    }

    void stubGetNbpNoExchangeRatesAvailableResponse() {
        stubGetNbpExchangeRatesResponse([])
    }

    void stubGetNbpExchangeRatesResponse(List<NbpExchangeRatesResponse> response) {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(NbpClient.EXCHANGE_RATES_TABLE_A_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(response))))
    }

    void stubGetNbpExchangeRatesFailureResponse() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(NbpClient.EXCHANGE_RATES_TABLE_A_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)))
    }
}
