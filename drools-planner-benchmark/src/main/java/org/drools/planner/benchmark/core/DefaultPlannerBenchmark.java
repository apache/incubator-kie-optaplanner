/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.benchmark.core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.drools.planner.benchmark.api.ranking.SolverBenchmarkRankingWeightFactory;
import org.drools.planner.benchmark.api.PlannerBenchmark;
import org.drools.planner.benchmark.core.statistic.BenchmarkReport;
import org.drools.planner.core.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the benchmarks on multiple {@link Solver} configurations on multiple problem instances (data sets).
 */
public class DefaultPlannerBenchmark implements PlannerBenchmark {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private File benchmarkDirectory = null;
    private File benchmarkReportDirectory = null;
    private Comparator<SolverBenchmark> solverBenchmarkRankingComparator = null;
    private SolverBenchmarkRankingWeightFactory solverBenchmarkRankingWeightFactory = null;

    private int parallelBenchmarkCount = -1;
    private long warmUpTimeMillisSpend = 0L;

    private List<SolverBenchmark> solverBenchmarkList = null;
    private List<ProblemBenchmark> unifiedProblemBenchmarkList = null;
    private final BenchmarkReport benchmarkReport = new BenchmarkReport(this);

    private long startingSystemTimeMillis;
    private Date startingTimestamp;
    private ExecutorService executorService;
    private Integer failureCount;
    private SingleBenchmark firstFailureSingleBenchmark;

    private Long averageProblemScale = null;
    private SolverBenchmark favoriteSolverBenchmark;
    private long benchmarkTimeMillisSpend;

    public File getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public void setBenchmarkDirectory(File benchmarkDirectory) {
        this.benchmarkDirectory = benchmarkDirectory;
    }

    public File getBenchmarkReportDirectory() {
        return benchmarkReportDirectory;
    }

    public Comparator<SolverBenchmark> getSolverBenchmarkRankingComparator() {
        return solverBenchmarkRankingComparator;
    }

    public void setSolverBenchmarkRankingComparator(Comparator<SolverBenchmark> solverBenchmarkRankingComparator) {
        this.solverBenchmarkRankingComparator = solverBenchmarkRankingComparator;
    }

    public SolverBenchmarkRankingWeightFactory getSolverBenchmarkRankingWeightFactory() {
        return solverBenchmarkRankingWeightFactory;
    }

    public void setSolverBenchmarkRankingWeightFactory(SolverBenchmarkRankingWeightFactory solverBenchmarkRankingWeightFactory) {
        this.solverBenchmarkRankingWeightFactory = solverBenchmarkRankingWeightFactory;
    }

    public int getParallelBenchmarkCount() {
        return parallelBenchmarkCount;
    }

    public void setParallelBenchmarkCount(int parallelBenchmarkCount) {
        this.parallelBenchmarkCount = parallelBenchmarkCount;
    }

    public long getWarmUpTimeMillisSpend() {
        return warmUpTimeMillisSpend;
    }

    public void setWarmUpTimeMillisSpend(long warmUpTimeMillisSpend) {
        this.warmUpTimeMillisSpend = warmUpTimeMillisSpend;
    }

    public List<SolverBenchmark> getSolverBenchmarkList() {
        return solverBenchmarkList;
    }

    public void setSolverBenchmarkList(List<SolverBenchmark> solverBenchmarkList) {
        this.solverBenchmarkList = solverBenchmarkList;
    }

    public List<ProblemBenchmark> getUnifiedProblemBenchmarkList() {
        return unifiedProblemBenchmarkList;
    }

    public void setUnifiedProblemBenchmarkList(List<ProblemBenchmark> unifiedProblemBenchmarkList) {
        this.unifiedProblemBenchmarkList = unifiedProblemBenchmarkList;
    }

    public Date getStartingTimestamp() {
        return startingTimestamp;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public Long getAverageProblemScale() {
        return averageProblemScale;
    }

    public long getBenchmarkTimeMillisSpend() {
        return benchmarkTimeMillisSpend;
    }

    public BenchmarkReport getBenchmarkReport() {
        return benchmarkReport;
    }

    // ************************************************************************
    // Benchmark methods
    // ************************************************************************

    public boolean hasMultipleParallelBenchmarks() {
        return parallelBenchmarkCount > 1;
    }

    public void benchmark() {
        benchmarkingStarted();
        warmUp();
        runSingleBenchmarks();
        benchmarkingEnded();
    }

    public void benchmarkingStarted() {
        startingSystemTimeMillis = System.currentTimeMillis();
        startingTimestamp = new Date();
        if (solverBenchmarkList == null || solverBenchmarkList.isEmpty()) {
            throw new IllegalArgumentException(
                    "The solverBenchmarkList (" + solverBenchmarkList + ") cannot be empty.");
        }
        initBenchmarkDirectoryAndSubdirs();
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            solverBenchmark.benchmarkingStarted();
        }
        for (ProblemBenchmark problemBenchmark : unifiedProblemBenchmarkList) {
            problemBenchmark.benchmarkingStarted();
        }
        executorService = Executors.newFixedThreadPool(parallelBenchmarkCount);
        failureCount = 0;
        firstFailureSingleBenchmark = null;
        averageProblemScale = null;
        favoriteSolverBenchmark = null;
        benchmarkTimeMillisSpend = -1L;
        logger.info("Benchmarking started: solverBenchmarkList size ({}), parallelBenchmarkCount ({}).",
                solverBenchmarkList.size(), parallelBenchmarkCount);
    }

    private void initBenchmarkDirectoryAndSubdirs() {
        if (benchmarkDirectory == null) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must not be null.");
        }
        benchmarkDirectory.mkdirs();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(startingTimestamp);
        benchmarkReportDirectory = new File(benchmarkDirectory, timestamp);
        benchmarkReportDirectory.mkdirs();
    }

    private void warmUp() {
        if (warmUpTimeMillisSpend > 0L) {
            logger.info("================================================================================");
            logger.info("Warming up");
            logger.info("================================================================================");
            long startingTimeMillis = System.currentTimeMillis();
            long timeLeft = warmUpTimeMillisSpend;
            Iterator<ProblemBenchmark> it = unifiedProblemBenchmarkList.iterator();
            while (timeLeft > 0L) {
                if (!it.hasNext()) {
                    it = unifiedProblemBenchmarkList.iterator();
                }
                ProblemBenchmark problemBenchmark = it.next();
                timeLeft = problemBenchmark.warmUp(startingTimeMillis, warmUpTimeMillisSpend, timeLeft);
            }
            logger.info("================================================================================");
            logger.info("Finished warmUp");
            logger.info("================================================================================");
        }
    }

    protected void runSingleBenchmarks() {
        Map<SingleBenchmark, Future<SingleBenchmark>> futureMap
                = new HashMap<SingleBenchmark, Future<SingleBenchmark>>();
        for (ProblemBenchmark problemBenchmark : unifiedProblemBenchmarkList) {
            for (SingleBenchmark singleBenchmark : problemBenchmark.getSingleBenchmarkList()) {
                Future<SingleBenchmark> future = executorService.submit(singleBenchmark);
                futureMap.put(singleBenchmark, future);
            }
        }
        // wait for the benchmarks to complete
        for (Map.Entry<SingleBenchmark, Future<SingleBenchmark>> futureEntry : futureMap.entrySet()) {
            SingleBenchmark singleBenchmark = futureEntry.getKey();
            Future<SingleBenchmark> future = futureEntry.getValue();
            Throwable failureThrowable = null;
            try {
                // Explicitly returning it in the Callable guarantees memory visibility
                singleBenchmark = future.get();
                // TODO WORKAROUND Remove when JBRULES-3462 is fixed.
                if (singleBenchmark.getScore() == null) {
                    throw new IllegalStateException("Score is null. TODO fix JBRULES-3462.");
                }
            } catch (InterruptedException e) {
                logger.error("The singleBenchmark (" + singleBenchmark.getName() + ") was interrupted.", e);
                failureThrowable = e;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                logger.error("The singleBenchmark (" + singleBenchmark.getName() + ") failed.", cause);
                failureThrowable = cause;
            } catch (IllegalStateException e) {
                // TODO WORKAROUND Remove when JBRULES-3462 is fixed.
                logger.error("The singleBenchmark (" + singleBenchmark.getName() + ") failed.", e);
                failureThrowable = e;
            }
            if (failureThrowable == null) {
                singleBenchmark.setSucceeded(true);
            } else {
                singleBenchmark.setSucceeded(false);
                singleBenchmark.setFailureThrowable(failureThrowable);
                failureCount++;
                if (firstFailureSingleBenchmark == null) {
                    firstFailureSingleBenchmark = singleBenchmark;
                }
            }
        }
    }

    public long calculateTimeMillisSpend() {
        long now = System.currentTimeMillis();
        return now - startingSystemTimeMillis;
    }

    public void benchmarkingEnded() {
        executorService.shutdownNow();
        for (ProblemBenchmark problemBenchmark : unifiedProblemBenchmarkList) {
            problemBenchmark.benchmarkingEnded();
        }
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            solverBenchmark.benchmarkingEnded();
        }
        determineTotalsAndAverages();
        determineSolverBenchmarkRanking();
        benchmarkTimeMillisSpend = calculateTimeMillisSpend();
        benchmarkReport.writeReport();
        if (failureCount == 0) {
            logger.info("Benchmarking ended: time spend ({}), favoriteSolverBenchmark ({}), statistic html overview ({}).",
                    new Object[]{benchmarkTimeMillisSpend, favoriteSolverBenchmark.getName(),
                            benchmarkReport.getHtmlOverviewFile().getAbsolutePath()});
        } else {
            logger.info("Benchmarking failed: time spend ({}), failureCount ({}), statistic html overview ({}).",
                    new Object[]{benchmarkTimeMillisSpend, failureCount,
                            benchmarkReport.getHtmlOverviewFile().getAbsolutePath()});
            throw new IllegalStateException("Benchmarking failed: failureCount (" + failureCount + ")." +
                    " The exception of the firstFailureSingleBenchmark (" + firstFailureSingleBenchmark.getName()
                    + ") is chained.",
                    firstFailureSingleBenchmark.getFailureThrowable());
        }
    }

    private void determineTotalsAndAverages() {
        long totalProblemScale = 0L;
        int problemScaleCount = 0;
        for (ProblemBenchmark problemBenchmark : unifiedProblemBenchmarkList) {
            Long problemScale = problemBenchmark.getProblemScale();
            if (problemScale != null && problemScale >= 0L) {
                totalProblemScale += problemScale;
                problemScaleCount++;
            }
        }
        averageProblemScale = problemScaleCount == 0 ? null : totalProblemScale / (long) problemScaleCount;
    }

    private void determineSolverBenchmarkRanking() {
        List<SolverBenchmark> rankedSolverBenchmarkList = new ArrayList<SolverBenchmark>(solverBenchmarkList);
        List<Comparable> rankedSolverBenchmarkComparableList = new ArrayList<Comparable>();
        Comparator reverseComparator = new ReverseComparator();
        // Do not rank a SolverBenchmark that has a failure
        for (Iterator<SolverBenchmark> it = rankedSolverBenchmarkList.iterator(); it.hasNext(); ) {
            SolverBenchmark solverBenchmark = it.next();
            if (solverBenchmark.hasAnyFailure()) {
                it.remove();
            }
        }
        if (solverBenchmarkRankingComparator != null) {
            Collections.sort(rankedSolverBenchmarkList, Collections.reverseOrder(solverBenchmarkRankingComparator));
        } else if (solverBenchmarkRankingWeightFactory != null) {
            SortedMap<Comparable, List<SolverBenchmark>> rankedSolverBenchmarkMap = new TreeMap<Comparable, List<SolverBenchmark>>(
                    reverseComparator);
            for (SolverBenchmark solverBenchmark : rankedSolverBenchmarkList) {
                Comparable rankingWeight = solverBenchmarkRankingWeightFactory.createRankingWeight(
                        rankedSolverBenchmarkList, solverBenchmark);
                List<SolverBenchmark> rankedSolverList = rankedSolverBenchmarkMap.get(rankingWeight);
                if (rankedSolverList == null) {
                    rankedSolverList = new ArrayList<SolverBenchmark>();
                    rankedSolverBenchmarkMap.put(rankingWeight, rankedSolverList);
                }
                rankedSolverList.add(solverBenchmark);
            }
            rankedSolverBenchmarkList.clear();
            for (Map.Entry<Comparable, List<SolverBenchmark>> entry : rankedSolverBenchmarkMap.entrySet()) {
                rankedSolverBenchmarkList.addAll(entry.getValue());
                for (int i = 0; i < entry.getValue().size(); i++) {
                    rankedSolverBenchmarkComparableList.add(entry.getKey());
                }
            }
        } else {
            throw new IllegalStateException("Ranking is impossible" +
                    " because solverBenchmarkRankingComparator and solverBenchmarkRankingWeightFactory are null.");
        }
        int ranking = 0;
        int sameRankCount = 0;
        int benchmarkNumber = 0;
        SolverBenchmark previousSolverBenchmark = null;
        for (SolverBenchmark solverBenchmark : rankedSolverBenchmarkList) {
            if (previousSolverBenchmark != null &&
                    !equalSolverRanking(solverBenchmark, previousSolverBenchmark,
                            rankedSolverBenchmarkComparableList, benchmarkNumber, reverseComparator)) {
                ranking += sameRankCount;
                sameRankCount = 1;
            } else {
                sameRankCount++;
            }
            solverBenchmark.setRanking(ranking);
            previousSolverBenchmark = solverBenchmark;
            benchmarkNumber++;
        }
        favoriteSolverBenchmark = rankedSolverBenchmarkList.isEmpty() ? null : rankedSolverBenchmarkList.get(0);
    }

    public boolean equalSolverRanking(SolverBenchmark leftSolverBenchmark, SolverBenchmark rightSolverBenchmark,
                                      List<Comparable> rankedSolverBenchmarkComparableList, int benchmarkNumber,
                                      Comparator comparator) {
        boolean equalSolverRanking = false;
        if (solverBenchmarkRankingComparator != null) {
            if (solverBenchmarkRankingComparator.compare(leftSolverBenchmark, rightSolverBenchmark) == 0) {
                equalSolverRanking = true;
            }
        } else if (solverBenchmarkRankingWeightFactory != null) {
            if (comparator.compare(rankedSolverBenchmarkComparableList.get(benchmarkNumber),
                    rankedSolverBenchmarkComparableList.get(benchmarkNumber - 1)) == 0) {
                equalSolverRanking = true;
            }
        }
        return equalSolverRanking;
    }

    public boolean hasAnyFailure() {
        return failureCount > 0;
    }

    // TODO Temporarily disabled because it crashes because of http://jira.codehaus.org/browse/XSTR-666
//    public void writeBenchmarkResult(XStream xStream) {
//        File benchmarkResultFile = new File(benchmarkReportDirectory, "benchmarkResult.xml");
//        OutputStreamWriter writer = null;
//        try {
//            writer = new OutputStreamWriter(new FileOutputStream(benchmarkResultFile), "UTF-8");
//            xStream.toXML(this, writer);
//        } catch (UnsupportedEncodingException e) {
//            throw new IllegalStateException("This JVM does not support UTF-8 encoding.", e);
//        } catch (FileNotFoundException e) {
//            throw new IllegalArgumentException(
//                    "Could not create benchmarkResultFile (" + benchmarkResultFile + ").", e);
//        } finally {
//            IOUtils.closeQuietly(writer);
//        }
//    }

}
