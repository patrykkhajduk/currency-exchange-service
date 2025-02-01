package io.hydev.currency.exchange

import io.restassured.RestAssured
import org.apache.http.HttpStatus
import org.hamcrest.Matchers

class ApplicationStartTest extends BaseIntegrationTest {

    def "should start application and expose health endpoint"() {
        expect:
        RestAssured.when()
                .get("/actuator/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("status", Matchers.equalTo("UP"))
    }
}
