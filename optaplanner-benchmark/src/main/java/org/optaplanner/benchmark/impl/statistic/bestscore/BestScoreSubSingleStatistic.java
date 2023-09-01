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

package org.optaplanner.benchmark.impl.statistic.bestscore;

import java.util.List;

import org.optaplanner.benchmark.config.statistic.ProblemStatisticType;
import org.optaplanner.benchmark.impl.result.SubSingleBenchmarkResult;
import org.optaplanner.benchmark.impl.statistic.ProblemBasedSubSingleStatistic;
import org.optaplanner.benchmark.impl.statistic.StatisticPoint;
import org.optaplanner.benchmark.impl.statistic.StatisticRegistry;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.monitoring.SolverMetric;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

import io.micrometer.core.instrument.Tags;

public class BestScoreSubSingleStatistic<Solution_>
        extends ProblemBasedSubSingleStatistic<Solution_, BestScoreStatisticPoint> {

    BestScoreSubSingleStatistic() {
        // For JAXB.
    }

    public BestScoreSubSingleStatistic(SubSingleBenchmarkResult subSingleBenchmarkResult) {
        super(subSingleBenchmarkResult, ProblemStatisticType.BEST_SCORE);
    }

    // ************************************************************************
    // Lifecycle methods
    // ************************************************************************

    @Override
    public void open(StatisticRegistry<Solution_> registry, Tags runTag, Solver<Solution_> solver) {
        registry.addListener(SolverMetric.BEST_SCORE,
                timestamp -> registry.extractScoreFromMeters(SolverMetric.BEST_SCORE, runTag,
                        score -> pointList.add(new BestScoreStatisticPoint(timestamp, score))));
    }

    // ************************************************************************
    // CSV methods
    // ************************************************************************

    @Override
    protected String getCsvHeader() {
        return StatisticPoint.buildCsvLine("timeMillisSpent", "score");
    }

    @Override
    protected BestScoreStatisticPoint createPointFromCsvLine(ScoreDefinition<?> scoreDefinition,
            List<String> csvLine) {
        return new BestScoreStatisticPoint(Long.parseLong(csvLine.get(0)),
                scoreDefinition.parseScore(csvLine.get(1)));
    }

}
