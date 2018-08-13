/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.tennis.app;

import java.io.File;

import org.junit.Test;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.tennis.domain.TennisSolution;

public class TennisPerformanceTest extends SolverPerformanceTest<TennisSolution> {

    public TennisPerformanceTest(String moveThreadCount) {
        super(moveThreadCount);
    }

    @Override
    protected TennisApp createCommonApp() {
        return new TennisApp();
    }

    // ************************************************************************
    // Tests
    // ************************************************************************

    @Test(timeout = 600000)
    public void solveModel_munich_7teams() {
        File unsolvedDataFile = new File("data/tennis/unsolved/munich-7teams.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-27239medium/-23706soft");
    }

    @Test(timeout = 600000)
    public void solveModel_munich_7teamsFastAssert() {
        File unsolvedDataFile = new File("data/tennis/unsolved/munich-7teams.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-27239medium/-23706soft", EnvironmentMode.FAST_ASSERT);
    }

}
