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
package org.optaplanner.core.impl.partitionedsearch;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.heuristic.policy.HeuristicConfigPolicy;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.recaller.BestSolutionRecallerConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.partitionedsearch.partitioner.SolutionPartitioner;
import org.optaplanner.core.impl.partitionedsearch.scope.PartitionedSearchPhaseScope;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleSupport;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.event.SolverEventSupport;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecaller;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.solver.termination.BasicPlumbingTermination;
import org.optaplanner.core.impl.solver.termination.Termination;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DefaultPartitionedSearchPhaseTest {

    @Test
    public void solve() {
        BestSolutionRecaller<TestdataSolution> bestSolutionRecaller = new BestSolutionRecallerConfig()
                .buildBestSolutionRecaller(EnvironmentMode.REPRODUCIBLE);
        bestSolutionRecaller.setSolverEventSupport(mock(SolverEventSupport.class));

        SolverFactory<TestdataSolution> sf = PlannerTestUtils
                .buildSolverFactory(TestdataSolution.class, TestdataEntity.class);
        HeuristicConfigPolicy configPolicy = new HeuristicConfigPolicy(EnvironmentMode.REPRODUCIBLE,
                (InnerScoreDirectorFactory) sf.buildSolver().getScoreDirectorFactory());

        DefaultSolverScope<TestdataSolution> scope = new DefaultSolverScope<>();
        scope.setScoreDirector(configPolicy.getScoreDirectorFactory().buildScoreDirector());

        TestdataSolution solution = new TestdataSolution();
        solution.setEntityList(Arrays.asList(new TestdataEntity("A")));
        solution.setValueList(Arrays.asList(new TestdataValue("1"), new TestdataValue("2")));

        scope.setBestSolution(solution);
        scope.setBestScore(SimpleScore.valueOfUninitialized(-1, 0));
        scope.setWorkingSolutionFromBestSolution();
        scope.setWorkingRandom(new Random(0));

        PhaseLifecycleSupport<TestdataSolution> phaseLifecycleSupport = new PhaseLifecycleSupport<>();
        Termination termination = new TerminationConfig().buildTermination(configPolicy, new BasicPlumbingTermination(false));

        DefaultPartitionedSearchPhase<TestdataSolution> phase
                = new DefaultPartitionedSearchPhase<>(0, "", bestSolutionRecaller, termination);
        phase.setSolverPhaseLifecycleSupport(phaseLifecycleSupport);
        phase.setSolutionPartitioner(new TestdataSolutionPartitioner());
        phase.setRunnablePartThreadLimit(1);
        phase.setThreadPoolExecutor(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<>()));
        phase.setConfigPolicy(configPolicy);
        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig();
        phase.setPhaseConfigList(Arrays.asList(constructionHeuristicPhaseConfig));

        // test partCount
        phase.addPhaseLifecycleListener(new PhaseLifecycleListenerAdapter<TestdataSolution>() {
            @Override
            public void phaseStarted(AbstractPhaseScope<TestdataSolution> phaseScope) {
                // TODO? should be possible to phase.addPartitionPhaseLifecycleListener() to avoid the cast?
                assertEquals(Integer.valueOf(1), ((PartitionedSearchPhaseScope) phaseScope).getPartCount());
            }
        });

        scope.startingNow();
        phase.solve(scope);
    }

    public static class TestdataSolutionPartitioner implements SolutionPartitioner<TestdataSolution> {

        @Override
        public List<TestdataSolution> splitWorkingSolution(
                ScoreDirector<TestdataSolution> scoreDirector, Integer runnablePartThreadLimit) {
            return Arrays.asList(scoreDirector.getWorkingSolution());
        }

    }
}
