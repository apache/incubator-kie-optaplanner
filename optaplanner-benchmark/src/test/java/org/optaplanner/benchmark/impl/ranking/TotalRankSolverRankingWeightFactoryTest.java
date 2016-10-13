/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.benchmark.impl.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.optaplanner.benchmark.impl.report.BenchmarkReport;
import org.optaplanner.benchmark.impl.result.SingleBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TotalRankSolverRankingWeightFactoryTest extends AbstractSolverRankingComparatorTest {

    private BenchmarkReport benchmarkReport;
    private TotalRankSolverRankingWeightFactory factory;
    private List<SolverBenchmarkResult> solverBenchmarkResultList;
    private SolverBenchmarkResult a;
    private SolverBenchmarkResult b;
    private List<SingleBenchmarkResult> aSingleBenchmarkResultList;
    private List<SingleBenchmarkResult> bSingleBenchmarkResultList;

    @Before
    public void setUp() {
        benchmarkReport = mock(BenchmarkReport.class);
        factory = new TotalRankSolverRankingWeightFactory();
        solverBenchmarkResultList = new ArrayList<>();
        a = new SolverBenchmarkResult(null);
        b = new SolverBenchmarkResult(null);
        aSingleBenchmarkResultList = new ArrayList<>();
        bSingleBenchmarkResultList = new ArrayList<>();
    }

    @Test
    public void normal() {
        addSingleBenchmark(aSingleBenchmarkResultList, -1000, -40, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -300, -40, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -40, -40, -1000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList);
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        addSingleBenchmark(bSingleBenchmarkResultList, -2000, -30, -2000); // Loses vs a
        addSingleBenchmark(bSingleBenchmarkResultList, -200, -30, -2000); // Wins vs a
        addSingleBenchmark(bSingleBenchmarkResultList, -30, -30, -2000); // Wins vs a
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList);
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        List<SingleBenchmarkResult> totalSingleBenchmarkResultList = new ArrayList<>(aSingleBenchmarkResultList);
        totalSingleBenchmarkResultList.addAll(bSingleBenchmarkResultList);
        addProblemBenchmark(totalSingleBenchmarkResultList);

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        assertThat(Arrays.asList(aWeight, bWeight)).isSorted();
    }

    @Test
    public void equalCount() {
        addSingleBenchmark(aSingleBenchmarkResultList, -5000, -90, -5000);
        addSingleBenchmark(aSingleBenchmarkResultList, -900, -90, -5000);
        addSingleBenchmark(aSingleBenchmarkResultList, -90, -90, -5000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList); // 0 wins - 1 equals - 5 losses
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        addSingleBenchmark(bSingleBenchmarkResultList, -1000, -20, -1000); // Wins vs a - wins vs c
        addSingleBenchmark(bSingleBenchmarkResultList, -200, -20, -1000); // Wins vs a - loses vs c
        addSingleBenchmark(bSingleBenchmarkResultList, -20, -20, -1000); // Wins vs a - loses vs c
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList); // 4 wins - 0 equals - 2 losses
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        SolverBenchmarkResult c = new SolverBenchmarkResult(null);
        List<SingleBenchmarkResult> cSingleBenchmarkResultList = new ArrayList<>();
        addSingleBenchmark(cSingleBenchmarkResultList, -5000, -10, -5000); // Loses vs b, Equals vs a
        addSingleBenchmark(cSingleBenchmarkResultList, -100, -10, -5000); // Wins vs a - wins vs b
        addSingleBenchmark(cSingleBenchmarkResultList, -10, -10, -5000); // Wins vs a - wins vs b
        c.setSingleBenchmarkResultList(cSingleBenchmarkResultList); // 4 wins - 1 equals - 1 losses
        c.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(c);
        List<SingleBenchmarkResult> totalSingleBenchmarkResultList = new ArrayList<>(aSingleBenchmarkResultList);
        totalSingleBenchmarkResultList.addAll(bSingleBenchmarkResultList);
        totalSingleBenchmarkResultList.addAll(cSingleBenchmarkResultList);
        addProblemBenchmark(totalSingleBenchmarkResultList);

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        Comparable cWeight = factory.createRankingWeight(solverBenchmarkResultList, c);

        assertThat(Arrays.asList(aWeight, bWeight, cWeight)).isSorted();
    }

    @Test
    public void uninitializedSingleBenchmarks() {
        SingleBenchmarkResult a0 = addSingleBenchmark(aSingleBenchmarkResultList, -1000, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -400, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -30, -30, -1000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList);
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        SingleBenchmarkResult b0 = addSingleBenchmark(bSingleBenchmarkResultList, -1000, -30, -1000);
        SingleBenchmarkResult b1 = addSingleBenchmark(bSingleBenchmarkResultList, -400, -30, -1000);
        addSingleBenchmark(bSingleBenchmarkResultList, -30, -30, -1000);
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList);
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        List<SingleBenchmarkResult> totalSingleBenchmarkResultList = new ArrayList<>(aSingleBenchmarkResultList);
        totalSingleBenchmarkResultList.addAll(bSingleBenchmarkResultList);
        addProblemBenchmark(totalSingleBenchmarkResultList);

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        assertThat(aWeight).isEqualByComparingTo(bWeight);
        assertThat(bWeight).isEqualByComparingTo(aWeight);

        a0.setAverageScore(SimpleScore.valueOf(-100, -1000));
        b0.setAverageScore(SimpleScore.valueOf(-100, -1000));
        a.accumulateResults(benchmarkReport);
        b.accumulateResults(benchmarkReport);
        // ranks, uninitialized variable counts, total scores and worst scores are equal
        assertThat(aWeight).isEqualByComparingTo(bWeight);
        assertThat(bWeight).isEqualByComparingTo(aWeight);

        b0.setAverageScore(SimpleScore.valueOfInitialized(-1000));
        b1.setAverageScore(SimpleScore.valueOf(-100, -400));
        b.accumulateResults(benchmarkReport);
        // ranks, uninitialized variable counts and total scores are equal, A loses on worst score (tie-breaker)
        assertThat(Arrays.asList(aWeight, bWeight)).isSorted();

        b1.setAverageScore(SimpleScore.valueOf(-101, -400));
        b.accumulateResults(benchmarkReport);
        // ranks are equal, uninitialized variable count is bigger in B
        assertThat(Arrays.asList(bWeight, aWeight)).isSorted();
    }

    @Test
    public void differentNumberOfSingleBenchmarks() {
        addSingleBenchmark(aSingleBenchmarkResultList, -1000, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -400, -30, -1000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList);
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        addSingleBenchmark(bSingleBenchmarkResultList, -1000, -30, -1000);
        addSingleBenchmark(bSingleBenchmarkResultList, -400, -30, -1000);
        addSingleBenchmark(bSingleBenchmarkResultList, -30, -30, -1000);
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList);
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        List<SingleBenchmarkResult> totalSingleBenchmarkResultList = new ArrayList<>(aSingleBenchmarkResultList);
        totalSingleBenchmarkResultList.addAll(bSingleBenchmarkResultList);
        addProblemBenchmark(totalSingleBenchmarkResultList);

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        assertThat(Arrays.asList(aWeight, bWeight)).isSorted();
    }

    @Test
    public void differentScoreTypeOfSingleBenchmarks() {
        addSingleBenchmark(aSingleBenchmarkResultList, -1000, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -400, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -30, -30, -1000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList);
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        // Scores with different number of levels are compared from the highest level, see ResilientScoreComparator.compare
        addSingleBenchmarkWithHardSoftLongScore(bSingleBenchmarkResultList, -1000, 0, -30, 0, -1000, -1000);
        addSingleBenchmarkWithHardSoftLongScore(bSingleBenchmarkResultList, -400, 0, -30, 0, -1000, -1000);
        addSingleBenchmarkWithHardSoftLongScore(bSingleBenchmarkResultList, -30, 0, -30, 0, -1000, -1000);
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList);
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        List<SingleBenchmarkResult> totalSingleBenchmarkResultList = new ArrayList<>(aSingleBenchmarkResultList);
        totalSingleBenchmarkResultList.addAll(bSingleBenchmarkResultList);
        addProblemBenchmark(totalSingleBenchmarkResultList);

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        assertThat(aWeight).isEqualByComparingTo(bWeight);
        assertThat(bWeight).isEqualByComparingTo(aWeight);
    }

    @Test
    public void disjunctPlannnerBenchmarks() {
        addSingleBenchmark(aSingleBenchmarkResultList, -1000, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -400, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -30, -30, -1000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList);
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        addSingleBenchmark(bSingleBenchmarkResultList, -2000, -30, -2000);
        addSingleBenchmark(bSingleBenchmarkResultList, -200, -30, -2000);
        addSingleBenchmark(bSingleBenchmarkResultList, -30, -30, -2000);
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList);
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        // A and B have different datasets (6 datasets in total)
        for (SolverBenchmarkResult solverBenchmarkResult : solverBenchmarkResultList) {
            for (SingleBenchmarkResult singleBenchmarkResult : solverBenchmarkResult.getSingleBenchmarkResultList()) {
                addProblemBenchmark(Arrays.asList(singleBenchmarkResult));
            }
        }

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        // Tie-breaker, A wins on total score
        assertThat(Arrays.asList(bWeight, aWeight)).isSorted();
    }

    @Test
    public void disjunctEqualPlannerBenchmarks() {
        addSingleBenchmark(aSingleBenchmarkResultList, -1000, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -400, -30, -1000);
        addSingleBenchmark(aSingleBenchmarkResultList, -30, -30, -1000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList);
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        addSingleBenchmark(bSingleBenchmarkResultList, -1000, -30, -1000);
        addSingleBenchmark(bSingleBenchmarkResultList, -400, -30, -1000);
        addSingleBenchmark(bSingleBenchmarkResultList, -30, -30, -1000);
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList);
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        // A and B have different datasets (6 datasets in total)
        for (SolverBenchmarkResult solverBenchmarkResult : solverBenchmarkResultList) {
            for (SingleBenchmarkResult singleBenchmarkResult : solverBenchmarkResult.getSingleBenchmarkResultList()) {
                addProblemBenchmark(Arrays.asList(singleBenchmarkResult));
            }
        }

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        // Tie-breaker (total score) is equal
        assertThat(aWeight).isEqualByComparingTo(bWeight);
        assertThat(bWeight).isEqualByComparingTo(aWeight);
    }

    @Test
    public void overlappingPlannnerBenchmarks() {
        SingleBenchmarkResult a0 = addSingleBenchmark(aSingleBenchmarkResultList, -1000, -30, -1000);
        SingleBenchmarkResult a1 = addSingleBenchmark(aSingleBenchmarkResultList, -400, -30, -1000);
        SingleBenchmarkResult a2 = addSingleBenchmark(aSingleBenchmarkResultList, -30, -30, -1000);
        a.setSingleBenchmarkResultList(aSingleBenchmarkResultList);
        a.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(a);
        SingleBenchmarkResult b0 = addSingleBenchmark(bSingleBenchmarkResultList, -1000, -30, -1000);
        SingleBenchmarkResult b1 = addSingleBenchmark(bSingleBenchmarkResultList, -400, -30, -1000);
        SingleBenchmarkResult b2 = addSingleBenchmark(bSingleBenchmarkResultList, -30, -30, -1000);
        b.setSingleBenchmarkResultList(bSingleBenchmarkResultList);
        b.accumulateResults(benchmarkReport);
        solverBenchmarkResultList.add(b);
        addProblemBenchmark(Arrays.asList(a0, b0));
        addProblemBenchmark(Arrays.asList(a1, b1));
        addProblemBenchmark(Arrays.asList(a2, b2));

        Comparable aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        Comparable bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        assertThat(aWeight).isEqualByComparingTo(bWeight);
        assertThat(bWeight).isEqualByComparingTo(aWeight);

        addProblemBenchmark(Arrays.asList(a1));
        addProblemBenchmark(Arrays.asList(a2));
        addProblemBenchmark(Arrays.asList(b0));
        addProblemBenchmark(Arrays.asList(b2));
        addProblemBenchmark(Arrays.asList(a0, b1));
        aWeight = factory.createRankingWeight(solverBenchmarkResultList, a);
        bWeight = factory.createRankingWeight(solverBenchmarkResultList, b);
        // A looses on score: a0 vs b1
        assertThat(Arrays.asList(aWeight, bWeight)).isSorted();
    }

}
