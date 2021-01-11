/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.quarkus.it.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;
import org.optaplanner.quarkus.it.domain.ITestdataPlanningEntity;
import org.optaplanner.quarkus.it.domain.ITestdataPlanningSolution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class SolverResourceTest {

    private static final long PROBLEM_ID = 1L;
    private static final String API_ROOT = "/iTestdataPlanningSolution";

    @Test
    @Timeout(600_000)
    public void solveTestData() throws Exception {
        ITestdataPlanningEntity e1 = new ITestdataPlanningEntity();
        ITestdataPlanningEntity e2 = new ITestdataPlanningEntity();
        ITestdataPlanningEntity e3 = new ITestdataPlanningEntity();
        ITestdataPlanningSolution inputProblem = new ITestdataPlanningSolution();
        inputProblem.setEntityList(Arrays.asList(e1, e2, e3));
        inputProblem.setValueList(Arrays.asList("v1", "v2", "v3"));

        assertThat(getSolverStatus()).isEqualTo(SolverStatus.NOT_SOLVING);
        solve(inputProblem);
        while (getSolverStatus() != SolverStatus.NOT_SOLVING) {
            Thread.sleep(100L);
        }

        ITestdataPlanningSolution solution = getSolution();

        assertThat(solution.getEntityList()).hasSize(3);
        assertThat(solution.getEntityList().get(0).getValue()).isEqualTo("v1");
        assertThat(solution.getEntityList().get(1).getValue()).isEqualTo("v2");
        assertThat(solution.getEntityList().get(2).getValue()).isEqualTo("v3");
        assertThat(solution.getScore()).isEqualTo(SimpleScore.of(0));
    }

    private SolverStatus getSolverStatus() {
        Response response = RestAssured
                .get(API_ROOT + "/status/" + PROBLEM_ID);
        response.then().assertThat().statusCode(200);
        return response.getBody().as(SolverStatus.class);
    }

    private void solve(ITestdataPlanningSolution inputPlanningProblem) {
        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(inputPlanningProblem)
                .post(API_ROOT + "/solve/" + PROBLEM_ID)
                .then()
                .assertThat()
                .statusCode(204);
    }

    private ITestdataPlanningSolution getSolution() throws JsonProcessingException {
        String solutionJson = RestAssured
                .get(API_ROOT + "/" + PROBLEM_ID)
                .getBody().asString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(OptaPlannerJacksonModule.createModule());
        return mapper.readValue(solutionJson, ITestdataPlanningSolution.class);
    }
}
