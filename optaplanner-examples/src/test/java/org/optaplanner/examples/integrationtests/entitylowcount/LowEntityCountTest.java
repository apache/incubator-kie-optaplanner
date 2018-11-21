/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.integrationtests.entitylowcount;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.SolverConfigContext;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.examples.common.app.DataSetLoader;
import org.optaplanner.examples.common.app.SolverBuilder;
import org.optaplanner.examples.tsp.app.TspApp;
import org.optaplanner.examples.tsp.domain.TspSolution;
import org.optaplanner.examples.tsp.domain.Visit;
import org.optaplanner.examples.tsp.solver.score.TspIncrementalScoreCalculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class LowEntityCountTest {

    private static final int LOW_ENTITY_STEPS_TERMINATION = 1;
    private static final String PATH_TO_DATA_DIRECTORY = "/org/optaplanner/examples/integrationtests/lowentitycount/data";
    private final TspApp app = new TspApp();

    @Test(timeout = 600_000)
    public void zeroEntity() {
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_STEPS_TERMINATION, app.getSolverConfig());
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%s/oneDomicileOnly.xml", PATH_TO_DATA_DIRECTORY));
        assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");
    }

    @Ignore("PLANNER-1180")
    @Test(timeout = 600_000)
    public void oneEntity() {
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_STEPS_TERMINATION, app.getSolverConfig());
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%s/oneDomicileOneEntity.xml", PATH_TO_DATA_DIRECTORY));
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test(timeout = 600_000)
    public void twoEntities() {
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_STEPS_TERMINATION, app.getSolverConfig());
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%s/twoEnt.xml", PATH_TO_DATA_DIRECTORY));
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test(timeout = 600_000)
    public void zeroEntityWithNearbySelection() {
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%s/oneDomicileOnly.xml", PATH_TO_DATA_DIRECTORY));
        Solver<TspSolution> tspSolutionSolver = createSolverConfig().buildSolver(new SolverConfigContext());
        assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");
    }

    @Ignore("PLANNER-1180")
    @Test(timeout = 600_000)
    public void oneEntityWithNearbySelection() {
        Solver<TspSolution> tspSolutionSolver = createSolverConfig().buildSolver(new SolverConfigContext());
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%s/oneDomicileOneEntity.xml", PATH_TO_DATA_DIRECTORY));
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test(timeout = 600_000)
    public void twoEntitiesWithNearbySelection() {
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%s/twoEnt.xml", PATH_TO_DATA_DIRECTORY));
        Solver<TspSolution> tspSolutionSolver = createSolverConfig().buildSolver(new SolverConfigContext());
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    private SolverConfig createSolverConfig() {
        SolverConfig config = new SolverConfig();
        config.setPhaseConfigList(new ArrayList<>(2));

        List<Class<?>> entityClassList = createEntityClassList();
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = createScoreDirectorFactoryConfig();
        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = getConstructionHeuristicPhaseConfig();
        LocalSearchPhaseConfig localSearchPhaseConfig = createLocalSearchPhaseConfig();

        config.setSolutionClass(TspSolution.class);
        config.setEntityClassList(entityClassList);

        config.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);
        config.getPhaseConfigList().add(constructionHeuristicPhaseConfig);
        config.getPhaseConfigList().add(localSearchPhaseConfig);

        return config;
    }

    private LocalSearchPhaseConfig createLocalSearchPhaseConfig() {
        LocalSearchPhaseConfig localSearchPhaseConfig = new LocalSearchPhaseConfig();
        UnionMoveSelectorConfig unionMoveSelectorConfig = createUnionMoveSelectorConfig();

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setLateAcceptanceSize(Integer.valueOf(200));

        LocalSearchForagerConfig foragerConfig = new LocalSearchForagerConfig();
        foragerConfig.setAcceptedCountLimit(Integer.valueOf(1));

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setStepCountLimit(LOW_ENTITY_STEPS_TERMINATION);

        localSearchPhaseConfig.setTerminationConfig(terminationConfig);

        localSearchPhaseConfig.setAcceptorConfig(acceptorConfig);
        localSearchPhaseConfig.setForagerConfig(foragerConfig);

        localSearchPhaseConfig.setMoveSelectorConfig(unionMoveSelectorConfig);
        return localSearchPhaseConfig;
    }

    private UnionMoveSelectorConfig createUnionMoveSelectorConfig() {
        UnionMoveSelectorConfig unionMoveSelectorConfig = new UnionMoveSelectorConfig();
        unionMoveSelectorConfig.setMoveSelectorConfigList(new ArrayList<>(3));

        unionMoveSelectorConfig.getMoveSelectorConfigList().add(new ChangeMoveSelectorConfig());
        unionMoveSelectorConfig.getMoveSelectorConfigList().add(new SwapMoveSelectorConfig());
        unionMoveSelectorConfig.getMoveSelectorConfigList().add(new TailChainSwapMoveSelectorConfig());
        return unionMoveSelectorConfig;
    }

    private ConstructionHeuristicPhaseConfig getConstructionHeuristicPhaseConfig() {
        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig();
        constructionHeuristicPhaseConfig.setConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT_DECREASING);
        return constructionHeuristicPhaseConfig;
    }

    private ScoreDirectorFactoryConfig createScoreDirectorFactoryConfig() {
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setIncrementalScoreCalculatorClass(TspIncrementalScoreCalculator.class);
        scoreDirectorFactoryConfig.setInitializingScoreTrend("ONLY_DOWN");
        return scoreDirectorFactoryConfig;
    }

    private List<Class<?>> createEntityClassList() {
        List<Class<?>> entityClassList = new ArrayList<>(1);
        entityClassList.add(Visit.class);
        return entityClassList;
    }
}
