/*
 * Copyright 2014 JBoss Inc
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

package org.optaplanner.benchmark.impl.aggregator;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.optaplanner.benchmark.config.report.BenchmarkReportConfig;
import org.optaplanner.benchmark.impl.report.BenchmarkReport;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SingleBenchmarkResult;
import org.optaplanner.benchmark.impl.statistic.SingleStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkAggregator {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private File benchmarkDirectory = null;
    private BenchmarkReportConfig benchmarkReportConfig = null;

    public File getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public void setBenchmarkDirectory(File benchmarkDirectory) {
        this.benchmarkDirectory = benchmarkDirectory;
    }

    public BenchmarkReportConfig getBenchmarkReportConfig() {
        return benchmarkReportConfig;
    }

    public void setBenchmarkReportConfig(BenchmarkReportConfig benchmarkReportConfig) {
        this.benchmarkReportConfig = benchmarkReportConfig;
    }

    // ************************************************************************
    // Aggregate methods
    // ************************************************************************

    public File aggregate(List<SingleBenchmarkResult> singleBenchmarkResultList) {
        if (benchmarkDirectory == null) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must not be null.");
        }
        if (!benchmarkDirectory.exists()) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must exist.");
        }
        if (benchmarkReportConfig == null) {
            throw new IllegalArgumentException("The benchmarkReportConfig (" + benchmarkReportConfig
                    + ") must not be null.");
        }
        if (singleBenchmarkResultList.isEmpty()) {
            throw new IllegalArgumentException("The singleBenchmarkResultList (" + singleBenchmarkResultList
                    + ") must not be empty.");
        }
        Date startingTimestamp = new Date();
        for (SingleBenchmarkResult singleBenchmarkResult : singleBenchmarkResultList) {
            singleBenchmarkResult.initSingleStatisticMap();
            for (SingleStatistic singleStatistic : singleBenchmarkResult.getSingleStatisticMap().values()) {
                singleStatistic.readCsvStatisticFile();
            }
        }
        PlannerBenchmarkResult plannerBenchmarkResult
                = PlannerBenchmarkResult.createMergedResult(singleBenchmarkResultList);
        plannerBenchmarkResult.setStartingTimestamp(startingTimestamp);
        plannerBenchmarkResult.initBenchmarkReportDirectory(benchmarkDirectory);

        BenchmarkReport benchmarkReport = benchmarkReportConfig.buildBenchmarkReport(plannerBenchmarkResult);
        plannerBenchmarkResult.accumulateResults(benchmarkReport);
        benchmarkReport.writeReport();

        logger.info("Aggregation ended: statistic html overview ({}).",
                benchmarkReport.getHtmlOverviewFile().getAbsolutePath());
        return benchmarkReport.getHtmlOverviewFile().getAbsoluteFile();
    }

}
