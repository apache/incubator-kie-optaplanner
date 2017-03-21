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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.partitionedsearch.PartitionedSearchPhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.partitionedsearch.partitioner.SolutionPartitioner;
import org.optaplanner.core.impl.partitionedsearch.scope.PartitionedSearchPhaseScope;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.junit.Assert.assertEquals;

public class DefaultPartitionedSearchPhaseTest {

    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    private int initialThreadCount;

    @Before
    public void setUp() {
        initialThreadCount = threads.getThreadCount();
    }

    @Test
    public void solve() {
        SolverFactory<TestdataSolution> solverFactory = createSolverFactory();
        DefaultSolver<TestdataSolution> solver = (DefaultSolver<TestdataSolution>) solverFactory.buildSolver();
        PartitionedSearchPhase<TestdataSolution> phase
                = (PartitionedSearchPhase<TestdataSolution>) solver.getPhaseList().get(0);

        // test partCount
        phase.addPhaseLifecycleListener(new PhaseLifecycleListenerAdapter<TestdataSolution>() {
            @Override
            public void phaseStarted(AbstractPhaseScope<TestdataSolution> phaseScope) {
                // TODO? should be possible to phase.addPartitionPhaseLifecycleListener() to avoid the cast?
                assertEquals(Integer.valueOf(1), ((PartitionedSearchPhaseScope) phaseScope).getPartCount());
            }
        });

        solver.solve(createSolution());
        assertEquals(initialThreadCount, threads.getThreadCount());
    }

    private static SolverFactory<TestdataSolution> createSolverFactory() {
        SolverFactory<TestdataSolution> solverFactory = PlannerTestUtils
                .buildSolverFactory(TestdataSolution.class, TestdataEntity.class);
        SolverConfig solverConfig = solverFactory.getSolverConfig();
        PartitionedSearchPhaseConfig partitionedSearchPhaseConfig = new PartitionedSearchPhaseConfig();
        partitionedSearchPhaseConfig.setSolutionPartitionerClass(TestdataSolutionPartitioner.class);
        solverConfig.setPhaseConfigList(Arrays.asList(partitionedSearchPhaseConfig));
        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig();
        LocalSearchPhaseConfig localSearchPhaseConfig = new LocalSearchPhaseConfig();
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setStepCountLimit(1);
        localSearchPhaseConfig.setTerminationConfig(terminationConfig);
        partitionedSearchPhaseConfig.setPhaseConfigList(Arrays.asList(constructionHeuristicPhaseConfig, localSearchPhaseConfig));
        return solverFactory;
    }

    private static TestdataSolution createSolution() {
        TestdataSolution solution = new TestdataSolution();
        solution.setEntityList(Arrays.asList(new TestdataEntity("A")));
        solution.setValueList(Arrays.asList(new TestdataValue("1"), new TestdataValue("2")));
        return solution;
    }

    public static class TestdataSolutionPartitioner implements SolutionPartitioner<TestdataSolution> {

        @Override
        public List<TestdataSolution> splitWorkingSolution(
                ScoreDirector<TestdataSolution> scoreDirector, Integer runnablePartThreadLimit) {
            return Arrays.asList(scoreDirector.getWorkingSolution());
        }

    }
}
