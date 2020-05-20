package org.acme.optaplanner.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.acme.optaplanner.domain.Timeslot;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class TimeslotResourceTest {

    @Test
    public void getAll() {
        List<Timeslot> timeslotList = given()
                .when().get("/timeslots")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", Timeslot.class);
        assertFalse(timeslotList.isEmpty());
        Timeslot firstTimeslot = timeslotList.get(0);
        assertEquals(DayOfWeek.MONDAY, firstTimeslot.getDayOfWeek());
        assertEquals(LocalTime.of(8, 30), firstTimeslot.getStartTime());
        assertEquals(LocalTime.of(9, 30), firstTimeslot.getEndTime());
    }

    @Test
    void addAndRemove() {
        Timeslot timeslot = given()
                .when()
                .contentType(ContentType.JSON)
                .body(new Timeslot(DayOfWeek.SUNDAY, LocalTime.of(20, 0), LocalTime.of(21, 0)))
                .post("/timeslots")
                .then()
                .statusCode(202)
                .extract().as(Timeslot.class);

        given()
                .when()
                .delete("/timeslots/{id}", timeslot.getId())
                .then()
                .statusCode(200);
    }

}
