package io.hydev.currency.exchange.rate.nbp.scheduler

import io.hydev.currency.exchange.BaseIntegrationTest
import io.hydev.currency.exchange.domain.model.Currency
import io.hydev.currency.exchange.rate.domain.model.ExchangeRate
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
        ratesByFromCurrency[Currency.PLN].forDate == rates.effectiveDate
        ratesByFromCurrency[Currency.PLN].createdDate != null
        ratesByFromCurrency[Currency.PLN].lastModifiedDate != null
        ratesByFromCurrency[Currency.USD].rate == 0.2469136
        ratesByFromCurrency[Currency.USD].forDate == rates.effectiveDate
        ratesByFromCurrency[Currency.USD].createdDate != null
        ratesByFromCurrency[Currency.USD].lastModifiedDate != null
    }

    def "updateAllExchangeRates should update exchange rate when already exists"() {
        given:
        ExchangeRate plnUsdExchangeRate = testHelper.storeExchangeRate(
                Currency.PLN, Currency.USD, 4.00, LocalDate.now().minusDays(1))
        ExchangeRate usdPlnExchangeRate = testHelper.storeExchangeRate(
                Currency.USD, Currency.PLN, 0.25, LocalDate.now().minusDays(1))

        testHelper.stubGetNbpExchangeRatesSuccessResponse(rates)

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        Map<Currency, ExchangeRate> ratesByFromCurrency = getRatesByFromCurrency(testHelper.findAllExchangeRates())
        ratesByFromCurrency.size() == 2
        ratesByFromCurrency[Currency.PLN].lockVersion == plnUsdExchangeRate.lockVersion + 1
        ratesByFromCurrency[Currency.PLN].rate == 4.05
        ratesByFromCurrency[Currency.PLN].forDate == rates.effectiveDate
        ratesByFromCurrency[Currency.PLN].createdDate == plnUsdExchangeRate.createdDate
        ratesByFromCurrency[Currency.PLN].lastModifiedDate != plnUsdExchangeRate.lastModifiedDate
        ratesByFromCurrency[Currency.USD].lockVersion == usdPlnExchangeRate.lockVersion + 1
        ratesByFromCurrency[Currency.USD].rate == 0.2469136
        ratesByFromCurrency[Currency.USD].forDate == rates.effectiveDate
        ratesByFromCurrency[Currency.USD].createdDate == usdPlnExchangeRate.createdDate
        ratesByFromCurrency[Currency.USD].lastModifiedDate != usdPlnExchangeRate.lastModifiedDate
    }

    def "updateAllExchangeRates should not update exchange rates when actual"() {
        given:
        ExchangeRate plnUsdExchangeRate = testHelper.storeExchangeRate(
                Currency.PLN, Currency.USD, 4.05, LocalDate.now())
        ExchangeRate usdPlnExchangeRate = testHelper.storeExchangeRate(
                Currency.USD, Currency.PLN, 0.2469136, LocalDate.now())

        testHelper.stubGetNbpExchangeRatesSuccessResponse(rates)

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        Map<Currency, ExchangeRate> ratesByFromCurrency = getRatesByFromCurrency(testHelper.findAllExchangeRates())
        ratesByFromCurrency.size() == 2
        ratesByFromCurrency[Currency.PLN].lockVersion == plnUsdExchangeRate.lockVersion
        ratesByFromCurrency[Currency.PLN].rate == 4.05
        ratesByFromCurrency[Currency.USD].lockVersion == usdPlnExchangeRate.lockVersion
        ratesByFromCurrency[Currency.USD].rate == 0.2469136
    }

    def "updateAllExchangeRates should not upsert exchange rate when connection to NBP fails"() {
        given:
        testHelper.stubGetNbpExchangeRatesFailureResponse()

        when:
        nbpScheduler.updateAllExchangeRates()

        then:
        thrown(NbpCommunicationException)

        and:
        testHelper.findAllExchangeRates().isEmpty()
    }

    private static Map<Currency, ExchangeRate> getRatesByFromCurrency(List<ExchangeRate> exchangeRates) {
        return exchangeRates.collectEntries { [it.id.fromCurrency, it] }
    }
}
