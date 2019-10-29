package org.optaplanner.benchmark.impl;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
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

    @Test
    public void throwIllegalStateException_WhenBenchmarkingStartedTwice() {
        SolverConfig solverConfig = PlannerTestUtils.buildSolverConfig(
                TestdataSolution.class, TestdataEntity.class);
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.create(
                PlannerBenchmarkConfig.createFromSolverConfig(solverConfig));

        TestdataSolution solution = mock(TestdataSolution.class);

        DefaultPlannerBenchmark benchmark = (DefaultPlannerBenchmark) benchmarkFactory.buildPlannerBenchmark(solution);
        benchmark.benchmarkingStarted();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(benchmark::benchmarkingStarted).withMessage("This benchmark has already ran before.");
    }

    @Test
    public void throwIllegalArgumentException_WhenSolverResultBenchmarkResultListIsEmpty() {
        SolverConfigContext solverConfigContext = mock(SolverConfigContext.class);
        File benchmarkDirectory = mock(File.class);
        ExecutorService executorService = mock(ExecutorService.class);
        BenchmarkReport benchmarkReport = mock(BenchmarkReport.class);

        PlannerBenchmarkResult benchmarkResultWithEmptySolverResultList = new PlannerBenchmarkResult();

        DefaultPlannerBenchmark benchmark = new DefaultPlannerBenchmark(benchmarkResultWithEmptySolverResultList,
                                                                        solverConfigContext, benchmarkDirectory,
                                                                        executorService, executorService, benchmarkReport);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(benchmark::benchmarkingStarted)
                .withMessageStartingWith("The solverBenchmarkResultList").withMessageEndingWith("cannot be empty.");
    }

    @Test
    public void throwIllegalArgumentException_WhenBenchmarkDirectoryIsNull() {
        SolverConfigContext solverConfigContext = mock(SolverConfigContext.class);
        ExecutorService executorService = mock(ExecutorService.class);
        BenchmarkReport benchmarkReport = mock(BenchmarkReport.class);
        SolverBenchmarkResult benchMarkResult = mock(SolverBenchmarkResult.class);

        PlannerBenchmarkResult plannerBenchmarkResult = new PlannerBenchmarkResult();
        plannerBenchmarkResult.setSolverBenchmarkResultList(Collections.singletonList(benchMarkResult));

        DefaultPlannerBenchmark benchmark = new DefaultPlannerBenchmark(plannerBenchmarkResult,
                                                                        solverConfigContext, null,
                                                                        executorService, executorService, benchmarkReport);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(benchmark::benchmarkingStarted)
                .withMessageStartingWith("The benchmarkDirectory").withMessageEndingWith("must not be null.");
    }

    @Test
    public void propagateExceptionMessage_WhenExceptionThrownDuringWarmUp() {
        SolverConfig solverConfig = PlannerTestUtils.buildSolverConfig(
                TestdataSolution.class, TestdataEntity.class);
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.create(
                PlannerBenchmarkConfig.createFromSolverConfig(solverConfig));

        TestdataSolution solution = mock(TestdataSolution.class);
        String exceptionMessage = "Message to be received with ExecutionException call.";
        when(solution.getEntityList()).thenThrow(new UnsupportedOperationException(exceptionMessage));

        DefaultPlannerBenchmark benchmark = (DefaultPlannerBenchmark) benchmarkFactory.buildPlannerBenchmark(solution);

        try {
            benchmark.benchmark();
        } catch (PlannerBenchmarkException e) {
            assertEquals(exceptionMessage, e.getCause().getMessage());
        }
    }
}