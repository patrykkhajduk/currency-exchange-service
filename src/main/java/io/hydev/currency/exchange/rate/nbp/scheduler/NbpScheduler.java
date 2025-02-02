package io.hydev.currency.exchange.rate.nbp.scheduler;

import io.hydev.currency.exchange.domain.model.Currency;
import io.hydev.currency.exchange.rate.nbp.client.NbpClient;
import io.hydev.currency.exchange.rate.nbp.client.model.NbpExchangeRatesResponse;
import io.hydev.currency.exchange.rate.nbp.service.NpbExchangeRateService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
class NbpScheduler {

    private final boolean upsertRatesOnStartup;
    private final NbpClient nbpClient;
    private final NpbExchangeRateService npbExchangeRateService;

    public NbpScheduler(@Value("${nbp.scheduler.upsert-exchange-rates-on-startup}") boolean upsertRatesOnStartup,
                        NbpClient nbpClient,
                        NpbExchangeRateService npbExchangeRateService) {
        this.upsertRatesOnStartup = upsertRatesOnStartup;
        this.nbpClient = nbpClient;
        this.npbExchangeRateService = npbExchangeRateService;
    }

    @PostConstruct
    void init() {
        if (upsertRatesOnStartup) {
            log.info("Upserting exchange rates on startup");
            updateAllExchangeRates();
        }
    }

    //Separating updating of exchange rates from getting them on the fly when requesting conversion
    //This way we can ensure that the exchange rates once fetched will always be available
    //for all instances even if connection to NBP is down
    //Since there are not too many exchange rates to be updated the process can be run as a scheduled job
    //For more frequent updates or more currencies,
    //parallelization of calls can be performed or self notification via queue can be useful for horizontal scaling
    @Scheduled(cron = "${nbp.scheduler.upsert-exchange-rates-cron}")
    @SchedulerLock(name = "jobs.nbp.upsert-exchange-rates", lockAtMostFor = "${nbp.scheduler.upsert-exchange-rates-lock-at-most-for}")
    public void updateAllExchangeRates() {
        log.info("Updating all non repeating combinations of exchange rates");
        NbpExchangeRatesResponse rates = nbpClient.getExchangeRates();
        resolveAllNonPlnCurrencies()
                .forEach(currency -> upsertExchangeRate(currency, rates));
    }

    private static List<Currency> resolveAllNonPlnCurrencies() {
        return Arrays.stream(Currency.values())
                .filter(currency -> currency != Currency.PLN)
                .toList();
    }

    private void upsertExchangeRate(Currency toCurrency, NbpExchangeRatesResponse rates) {
        try {
            npbExchangeRateService.upsertExchangeRate(toCurrency, rates);
        } catch (Exception e) {
            //Avoid stopping upsert for other rates when one of them fails
            log.error("Failed to upsert exchange rate form {} to {}", Currency.PLN, toCurrency, e);
        }
    }
}
