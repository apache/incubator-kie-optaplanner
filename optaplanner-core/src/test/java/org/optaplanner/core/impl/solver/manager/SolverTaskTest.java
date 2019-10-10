/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.solver.manager;

import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

import static org.junit.Assert.assertNotNull;

public class SolverTaskTest {

    public static final String SOLVER_CONFIG = "org/optaplanner/core/impl/solver/testdataSolverConfigXStream.xml";

    @Test
    public void getBestScoreDoesntReturnNullIfSolutionUninitialized() throws InterruptedException {
        TestdataSolution solution = new TestdataSolution("s1");
        solution.setCode("0");
        solution.setValueList(Arrays.asList(new TestdataValue("v1"), new TestdataValue("v2")));
        solution.setEntityList(Arrays.asList(new TestdataEntity("e1"), new TestdataEntity("e2")));

        SolverFactory<TestdataSolution> solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG);
        Solver<TestdataSolution> solver = solverFactory.buildSolver();
        SolverTask<TestdataSolution> solverTask = new SolverTask<>(0L, solver, solution);

        assertNotNull(solverTask.getBestScore());
    }
}
