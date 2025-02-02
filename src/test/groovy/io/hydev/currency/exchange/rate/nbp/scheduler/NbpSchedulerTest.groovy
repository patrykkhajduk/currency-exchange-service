package io.hydev.currency.exchange.rate.nbp.scheduler

import io.hydev.currency.exchange.BaseIntegrationTest
import io.hydev.currency.exchange.domain.model.Currency
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate
import io.hydev.currency.exchange.rate.nbp.client.NbpClient
import io.hydev.currency.exchange.rate.nbp.client.exception.NbpCommunicationException
import io.hydev.currency.exchange.rate.nbp.client.model.NbpExchangeRatesResponse
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Subject

import java.time.LocalDate

class NbpSchedulerTest extends BaseIntegrationTest {

    private NbpExchangeRatesResponse rates = new NbpExchangeRatesResponse(
            LocalDate.now(),
            [new NbpExchangeRatesResponse.Rate(Currency.USD.name(), 4.05),
             new NbpExchangeRatesResponse.Rate("EUR", 4.25)])

    @Autowired
    @Subject
    private NbpScheduler nbpScheduler

    def "updateAllExchangeRates should create new exchange rate when not yet initialized"() {
        given:
        testHelper.stubGetNbpExchangeRatesSuccessResponse(rates)

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        Map<Currency, ExchangeRate> ratesByFromCurrency = getRatesByFromCurrency(testHelper.findAllExchangeRates())
        ratesByFromCurrency.size() == 2
        ratesByFromCurrency[Currency.PLN].rate == 4.05
        ratesByFromCurrency[Currency.PLN].forDate == rates.effectiveDate()
        ratesByFromCurrency[Currency.PLN].createdDate != null
        ratesByFromCurrency[Currency.PLN].lastModifiedDate != null
        ratesByFromCurrency[Currency.USD].rate == 0.2469136
        ratesByFromCurrency[Currency.USD].forDate == rates.effectiveDate()
        ratesByFromCurrency[Currency.USD].createdDate != null
        ratesByFromCurrency[Currency.USD].lastModifiedDate != null
    }

    def "updateAllExchangeRates should add new exchange rates when already exists for older date"() {
        given:
        testHelper.storeExchangeRate(Currency.PLN, Currency.USD, 4.00, LocalDate.now().minusDays(1))
        testHelper.storeExchangeRate(Currency.USD, Currency.PLN, 0.25, LocalDate.now().minusDays(1))

        testHelper.stubGetNbpExchangeRatesSuccessResponse(rates)

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        List<ExchangeRate> exchangeRates = testHelper.findAllExchangeRates()
        exchangeRates.size() == 4

        and:
        Map<Currency, ExchangeRate> newRatesByFromCurrency = getRatesByFromCurrency(
                exchangeRates.findAll({ it.forDate == rates.effectiveDate()}))
        newRatesByFromCurrency.size() == 2
        newRatesByFromCurrency[Currency.PLN].rate == 4.05
        newRatesByFromCurrency[Currency.PLN].forDate == rates.effectiveDate()
        newRatesByFromCurrency[Currency.PLN].createdDate != null
        newRatesByFromCurrency[Currency.PLN].lastModifiedDate != null
        newRatesByFromCurrency[Currency.USD].rate == 0.2469136
        newRatesByFromCurrency[Currency.USD].forDate == rates.effectiveDate()
        newRatesByFromCurrency[Currency.USD].createdDate != null
        newRatesByFromCurrency[Currency.USD].lastModifiedDate != null
    }

    def "updateAllExchangeRates should not update exchange rates when actual"() {
        given:
        testHelper.storeExchangeRate(Currency.PLN, Currency.USD, 4.05, LocalDate.now())
        testHelper.storeExchangeRate(Currency.USD, Currency.PLN, 0.2469136, LocalDate.now())

        testHelper.stubGetNbpExchangeRatesSuccessResponse(rates)

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        testHelper.findAllExchangeRates().size() == 2
    }

    def "updateAllExchangeRates should not upsert exchange rate when connection to NBP fails"() {
        given:
        testHelper.stubGetNbpExchangeRatesFailureResponse()

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        NbpCommunicationException exception =thrown(NbpCommunicationException)
        exception.message.startsWith("Error when executing GET:$NbpClient.EXCHANGE_RATES_TABLE_A_URL got status: 500")

        and:
        testHelper.findAllExchangeRates().isEmpty()
    }

    def "updateAllExchangeRates should not upsert exchange rate NBP returns empty response"() {
        given:
        testHelper.stubGetNbpNoExchangeRatesAvailableResponse()

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        NbpCommunicationException exception =thrown(NbpCommunicationException)
        exception.message == "No exchange rates found"

        and:
        testHelper.findAllExchangeRates().isEmpty()
    }

    private static Map<Currency, ExchangeRate> getRatesByFromCurrency(List<ExchangeRate> exchangeRates) {
        return exchangeRates.collectEntries { [it.fromCurrency, it] }
    }
}
