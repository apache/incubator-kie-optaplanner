/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.quarkus.example.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.optaplanner.quarkus.example.domain.Detective;
import org.optaplanner.quarkus.example.domain.Investigation;
import org.optaplanner.quarkus.example.domain.PoliceSolution;

@Path("/police")
public class PoliceResource {


//    SolverManager<PoliceSolution> solverManager;

    // To trigger this, open http://localhost:8080/police/solve?investigationListSize=5
    @GET
    @Path("/solve")
    @Produces(MediaType.APPLICATION_JSON)
    public UUID solve(@QueryParam("investigationListSize") int investigationListSize) {
        PoliceSolution problem = generateProblem(investigationListSize);
        return UUID.randomUUID();
//        return solverManager.solve(problem);
    }

    private PoliceSolution generateProblem(int investigationListSize) {
        Random random = new Random(37);
        int detectiveListSize = (investigationListSize + 3) / 4;
        List<Detective> detectiveList = new ArrayList<>(detectiveListSize);
        for (int i = 0; i < detectiveListSize; i++) {
            // A quarter of all detective work part-time
            Duration workDuration = (i % 4 != 3) ? Duration.ofHours(40) : Duration.ofHours(20);
            detectiveList.add(new Detective("Detective " + i, workDuration));
        }
        List<Investigation> investigationList = new ArrayList<>(investigationListSize);
        for (int i = 0; i < investigationListSize; i++) {
            // On average, there is a capacity of 8.75 hours per investigation
            // Random distribution of on average 7 hours for the estimated duration per investigation
            Duration estimatedDuration = Duration.ofHours(1 + random.nextInt(12));
            investigationList.add(new Investigation(i, estimatedDuration));
        }
        return new PoliceSolution(detectiveList, investigationList);
    }

}
