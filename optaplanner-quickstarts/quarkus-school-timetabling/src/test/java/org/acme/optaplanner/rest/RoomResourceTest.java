package org.acme.optaplanner.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.acme.optaplanner.domain.Room;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class RoomResourceTest {

    @Test
    public void getAll() {
        List<Room> roomList = given()
                .when().get("/rooms")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", Room.class);
        assertFalse(roomList.isEmpty());
        Room firstRoom = roomList.get(0);
        assertEquals("Room A", firstRoom.getName());
    }

    @Test
    void addAndRemove() {
        Room room = given()
                .when()
                .contentType(ContentType.JSON)
                .body(new Room("Test room"))
                .post("/rooms")
                .then()
                .statusCode(202)
                .extract().as(Room.class);

        given()
                .when()
                .delete("/rooms/{id}", room.getId())
                .then()
                .statusCode(200);
    }

}
