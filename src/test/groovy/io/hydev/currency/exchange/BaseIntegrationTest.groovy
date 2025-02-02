package io.hydev.currency.exchange

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.hydev.currency.exchange.utils.TestHelper
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(classes = CurrencyExchangeApplication.class, webEnvironment = RANDOM_PORT)
abstract class BaseIntegrationTest extends Specification {

    private static final int WIREMOCK_PORT = new Random().nextInt(1000, 8000)
    //Initializing Wiremock server as const to have consistency between application context (properties) and multiple test class runs
    private static final WireMockServer wireMockServer = new WireMockServer(WIREMOCK_PORT).with {
        it.start()
        return it
    }

    @Shared
    public static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:17.2")
            .withDatabaseName("currency-exchange")
            .withUsername("test")
            .withPassword("secret")

    @LocalServerPort
    private int port

    @Value('${server.servlet.context-path}')
    private String contextPath

    @Autowired
    protected ObjectMapper objectMapper

    @Autowired
    protected TestHelper testHelper

    def setupSpec() {
        postgresContainer.start()
        WireMock.configureFor(WIREMOCK_PORT);
    }

    def setup() {
        RestAssured.port = port
        RestAssured.baseURI = "http://localhost:$port$contextPath"
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        wireMockServer.resetAll()
        testHelper.clearData()
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgresContainer.getJdbcUrl())
        registry.add("spring.datasource.username", () -> postgresContainer.getUsername())
        registry.add("spring.datasource.password", () -> postgresContainer.getPassword())
        registry.add("spring.datasource.hikari.minimum-idle", () -> 3)
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> 5)
        registry.add("spring.liquibase.url", () -> postgresContainer.getJdbcUrl())
        registry.add("spring.liquibase.user", () -> postgresContainer.getUsername())
        registry.add("spring.liquibase.password", () -> postgresContainer.getPassword())
        registry.add("nbp.client.connection-properties.base-url", () -> "http://localhost:$WIREMOCK_PORT")
        registry.add("nbp.scheduler.update-exchange-rates-cron", () -> "0 0 0 * * *")
        registry.add("nbp.scheduler.update-exchange-rates-on-startup", () -> false)
    }
}
