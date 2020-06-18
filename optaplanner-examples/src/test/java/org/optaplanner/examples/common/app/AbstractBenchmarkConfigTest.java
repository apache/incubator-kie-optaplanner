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

package org.optaplanner.examples.common.app;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.benchmark.impl.DefaultPlannerBenchmark;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.core.config.solver.SolverConfig;

public abstract class AbstractBenchmarkConfigTest {

    protected abstract CommonBenchmarkApp getBenchmarkApp();

    @TestFactory
    Stream<DynamicTest> testBenchmarkApp() {
        return getBenchmarkApp().getArgOptions().stream()
                .map(argOption -> dynamicTest(argOption.toString(), () -> buildPlannerBenchmark(argOption)));
    }

    private static void buildPlannerBenchmark(CommonBenchmarkApp.ArgOption argOption) {
        String benchmarkConfigResource = argOption.getBenchmarkConfigResource();
        PlannerBenchmarkFactory benchmarkFactory;
        if (!argOption.isTemplate()) {
            benchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(benchmarkConfigResource);
        } else {
            benchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(benchmarkConfigResource);
        }
        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark();
        buildEverySolver(benchmark);
    }

    private static void buildEverySolver(PlannerBenchmark plannerBenchmark) {
        PlannerBenchmarkResult plannerBenchmarkResult = ((DefaultPlannerBenchmark) plannerBenchmark)
                .getPlannerBenchmarkResult();
        for (SolverBenchmarkResult solverBenchmarkResult : plannerBenchmarkResult.getSolverBenchmarkResultList()) {
            SolverConfig solverConfig = solverBenchmarkResult.getSolverConfig();
            solverConfig.buildSolver();
        }
    }
}
