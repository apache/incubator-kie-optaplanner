package org.optaplanner.benchmark.impl;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.optaplanner.benchmark.api.*;
import org.optaplanner.benchmark.config.PlannerBenchmarkConfig;
import org.optaplanner.benchmark.impl.report.BenchmarkReport;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.core.config.SolverConfigContext;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

public class DefaultPlannerBenchmarkTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void throwIllegalStateExceptionWhenBenchmarkingStartedTwice() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("This benchmark has already ran before.");

        SolverConfig solverConfig = PlannerTestUtils.buildSolverConfig(
                TestdataSolution.class, TestdataEntity.class);
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.create(
                PlannerBenchmarkConfig.createFromSolverConfig(solverConfig));

        TestdataSolution solution = mock(TestdataSolution.class);

        DefaultPlannerBenchmark benchmark = (DefaultPlannerBenchmark) benchmarkFactory.buildPlannerBenchmark(solution);
        benchmark.benchmarkingStarted();
        benchmark.benchmarkingStarted();
    }

    @Test
    public void throwIllegalArgumentExceptionWhenSolverResultBenchmarkResultListIsEmpty() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The solverBenchmarkResultList");
        exceptionRule.expectMessage("cannot be empty.");

        SolverConfigContext solverConfigContext = mock(SolverConfigContext.class);
        File benchmarkDirectory = mock(File.class);
        ExecutorService executorService = mock(ExecutorService.class);
        BenchmarkReport benchmarkReport = mock(BenchmarkReport.class);

        PlannerBenchmarkResult benchmarkResultWithEmptySolverResultList = new PlannerBenchmarkResult();

        DefaultPlannerBenchmark benchmark = new DefaultPlannerBenchmark(benchmarkResultWithEmptySolverResultList,
                                                                        solverConfigContext, benchmarkDirectory,
                                                                        executorService, executorService, benchmarkReport);
        benchmark.benchmarkingStarted();
    }

    @Test
    public void throwIllegalArgumentExceptionWhenBenchmarkDirectoryIsNull() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The benchmarkDirectory");
        exceptionRule.expectMessage("must not be null.");

        SolverConfigContext solverConfigContext = mock(SolverConfigContext.class);
        ExecutorService executorService = mock(ExecutorService.class);
        BenchmarkReport benchmarkReport = mock(BenchmarkReport.class);
        SolverBenchmarkResult benchMarkResult = mock(SolverBenchmarkResult.class);

        PlannerBenchmarkResult plannerBenchmarkResult = new PlannerBenchmarkResult();
        plannerBenchmarkResult.setSolverBenchmarkResultList(Collections.singletonList(benchMarkResult));

        DefaultPlannerBenchmark benchmark = new DefaultPlannerBenchmark(plannerBenchmarkResult,
                                                                        solverConfigContext, null,
                                                                        executorService, executorService, benchmarkReport);
        benchmark.benchmarkingStarted();
    }
}