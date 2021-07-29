/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.batchscheduling.app;

import java.util.stream.Stream;

import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.batchscheduling.domain.BatchSchedule;
import org.optaplanner.examples.common.app.SolverPerformanceTest;

public class BatchSchedulingPerformanceTest extends SolverPerformanceTest<BatchSchedule> {

    private static final String UNSOLVED_DATA_FILE = "data/batchscheduling/unsolved/Data50_25.xml";

    @Override
    protected BatchSchedulingApp createCommonApp() {
        return new BatchSchedulingApp();
    }

    @Override
    protected Stream<TestData> testData() {
        return Stream.of(
                testData(UNSOLVED_DATA_FILE, "0hard/-902335925205947medium/-20672soft", EnvironmentMode.REPRODUCIBLE),
                testData(UNSOLVED_DATA_FILE, "0hard/-918680355373904medium/-22228soft", EnvironmentMode.FAST_ASSERT));
    }
}
