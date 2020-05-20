package org.acme.optaplanner.ui;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class WebjarLoadingTest {

    @Test
    public void getBootstrapCss() {
        given()
                .when().get("webjars/bootstrap/4.3.1/css/bootstrap.min.css")
                .then()
                .statusCode(200);
    }

}
