package org.optaplanner.core.impl.constructionheuristic.integration;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySorterManner;
import org.optaplanner.core.config.heuristic.selector.value.ValueSorterManner;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.constructionheuristic.integration.tools.ConstructionHeuristicAssert;
import org.optaplanner.core.impl.constructionheuristic.integration.tools.ConstructionHeuristicAssertImpl;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.TestdataQueenEasyScoreFunction;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.TestdataQueenEntity;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.TestdataQueensSolution;
import org.optaplanner.core.impl.testdata.util.listeners.StepTestListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ConstructionHeuristicIntegrationTest {

    private final ConstructionHeuristicType constructionHeuristicType;
    private final EntitySorterManner entitySorterManner;
    private final ValueSorterManner valueSorterManner;
    private final ConstructionHeuristicAssert assertQueens;

    public ConstructionHeuristicIntegrationTest(ConstructionHeuristicType constructionHeuristicType, EntitySorterManner entitySorterManner, ValueSorterManner valueSorterManner, ConstructionHeuristicAssert assertQueens) {
        this.constructionHeuristicType = constructionHeuristicType;
        this.entitySorterManner = entitySorterManner;
        this.valueSorterManner = valueSorterManner;
        this.assertQueens = assertQueens;
    }

    @Test
    public void testConstructionHeuristics() {
        SolverConfig config = new SolverConfig();
        config.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);
        config.setSolutionClass(TestdataQueensSolution.class);
        config.setEntityClassList(Collections.<Class<?>>singletonList(TestdataQueenEntity.class));

        config.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig());
        config.getScoreDirectorFactoryConfig().setScoreDefinitionType(ScoreDefinitionType.SIMPLE);
        config.getScoreDirectorFactoryConfig().setEasyScoreCalculatorClass(TestdataQueenEasyScoreFunction.class);

        config.setTerminationConfig(new TerminationConfig());
        config.getTerminationConfig().setBestScoreLimit("0");

        ConstructionHeuristicPhaseConfig chConfig = new ConstructionHeuristicPhaseConfig();
        chConfig.setConstructionHeuristicType(constructionHeuristicType);
        chConfig.setEntitySorterManner(entitySorterManner);
        chConfig.setValueSorterManner(valueSorterManner);
        config.setPhaseConfigList(Collections.<PhaseConfig>singletonList(chConfig));

        TestdataQueensSolution solution = new TestdataQueensSolution();
        List<TestdataQueenEntity> entities = new ArrayList<TestdataQueenEntity>();
        int dimension = 8;
        List<Integer> rowList = new ArrayList<Integer>();
        List<Integer> columnList = new ArrayList<Integer>();
        for (int i = 0; i < dimension; i++) {
            TestdataQueenEntity entity = new TestdataQueenEntity();
            entity.setColumn(i);
            rowList.add(i);
            columnList.add(i);
            entities.add(entity);
        }
        solution.setN(dimension);
        solution.setRowList(rowList);
        solution.setColumnList(columnList);
        solution.setTestdataQueenEntityList(entities);

        ConstructionHeursiticListener listener = new ConstructionHeursiticListener();

        DefaultSolver solver = (DefaultSolver) config.buildSolver();
        solver.addPhaseLifecycleListener(listener);
        solver.solve(solution);

        TestdataQueensSolution result = (TestdataQueensSolution) solver.getBestSolution();

        assertNotNull(result);
        assertQueens.assertSolution(dimension, listener.getCoordinatesBySteps());
    }

    @Parameters(name = "ConstructionHeuristicType: {0}, EntitySorterManner: {1}, ValueSorterManner: {2}")
    public static Collection<Object[]> params() {
        Collection col = new ArrayList();

        col.add(new Object[] {ConstructionHeuristicType.FIRST_FIT, null, null, new ConstructionHeuristicAssertImpl.AssertFirstFit() });
        col.add(new Object[] {ConstructionHeuristicType.FIRST_FIT_DECREASING, null, null, new ConstructionHeuristicAssertImpl.AssertFirstFitDecreasing() });
        col.add(new Object[] {ConstructionHeuristicType.WEAKEST_FIT, null, null, new ConstructionHeuristicAssertImpl.AssertWeakestFit() });
        col.add(new Object[] {ConstructionHeuristicType.WEAKEST_FIT_DECREASING, null, null,
                new ConstructionHeuristicAssertImpl.AssertWeakestFitDecreasing() });
        col.add(new Object[] {ConstructionHeuristicType.STRONGEST_FIT, null, null, new ConstructionHeuristicAssertImpl.AssertStrongestFit() });
        col.add(new Object[] {ConstructionHeuristicType.STRONGEST_FIT_DECREASING, null, null,
                new ConstructionHeuristicAssertImpl.AssertStrongestFitDecreasing() });
        col.add(new Object[] {ConstructionHeuristicType.CHEAPEST_INSERTION, null, null, new ConstructionHeuristicAssertImpl.AssertCheapestInsertion() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, null, null,
                new ConstructionHeuristicAssertImpl.AssertWeakestFitDecreasing() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, EntitySorterManner.NONE, ValueSorterManner.NONE, new ConstructionHeuristicAssertImpl.AssertFirstFit() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, EntitySorterManner.DECREASING_DIFFICULTY, ValueSorterManner.NONE,
                new ConstructionHeuristicAssertImpl.AssertFirstFitDecreasing() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, EntitySorterManner.DECREASING_DIFFICULTY_IF_AVAILABLE, ValueSorterManner.NONE,
                new ConstructionHeuristicAssertImpl.AssertFirstFitDecreasing() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, EntitySorterManner.NONE, ValueSorterManner.INCREASING_STRENGTH,
                new ConstructionHeuristicAssertImpl.AssertWeakestFit() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, EntitySorterManner.NONE, ValueSorterManner.DECREASING_STRENGTH,
                new ConstructionHeuristicAssertImpl.AssertStrongestFit() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, EntitySorterManner.DECREASING_DIFFICULTY, ValueSorterManner.DECREASING_STRENGTH_IF_AVAILABLE,
                new ConstructionHeuristicAssertImpl.AssertStrongestFitDecreasing() });
        col.add(new Object[] {ConstructionHeuristicType.ALLOCATE_ENTITY_FROM_QUEUE, EntitySorterManner.DECREASING_DIFFICULTY, ValueSorterManner.INCREASING_STRENGTH_IF_AVAILABLE,
                new ConstructionHeuristicAssertImpl.AssertWeakestFitDecreasing() });
        return col;
    }

    /**
     * inner class - listener registering each step
     */
    private class ConstructionHeursiticListener extends StepTestListener {
        private List<Integer> filledColumns = new ArrayList<Integer>();
        private List<ImmutablePair<Integer, Integer>> coordinates = new ArrayList<ImmutablePair<Integer, Integer>>();

        @Override
        public void stepEnded(AbstractStepScope stepScope) {
            TestdataQueensSolution queens = (TestdataQueensSolution) stepScope.getWorkingSolution();
            for (TestdataQueenEntity q : queens.getTestdataQueenEntityList()) {
                if (q.getRow() != null && !filledColumns.contains(q.getColumn())) {
                    filledColumns.add(q.getColumn());
                    coordinates.add(new ImmutablePair<Integer, Integer>(q.getRow(), q.getColumn()));
                }
            }
        }

        public List<ImmutablePair<Integer, Integer>> getCoordinatesBySteps() {
            return coordinates;
        }
    }

}
