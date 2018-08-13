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

package org.optaplanner.examples.curriculumcourse.app;

import java.io.File;

import org.junit.Test;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.curriculumcourse.domain.CourseSchedule;

public class CurriculumCoursePerformanceTest extends SolverPerformanceTest<CourseSchedule> {

    public CurriculumCoursePerformanceTest(String moveThreadCount) {
        super(moveThreadCount);
    }

    @Override
    protected CurriculumCourseApp createCommonApp() {
        return new CurriculumCourseApp();
    }

    // ************************************************************************
    // Tests
    // ************************************************************************

    @Test(timeout = 600000)
    public void solveComp01_initialized() {
        File unsolvedDataFile = new File("data/curriculumcourse/unsolved/comp01_initialized.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-99soft");
    }

    @Test(timeout = 600000)
    public void solveComp01_initializedFastAssert() {
        File unsolvedDataFile = new File("data/curriculumcourse/unsolved/comp01_initialized.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-140soft", EnvironmentMode.FAST_ASSERT);
    }

}
