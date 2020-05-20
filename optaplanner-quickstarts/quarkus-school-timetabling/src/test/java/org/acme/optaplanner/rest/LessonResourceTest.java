package org.acme.optaplanner.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.acme.optaplanner.domain.Lesson;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class LessonResourceTest {

    @Test
    public void getAll() {
        List<Lesson> lessonList = given()
                .when().get("/lessons")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", Lesson.class);
        assertFalse(lessonList.isEmpty());
        Lesson firstLesson = lessonList.get(0);
        assertEquals("Biology", firstLesson.getSubject());
        assertEquals("C. Darwin", firstLesson.getTeacher());
        assertEquals("9th grade", firstLesson.getStudentGroup());
    }

    @Test
    void addAndRemove() {
        Lesson lesson = given()
                .when()
                .contentType(ContentType.JSON)
                .body(new Lesson("Test subject", "Test teacher", "test studentGroup"))
                .post("/lessons")
                .then()
                .statusCode(202)
                .extract().as(Lesson.class);

        given()
                .when()
                .delete("/lessons/{id}", lesson.getId())
                .then()
                .statusCode(200);
    }

}
