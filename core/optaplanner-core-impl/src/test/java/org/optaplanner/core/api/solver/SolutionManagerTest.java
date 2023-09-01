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

package org.optaplanner.core.api.solver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowedSolution;

public class SolutionManagerTest {

    public static final SolverFactory<TestdataShadowedSolution> SOLVER_FACTORY =
            SolverFactory.createFromXmlResource("org/optaplanner/core/api/solver/testdataShadowedSolverConfig.xml");

    @ParameterizedTest
    @EnumSource(SolutionManagerSource.class)
    void updateEverything(SolutionManagerSource SolutionManagerSource) {
        SolutionManager<TestdataShadowedSolution, ?> SolutionManager =
                SolutionManagerSource.createSolutionManager(SOLVER_FACTORY);
        assertThat(SolutionManager).isNotNull();
        TestdataShadowedSolution solution = TestdataShadowedSolution.generateSolution();
        assertSoftly(softly -> {
            softly.assertThat(solution.getScore()).isNull();
            softly.assertThat(solution.getEntityList().get(0).getFirstShadow()).isNull();
        });
        SolutionManager.update(solution);
        assertSoftly(softly -> {
            softly.assertThat(solution.getScore()).isNotNull();
            softly.assertThat(solution.getEntityList().get(0).getFirstShadow()).isNotNull();
        });
    }

    @ParameterizedTest
    @EnumSource(SolutionManagerSource.class)
    void updateOnlyShadowVariables(SolutionManagerSource SolutionManagerSource) {
        SolutionManager<TestdataShadowedSolution, ?> SolutionManager =
                SolutionManagerSource.createSolutionManager(SOLVER_FACTORY);
        assertThat(SolutionManager).isNotNull();
        TestdataShadowedSolution solution = TestdataShadowedSolution.generateSolution();
        assertSoftly(softly -> {
            softly.assertThat(solution.getScore()).isNull();
            softly.assertThat(solution.getEntityList().get(0).getFirstShadow()).isNull();
        });
        SolutionManager.update(solution, SolutionUpdatePolicy.UPDATE_SHADOW_VARIABLES_ONLY);
        assertSoftly(softly -> {
            softly.assertThat(solution.getScore()).isNull();
            softly.assertThat(solution.getEntityList().get(0).getFirstShadow()).isNotNull();
        });
    }

    @ParameterizedTest
    @EnumSource(SolutionManagerSource.class)
    void updateOnlyScore(SolutionManagerSource SolutionManagerSource) {
        SolutionManager<TestdataShadowedSolution, ?> SolutionManager =
                SolutionManagerSource.createSolutionManager(SOLVER_FACTORY);
        assertThat(SolutionManager).isNotNull();
        TestdataShadowedSolution solution = TestdataShadowedSolution.generateSolution();
        assertSoftly(softly -> {
            softly.assertThat(solution.getScore()).isNull();
            softly.assertThat(solution.getEntityList().get(0).getFirstShadow()).isNull();
        });
        SolutionManager.update(solution, SolutionUpdatePolicy.UPDATE_SCORE_ONLY);
        assertSoftly(softly -> {
            softly.assertThat(solution.getScore()).isNotNull();
            softly.assertThat(solution.getEntityList().get(0).getFirstShadow()).isNull();
        });
    }

    @ParameterizedTest
    @EnumSource(SolutionManagerSource.class)
    void explain(SolutionManagerSource SolutionManagerSource) {
        SolutionManager<TestdataShadowedSolution, ?> solutionManager =
                SolutionManagerSource.createSolutionManager(SOLVER_FACTORY);
        assertThat(solutionManager).isNotNull();
        TestdataShadowedSolution solution = TestdataShadowedSolution.generateSolution();
        ScoreExplanation<TestdataShadowedSolution, ?> scoreExplanation = solutionManager.explain(solution);
        assertThat(scoreExplanation).isNotNull();
        assertSoftly(softly -> {
            softly.assertThat(scoreExplanation.getScore()).isNotNull();
            softly.assertThat(scoreExplanation.getSummary()).isNotBlank();
            softly.assertThat(scoreExplanation.getConstraintMatchTotalMap())
                    .containsOnlyKeys("org.optaplanner.core.impl.testdata.domain.shadow/testConstraint");
            softly.assertThat(scoreExplanation.getIndictmentMap())
                    .containsOnlyKeys(solution.getEntityList().toArray());

        });
    }

    public enum SolutionManagerSource {

        FROM_SOLVER_FACTORY(SolutionManager::create),
        FROM_SOLVER_MANAGER(solverFactory -> SolutionManager.create(SolverManager.create(solverFactory)));

        private final Function<SolverFactory, SolutionManager> SolutionManagerConstructor;

        SolutionManagerSource(Function<SolverFactory, SolutionManager> SolutionManagerConstructor) {
            this.SolutionManagerConstructor = SolutionManagerConstructor;
        }

        public <Solution_, Score_ extends Score<Score_>> SolutionManager<Solution_, Score_>
                createSolutionManager(SolverFactory<Solution_> solverFactory) {
            return SolutionManagerConstructor.apply(solverFactory);
        }

    }

}
