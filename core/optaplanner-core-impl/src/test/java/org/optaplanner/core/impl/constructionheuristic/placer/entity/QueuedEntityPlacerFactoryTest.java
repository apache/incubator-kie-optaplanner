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

package org.optaplanner.core.impl.constructionheuristic.placer.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.constructionheuristic.placer.entity.PlacementAssertions.assertEntityPlacement;
import static org.optaplanner.core.impl.heuristic.HeuristicConfigPolicyTestUtils.buildHeuristicConfigPolicy;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.config.constructionheuristic.placer.QueuedEntityPlacerConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.constructionheuristic.placer.Placement;
import org.optaplanner.core.impl.constructionheuristic.placer.QueuedEntityPlacer;
import org.optaplanner.core.impl.constructionheuristic.placer.QueuedEntityPlacerFactory;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarSolution;

class QueuedEntityPlacerFactoryTest {

    @Test
    void buildFromUnfoldNew() {
        SolutionDescriptor<TestdataMultiVarSolution> solutionDescriptor = TestdataMultiVarSolution.buildSolutionDescriptor();

        ChangeMoveSelectorConfig primaryMoveSelectorConfig = new ChangeMoveSelectorConfig()
                .withValueSelectorConfig(new ValueSelectorConfig("primaryValue"));
        ChangeMoveSelectorConfig secondaryMoveSelectorConfig = new ChangeMoveSelectorConfig()
                .withValueSelectorConfig(new ValueSelectorConfig("secondaryValue"));

        HeuristicConfigPolicy<TestdataMultiVarSolution> configPolicy = buildHeuristicConfigPolicy(solutionDescriptor);
        QueuedEntityPlacerConfig placerConfig = QueuedEntityPlacerFactory.unfoldNew(configPolicy,
                Arrays.asList(primaryMoveSelectorConfig, secondaryMoveSelectorConfig));

        assertThat(placerConfig.getEntitySelectorConfig().getEntityClass()).isAssignableFrom(TestdataMultiVarEntity.class);
        assertThat(placerConfig.getMoveSelectorConfigList())
                .hasSize(2)
                .hasOnlyElementsOfType(ChangeMoveSelectorConfig.class);

        QueuedEntityPlacer<TestdataMultiVarSolution> entityPlacer =
                new QueuedEntityPlacerFactory<TestdataMultiVarSolution>(placerConfig)
                        .buildEntityPlacer(configPolicy);

        SolverScope<TestdataMultiVarSolution> solverScope = mock(SolverScope.class);
        entityPlacer.solvingStarted(solverScope);
        AbstractPhaseScope<TestdataMultiVarSolution> phaseScope = mock(AbstractPhaseScope.class);
        when(phaseScope.getSolverScope()).thenReturn(solverScope);
        InnerScoreDirector<TestdataMultiVarSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        when(phaseScope.getScoreDirector()).thenReturn((InnerScoreDirector) scoreDirector);
        when(scoreDirector.getWorkingSolution()).thenReturn(generateTestdataSolution());
        entityPlacer.phaseStarted(phaseScope);
        Iterator<Placement<TestdataMultiVarSolution>> placementIterator = entityPlacer.iterator();
        assertThat(placementIterator).hasNext();

        AbstractStepScope<TestdataMultiVarSolution> stepScope = mock(AbstractStepScope.class);
        when(stepScope.getPhaseScope()).thenReturn(phaseScope);
        when(stepScope.getScoreDirector()).thenReturn((InnerScoreDirector) scoreDirector);
        entityPlacer.stepStarted(stepScope);
        Placement<TestdataMultiVarSolution> placement = placementIterator.next();

        assertEntityPlacement(placement, "e1", "e1v1", "e1v2", "e2v1", "e2v2");
    }

    private TestdataMultiVarSolution generateTestdataSolution() {
        TestdataMultiVarEntity entity1 = new TestdataMultiVarEntity("e1");
        entity1.setPrimaryValue(new TestdataValue("e1v1"));
        entity1.setSecondaryValue(new TestdataValue("e1v2"));
        TestdataMultiVarEntity entity2 = new TestdataMultiVarEntity("e2");
        entity2.setPrimaryValue(new TestdataValue("e2v1"));
        entity2.setSecondaryValue(new TestdataValue("e2v2"));

        TestdataMultiVarSolution solution = new TestdataMultiVarSolution("s");
        solution.setMultiVarEntityList(Arrays.asList(entity1, entity2));
        solution.setValueList(Arrays.asList(entity1.getPrimaryValue(), entity1.getSecondaryValue(), entity2.getPrimaryValue(),
                entity2.getSecondaryValue()));
        return solution;
    }
}
