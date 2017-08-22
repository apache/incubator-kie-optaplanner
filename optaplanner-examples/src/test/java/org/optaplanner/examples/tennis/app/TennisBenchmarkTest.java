/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.examples.common.app.LoggingTest;
import org.optaplanner.examples.tennis.domain.TennisSolution;
import org.optaplanner.examples.tennis.persistence.TennisGenerator;

public class TennisBenchmarkTest extends LoggingTest {

    @Test
    public void benchmark() {
        SolverFactory<TennisSolution> solverFactory = SolverFactory.createFromXmlResource(TennisApp.SOLVER_CONFIG);
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromSolverFactory(
                solverFactory, new File("target/test/data/tennis"));

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setScoreCalculationCountLimit(1000L);
        benchmarkFactory.getPlannerBenchmarkConfig().getInheritedSolverBenchmarkConfig().getSolverConfig().setTerminationConfig(terminationConfig);

        TennisSolution problem = new TennisGenerator().createTennisSolution();
        PlannerBenchmark plannerBenchmark = benchmarkFactory.buildPlannerBenchmark(problem);
        plannerBenchmark.benchmark();

    }

}
