package io.hydev.currency.exchange.rate.nbp.client.configuration;

import io.hydev.currency.exchange.utils.ConnectionProperties;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "nbp.client")
@Validated
record NbpClientProperties(@NotNull ConnectionProperties connectionProperties) {
}
