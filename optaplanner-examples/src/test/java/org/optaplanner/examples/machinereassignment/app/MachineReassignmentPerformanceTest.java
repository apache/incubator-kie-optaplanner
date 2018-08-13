/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.machinereassignment.app;

import java.io.File;

import org.junit.Test;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.machinereassignment.domain.MachineReassignment;

public class MachineReassignmentPerformanceTest extends SolverPerformanceTest<MachineReassignment> {

    public MachineReassignmentPerformanceTest(String moveThreadCount) {
        super(moveThreadCount);
    }

    @Override
    protected MachineReassignmentApp createCommonApp() {
        return new MachineReassignmentApp();
    }

    // ************************************************************************
    // Tests
    // ************************************************************************

    @Test(timeout = 600000)
    public void solveModel_a2_1() {
        File unsolvedDataFile = new File("data/machinereassignment/unsolved/model_a2_1.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-117351236soft");
    }

    @Test(timeout = 600000)
    public void solveModel_a2_1FastAssert() {
        File unsolvedDataFile = new File("data/machinereassignment/unsolved/model_a2_1.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-272621414soft", EnvironmentMode.FAST_ASSERT);
    }

}
