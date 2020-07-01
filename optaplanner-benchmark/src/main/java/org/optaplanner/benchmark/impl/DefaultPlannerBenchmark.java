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

package org.optaplanner.benchmark.impl;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkException;
import org.optaplanner.benchmark.impl.report.BenchmarkReport;
import org.optaplanner.benchmark.impl.result.BenchmarkResultIO;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.result.ProblemBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SingleBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SubSingleBenchmarkResult;
import org.optaplanner.benchmark.impl.statistic.ProblemStatistic;
import org.optaplanner.benchmark.impl.statistic.PureSubSingleStatistic;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPlannerBenchmark implements PlannerBenchmark {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    protected final transient Logger singleBenchmarkRunnerExceptionLogger = LoggerFactory.getLogger(
            getClass().getName() + ".singleBenchmarkRunnerException");

    private final PlannerBenchmarkResult plannerBenchmarkResult;

    private final File benchmarkDirectory;
    private final ExecutorService warmUpExecutorService;
    private final ExecutorCompletionService<SubSingleBenchmarkRunner> warmUpExecutorCompletionService;
    private final ExecutorService executorService;
    private final BenchmarkResultIO benchmarkResultIO;
    private final BenchmarkReport benchmarkReport;

    private long startingSystemTimeMillis = -1L;
    private SubSingleBenchmarkRunner firstFailureSubSingleBenchmarkRunner = null;

    public DefaultPlannerBenchmark(PlannerBenchmarkResult plannerBenchmarkResult, File benchmarkDirectory,
            ExecutorService warmUpExecutorService, ExecutorService executorService, BenchmarkReport benchmarkReport) {
        this.plannerBenchmarkResult = plannerBenchmarkResult;
        this.benchmarkDirectory = benchmarkDirectory;
        this.warmUpExecutorService = warmUpExecutorService;
        warmUpExecutorCompletionService = new ExecutorCompletionService<>(warmUpExecutorService);
        this.executorService = executorService;
        this.benchmarkReport = benchmarkReport;
        benchmarkResultIO = new BenchmarkResultIO();
    }

    public PlannerBenchmarkResult getPlannerBenchmarkResult() {
        return plannerBenchmarkResult;
    }

    public File getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public BenchmarkReport getBenchmarkReport() {
        return benchmarkReport;
    }

    // ************************************************************************
    // Benchmark methods
    // ************************************************************************

    @Override
    public void benchmark() {
        benchmarkingStarted();
        warmUp();
        runSingleBenchmarks();
        benchmarkingEnded();
    }

    public void benchmarkingStarted() {
        if (startingSystemTimeMillis >= 0L) {
            throw new IllegalStateException("This benchmark has already ran before.");
        }
        startingSystemTimeMillis = System.currentTimeMillis();
        plannerBenchmarkResult.setStartingTimestamp(OffsetDateTime.now());
        List<SolverBenchmarkResult> solverBenchmarkResultList = plannerBenchmarkResult.getSolverBenchmarkResultList();
        if (ConfigUtils.isEmptyCollection(solverBenchmarkResultList)) {
            throw new IllegalArgumentException(
                    "The solverBenchmarkResultList (" + solverBenchmarkResultList + ") cannot be empty.");
        }
        initBenchmarkDirectoryAndSubdirectories();
        plannerBenchmarkResult.initSystemProperties();
        logger.info("Benchmarking started: parallelBenchmarkCount ({})"
                + " for problemCount ({}), solverCount ({}), totalSubSingleCount ({}).",
                plannerBenchmarkResult.getParallelBenchmarkCount(),
                plannerBenchmarkResult.getUnifiedProblemBenchmarkResultList().size(),
                solverBenchmarkResultList.size(),
                plannerBenchmarkResult.getTotalSubSingleCount());
    }

    private void initBenchmarkDirectoryAndSubdirectories() {
        if (benchmarkDirectory == null) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must not be null.");
        }
        // benchmarkDirectory usually already exists
        benchmarkDirectory.mkdirs();
        plannerBenchmarkResult.initBenchmarkReportDirectory(benchmarkDirectory);
    }

    private void warmUp() {
        if (plannerBenchmarkResult.getWarmUpTimeMillisSpentLimit() <= 0L) {
            return;
        }
        logger.info("================================================================================");
        logger.info("Warm up started");
        logger.info("================================================================================");
        long timeLeftTotal = plannerBenchmarkResult.getWarmUpTimeMillisSpentLimit();
        int parallelBenchmarkCount = plannerBenchmarkResult.getParallelBenchmarkCount();
        int solverBenchmarkResultCount = plannerBenchmarkResult.getSolverBenchmarkResultList().size();
        /*
         * cyclesCount needs to be long, as opposed to its natural int,
         * otherwise JDK 11 compiler uses Math.floorDiv(long, int), which is only available on JDK 9+.
         * When such compiled code is then used on Java 8, there is a NoSuchMethodError.
         * All is fine When the code is compiled on JDK 8, the compiler will use the good old Math.floorDiv(long, long).
         * TODO Remove when JDK 8 is no longer supported.
         */
        long cyclesCount = ConfigUtils.ceilDivide(solverBenchmarkResultCount, parallelBenchmarkCount);
        long timeLeftPerCycle = Math.floorDiv(timeLeftTotal, cyclesCount);
        Map<ProblemBenchmarkResult, List<ProblemStatistic>> originalProblemStatisticMap = new HashMap<>(
                plannerBenchmarkResult.getUnifiedProblemBenchmarkResultList().size());
        ConcurrentMap<SolverBenchmarkResult, Integer> singleBenchmarkResultIndexMap = new ConcurrentHashMap<>(
                solverBenchmarkResultCount);

        Map<SolverBenchmarkResult, WarmUpConfigBackup> warmUpConfigBackupMap = WarmUpConfigBackup
                .backupBenchmarkConfig(plannerBenchmarkResult, originalProblemStatisticMap);
        SolverBenchmarkResult[] solverBenchmarkResultCycle = new SolverBenchmarkResult[parallelBenchmarkCount];
        int solverBenchmarkResultIndex = 0;
        for (int i = 0; i < cyclesCount; i++) {
            long timeCycleEnd = System.currentTimeMillis() + timeLeftPerCycle;
            for (int j = 0; j < parallelBenchmarkCount; j++) {
                solverBenchmarkResultCycle[j] = plannerBenchmarkResult.getSolverBenchmarkResultList()
                        .get(solverBenchmarkResultIndex % solverBenchmarkResultCount);
                solverBenchmarkResultIndex++;
            }
            ConcurrentMap<Future<SubSingleBenchmarkRunner>, SubSingleBenchmarkRunner> futureMap = new ConcurrentHashMap<>(
                    parallelBenchmarkCount);
            warmUpPopulate(futureMap, singleBenchmarkResultIndexMap, solverBenchmarkResultCycle, timeLeftPerCycle);
            warmUp(futureMap, singleBenchmarkResultIndexMap, timeCycleEnd);
        }
        WarmUpConfigBackup.restoreBenchmarkConfig(plannerBenchmarkResult, originalProblemStatisticMap, warmUpConfigBackupMap);
        List<Runnable> notFinishedWarmUpList = warmUpExecutorService.shutdownNow();
        if (!notFinishedWarmUpList.isEmpty()) {
            throw new IllegalStateException("Impossible state: notFinishedWarmUpList (" + notFinishedWarmUpList
                    + ") is not empty.");
        }
        logger.info("================================================================================");
        logger.info("Warm up ended");
        logger.info("================================================================================");
    }

    private void warmUpPopulate(Map<Future<SubSingleBenchmarkRunner>, SubSingleBenchmarkRunner> futureMap,
            ConcurrentMap<SolverBenchmarkResult, Integer> singleBenchmarkResultIndexMap,
            SolverBenchmarkResult[] solverBenchmarkResultArray, long timeLeftPerSolverConfig) {
        for (SolverBenchmarkResult solverBenchmarkResult : solverBenchmarkResultArray) {
            TerminationConfig originalTerminationConfig = solverBenchmarkResult.getSolverConfig().getTerminationConfig();
            TerminationConfig tmpTerminationConfig = new TerminationConfig();
            if (originalTerminationConfig != null) {
                tmpTerminationConfig.inherit(originalTerminationConfig);
            }
            tmpTerminationConfig.shortenTimeMillisSpentLimit(timeLeftPerSolverConfig);
            solverBenchmarkResult.getSolverConfig().setTerminationConfig(tmpTerminationConfig);

            Integer singleBenchmarkResultIndex = singleBenchmarkResultIndexMap.get(solverBenchmarkResult);
            singleBenchmarkResultIndex = (singleBenchmarkResultIndex == null) ? 0
                    : singleBenchmarkResultIndex % solverBenchmarkResult.getSingleBenchmarkResultList().size();
            SingleBenchmarkResult singleBenchmarkResult = solverBenchmarkResult.getSingleBenchmarkResultList()
                    .get(singleBenchmarkResultIndex);
            // Just take the first subSingle, we don't need to warm up each one
            SubSingleBenchmarkRunner subSingleBenchmarkRunner = new SubSingleBenchmarkRunner(
                    singleBenchmarkResult.getSubSingleBenchmarkResultList().get(0), true);
            Future<SubSingleBenchmarkRunner> future = warmUpExecutorCompletionService.submit(subSingleBenchmarkRunner);
            futureMap.put(future, subSingleBenchmarkRunner);
            singleBenchmarkResultIndexMap.put(solverBenchmarkResult, singleBenchmarkResultIndex + 1);
        }
    }

    private void warmUp(Map<Future<SubSingleBenchmarkRunner>, SubSingleBenchmarkRunner> futureMap,
            ConcurrentMap<SolverBenchmarkResult, Integer> singleBenchmarkResultIndexMap, long timePhaseEnd) {
        // Wait for the warm up benchmarks to complete
        int tasksCount = futureMap.size();
        // Use a counter because completion order of futures is different from input order
        for (int i = 0; i < tasksCount; i++) {
            Future<SubSingleBenchmarkRunner> future;
            try {
                future = warmUpExecutorCompletionService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Waiting for a warm up singleBenchmarkRunner was interrupted.", e);
            }

            Throwable failureThrowable = null;
            SubSingleBenchmarkRunner subSingleBenchmarkRunner;
            try {
                // Explicitly returning it in the Callable guarantees memory visibility
                subSingleBenchmarkRunner = future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                subSingleBenchmarkRunner = futureMap.get(future);
                singleBenchmarkRunnerExceptionLogger.error("The warm up singleBenchmarkRunner ({}) was interrupted.",
                        subSingleBenchmarkRunner, e);
                failureThrowable = e;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                subSingleBenchmarkRunner = futureMap.get(future);
                singleBenchmarkRunnerExceptionLogger.warn("The warm up singleBenchmarkRunner ({}) failed.",
                        subSingleBenchmarkRunner, cause);
                failureThrowable = cause;
            }
            if (failureThrowable != null) {
                subSingleBenchmarkRunner.setFailureThrowable(failureThrowable);
                if (firstFailureSubSingleBenchmarkRunner == null) {
                    firstFailureSubSingleBenchmarkRunner = subSingleBenchmarkRunner;
                }
            }

            SolverBenchmarkResult solverBenchmarkResult = subSingleBenchmarkRunner.getSubSingleBenchmarkResult()
                    .getSingleBenchmarkResult().getSolverBenchmarkResult();
            long timeLeftInCycle = timePhaseEnd - System.currentTimeMillis();
            if (timeLeftInCycle > 0L) {
                SolverBenchmarkResult[] solverBenchmarkResultSingleton = new SolverBenchmarkResult[] { solverBenchmarkResult };
                warmUpPopulate(futureMap, singleBenchmarkResultIndexMap, solverBenchmarkResultSingleton, timeLeftInCycle);
                tasksCount++;
            }
        }
    }

    protected void runSingleBenchmarks() {
        Map<SubSingleBenchmarkRunner, Future<SubSingleBenchmarkRunner>> futureMap = new HashMap<>();
        for (ProblemBenchmarkResult<Object> problemBenchmarkResult : plannerBenchmarkResult
                .getUnifiedProblemBenchmarkResultList()) {
            for (SingleBenchmarkResult singleBenchmarkResult : problemBenchmarkResult.getSingleBenchmarkResultList()) {
                for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult
                        .getSubSingleBenchmarkResultList()) {
                    SubSingleBenchmarkRunner subSingleBenchmarkRunner = new SubSingleBenchmarkRunner(
                            subSingleBenchmarkResult, false);
                    Future<SubSingleBenchmarkRunner> future = executorService.submit(subSingleBenchmarkRunner);
                    futureMap.put(subSingleBenchmarkRunner, future);
                }
            }
        }
        // Wait for the benchmarks to complete
        for (Map.Entry<SubSingleBenchmarkRunner, Future<SubSingleBenchmarkRunner>> futureEntry : futureMap.entrySet()) {
            SubSingleBenchmarkRunner subSingleBenchmarkRunner = futureEntry.getKey();
            Future<SubSingleBenchmarkRunner> future = futureEntry.getValue();
            Throwable failureThrowable = null;
            try {
                // Explicitly returning it in the Callable guarantees memory visibility
                subSingleBenchmarkRunner = future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                singleBenchmarkRunnerExceptionLogger.error("The subSingleBenchmarkRunner ({}) was interrupted.",
                        subSingleBenchmarkRunner, e);
                failureThrowable = e;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                singleBenchmarkRunnerExceptionLogger.warn("The subSingleBenchmarkRunner ({}) failed.",
                        subSingleBenchmarkRunner, cause);
                failureThrowable = cause;
            }
            if (failureThrowable == null) {
                subSingleBenchmarkRunner.getSubSingleBenchmarkResult().setSucceeded(true);
            } else {
                subSingleBenchmarkRunner.getSubSingleBenchmarkResult().setSucceeded(false);
                subSingleBenchmarkRunner.setFailureThrowable(failureThrowable);
                if (firstFailureSubSingleBenchmarkRunner == null) {
                    firstFailureSubSingleBenchmarkRunner = subSingleBenchmarkRunner;
                }
            }
        }
    }

    public void benchmarkingEnded() {
        List<Runnable> notExecutedBenchmarkList = executorService.shutdownNow();
        if (!notExecutedBenchmarkList.isEmpty()) {
            throw new IllegalStateException("Impossible state: notExecutedBenchmarkList size ("
                    + notExecutedBenchmarkList + ").");
        }
        plannerBenchmarkResult.setBenchmarkTimeMillisSpent(calculateTimeMillisSpent());
        benchmarkResultIO.writePlannerBenchmarkResult(plannerBenchmarkResult.getBenchmarkReportDirectory(),
                plannerBenchmarkResult);
        benchmarkReport.writeReport();
        if (plannerBenchmarkResult.getFailureCount() == 0) {
            logger.info("Benchmarking ended: time spent ({}), favoriteSolverBenchmark ({}), statistic html overview ({}).",
                    plannerBenchmarkResult.getBenchmarkTimeMillisSpent(),
                    plannerBenchmarkResult.getFavoriteSolverBenchmarkResult().getName(),
                    benchmarkReport.getHtmlOverviewFile().getAbsolutePath());
        } else {
            logger.info("Benchmarking failed: time spent ({}), failureCount ({}), statistic html overview ({}).",
                    plannerBenchmarkResult.getBenchmarkTimeMillisSpent(),
                    plannerBenchmarkResult.getFailureCount(),
                    benchmarkReport.getHtmlOverviewFile().getAbsolutePath());
            throw new PlannerBenchmarkException("Benchmarking failed: failureCount ("
                    + plannerBenchmarkResult.getFailureCount() + ")." +
                    " The exception of the firstFailureSingleBenchmarkRunner ("
                    + firstFailureSubSingleBenchmarkRunner.getName() + ") is chained.",
                    firstFailureSubSingleBenchmarkRunner.getFailureThrowable());
        }
    }

    public long calculateTimeMillisSpent() {
        long now = System.currentTimeMillis();
        return now - startingSystemTimeMillis;
    }

    private static final class WarmUpConfigBackup {

        private final TerminationConfig terminationConfig;
        private final Map<SubSingleBenchmarkResult, List<PureSubSingleStatistic>> pureSubSingleStatisticMap;

        public WarmUpConfigBackup(TerminationConfig terminationConfig) {
            this.terminationConfig = terminationConfig;
            this.pureSubSingleStatisticMap = new HashMap<>();
        }

        public Map<SubSingleBenchmarkResult, List<PureSubSingleStatistic>> getPureSubSingleStatisticMap() {
            return pureSubSingleStatisticMap;
        }

        public TerminationConfig getTerminationConfig() {
            return terminationConfig;
        }

        private static void restoreBenchmarkConfig(PlannerBenchmarkResult plannerBenchmarkResult,
                Map<ProblemBenchmarkResult, List<ProblemStatistic>> originalProblemStatisticMap,
                Map<SolverBenchmarkResult, WarmUpConfigBackup> warmUpConfigBackupMap) {
            for (SolverBenchmarkResult solverBenchmarkResult : plannerBenchmarkResult.getSolverBenchmarkResultList()) {
                WarmUpConfigBackup warmUpConfigBackup = warmUpConfigBackupMap.get(solverBenchmarkResult);
                TerminationConfig originalTerminationConfig = warmUpConfigBackup.getTerminationConfig();
                solverBenchmarkResult.getSolverConfig().setTerminationConfig(originalTerminationConfig);
                for (SingleBenchmarkResult singleBenchmarkResult : solverBenchmarkResult.getSingleBenchmarkResultList()) {
                    ProblemBenchmarkResult problemBenchmarkResult = singleBenchmarkResult.getProblemBenchmarkResult();
                    if (problemBenchmarkResult.getProblemStatisticList() == null
                            || problemBenchmarkResult.getProblemStatisticList().size() <= 0) {
                        problemBenchmarkResult.setProblemStatisticList(originalProblemStatisticMap.get(problemBenchmarkResult));
                    }
                    for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult
                            .getSubSingleBenchmarkResultList()) {
                        List<PureSubSingleStatistic> pureSubSingleStatisticList = warmUpConfigBackup
                                .getPureSubSingleStatisticMap().get(subSingleBenchmarkResult);
                        subSingleBenchmarkResult.setPureSubSingleStatisticList(pureSubSingleStatisticList);
                        subSingleBenchmarkResult.initSubSingleStatisticMap();
                    }
                    singleBenchmarkResult.initSubSingleStatisticMaps();
                }
            }
        }

        private static Map<SolverBenchmarkResult, WarmUpConfigBackup> backupBenchmarkConfig(
                PlannerBenchmarkResult plannerBenchmarkResult,
                Map<ProblemBenchmarkResult, List<ProblemStatistic>> originalProblemStatisticMap) { // backup & remove stats, backup termination config
            Map<SolverBenchmarkResult, WarmUpConfigBackup> warmUpConfigBackupMap = new HashMap<>(
                    plannerBenchmarkResult.getSolverBenchmarkResultList().size());
            for (SolverBenchmarkResult solverBenchmarkResult : plannerBenchmarkResult.getSolverBenchmarkResultList()) {
                TerminationConfig originalTerminationConfig = solverBenchmarkResult.getSolverConfig().getTerminationConfig();
                WarmUpConfigBackup warmUpConfigBackup = new WarmUpConfigBackup(originalTerminationConfig);
                for (SingleBenchmarkResult singleBenchmarkResult : solverBenchmarkResult.getSingleBenchmarkResultList()) {
                    for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult
                            .getSubSingleBenchmarkResultList()) {
                        List<PureSubSingleStatistic> originalPureSubSingleStatisticList = subSingleBenchmarkResult
                                .getPureSubSingleStatisticList();
                        List<PureSubSingleStatistic> subSingleBenchmarkStatisticPutResult = warmUpConfigBackup
                                .getPureSubSingleStatisticMap()
                                .put(subSingleBenchmarkResult, originalPureSubSingleStatisticList);
                        if (subSingleBenchmarkStatisticPutResult != null) {
                            throw new IllegalStateException(
                                    "SubSingleBenchmarkStatisticMap of WarmUpConfigBackup (" + warmUpConfigBackup
                                            + ") already contained key (" + subSingleBenchmarkResult + ") with value ("
                                            + subSingleBenchmarkStatisticPutResult + ").");
                        }
                    }
                    ProblemBenchmarkResult problemBenchmarkResult = singleBenchmarkResult.getProblemBenchmarkResult();
                    originalProblemStatisticMap.putIfAbsent(problemBenchmarkResult,
                            problemBenchmarkResult.getProblemStatisticList());
                    singleBenchmarkResult.getProblemBenchmarkResult().setProblemStatisticList(Collections.emptyList());
                    for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult
                            .getSubSingleBenchmarkResultList()) { // needs to happen after all problem stats
                        subSingleBenchmarkResult.setPureSubSingleStatisticList(Collections.emptyList());
                        subSingleBenchmarkResult.initSubSingleStatisticMap();
                    }
                }
                WarmUpConfigBackup warmUpConfigBackupPutResult = warmUpConfigBackupMap.put(solverBenchmarkResult,
                        warmUpConfigBackup);
                if (warmUpConfigBackupPutResult != null) {
                    throw new IllegalStateException("WarmUpConfigBackupMap already contained key (" + solverBenchmarkResult
                            + ") with value (" + warmUpConfigBackupPutResult + ").");
                }
            }
            return warmUpConfigBackupMap;
        }
    }

    @Override
    public void benchmarkAndShowReportInBrowser() {
        benchmark();
        showReportInBrowser();
    }

    private void showReportInBrowser() {
        File htmlOverviewFile = benchmarkReport.getHtmlOverviewFile();
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
            logger.warn("The default browser can't be opened to show htmlOverviewFile ({}).", htmlOverviewFile);
            return;
        }
        try {
            desktop.browse(htmlOverviewFile.getAbsoluteFile().toURI());
        } catch (IOException e) {
            throw new IllegalStateException("Failed showing htmlOverviewFile (" + htmlOverviewFile
                    + ") in the default browser.", e);
        }
    }

}
