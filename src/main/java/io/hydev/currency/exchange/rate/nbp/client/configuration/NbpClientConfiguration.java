package io.hydev.currency.exchange.rate.nbp.client.configuration;

import io.hydev.currency.exchange.rate.nbp.client.NbpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class NbpClientConfiguration {

    @Bean
    NbpClient nbpClient(NbpClientProperties nbpClientProperties, RestTemplateBuilder builder) {
        return new NbpClient(
                nbpClientProperties.connectionProperties()
                        .apply(builder)
                        .build());
    }
}
