package io.hydev.currency.exchange.utils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public record ConnectionProperties(
        @NotBlank String baseUrl,
        @NotNull @Positive Integer maxConnectionsPerRoute,
        @NotNull @Positive Integer maxTotalConnections,
        @NotNull Duration connectionRequestTimeout,
        @NotNull Duration responseTimeout,
        @NotNull Duration connectionTimeout) {

    public RestTemplateBuilder apply(RestTemplateBuilder builder) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        connectionManager.setMaxTotal(maxTotalConnections);

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(connectionRequestTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(responseTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return builder.rootUri(baseUrl).requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
