/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.facilitylocation.solver;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.acme.facilitylocation.bootstrap.DemoDataBuilder;
import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.solver.SolverManager;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SolverManagerTest {

    @Inject
    SolverManager<FacilityLocationProblem, Long> solverManager;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        FacilityLocationProblem problem = DemoDataBuilder.builder()
                .setCapacity(1200)
                .setDemand(900)
                .setAverageSetupCost(1000).setSetupCostStandardDeviation(200)
                .setFacilityCount(10)
                .setConsumerCount(150)
                .setSouthWestCorner(new Location(-10, -10))
                .setNorthEastCorner(new Location(10, 10))
                .build();
        solverManager.solve(0L, id -> problem, SolverManagerTest::printSolution).getFinalBestSolution();
    }

    static void printSolution(FacilityLocationProblem solution) {
        solution.getFacilities().forEach(facility -> System.out.printf("$%4d (%3d/%3d)%n",
                facility.getSetupCost(),
                facility.getUsedCapacity(),
                facility.getCapacity()));
    }
}
