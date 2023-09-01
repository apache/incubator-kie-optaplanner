/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.benchmark.impl.aggregator;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.optaplanner.benchmark.config.report.BenchmarkReportConfig;
import org.optaplanner.benchmark.impl.report.BenchmarkReport;
import org.optaplanner.benchmark.impl.report.BenchmarkReportFactory;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SingleBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SubSingleBenchmarkResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkAggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkAggregator.class);

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
        return aggregate(singleBenchmarkResultList, null);
    }

    public File aggregate(List<SingleBenchmarkResult> singleBenchmarkResultList,
            Map<SolverBenchmarkResult, String> solverBenchmarkResultNameMap) {
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
        OffsetDateTime startingTimestamp = OffsetDateTime.now();
        for (SingleBenchmarkResult singleBenchmarkResult : singleBenchmarkResultList) {
            for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult.getSubSingleBenchmarkResultList()) {
                subSingleBenchmarkResult.setSingleBenchmarkResult(singleBenchmarkResult);
            }
            singleBenchmarkResult.initSubSingleStatisticMaps();
        }
        // Handle renamed solver benchmarks after statistics have been read (they're resolved by
        // original solver benchmarks' names)
        if (solverBenchmarkResultNameMap != null) {
            for (Entry<SolverBenchmarkResult, String> entry : solverBenchmarkResultNameMap.entrySet()) {
                SolverBenchmarkResult result = entry.getKey();
                String newName = entry.getValue();
                if (!result.getName().equals(newName)) {
                    result.setName(newName);
                }
            }
        }

        PlannerBenchmarkResult plannerBenchmarkResult = PlannerBenchmarkResult.createMergedResult(
                singleBenchmarkResultList);
        plannerBenchmarkResult.setStartingTimestamp(startingTimestamp);
        plannerBenchmarkResult.initBenchmarkReportDirectory(benchmarkDirectory);

        BenchmarkReportFactory benchmarkReportFactory = new BenchmarkReportFactory(benchmarkReportConfig);
        BenchmarkReport benchmarkReport = benchmarkReportFactory.buildBenchmarkReport(plannerBenchmarkResult);
        plannerBenchmarkResult.accumulateResults(benchmarkReport);
        benchmarkReport.writeReport();

        LOGGER.info("Aggregation ended: statistic html overview ({}).",
                benchmarkReport.getHtmlOverviewFile().getAbsolutePath());
        return benchmarkReport.getHtmlOverviewFile().getAbsoluteFile();
    }

}
