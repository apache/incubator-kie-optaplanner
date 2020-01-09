package org.optaplanner.core.impl.exhaustivesearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.exhaustivesearch.ExhaustiveSearchPhaseConfig;
import org.optaplanner.core.config.exhaustivesearch.ExhaustiveSearchType;
import org.optaplanner.core.config.exhaustivesearch.NodeExplorationType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySorterManner;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSorterManner;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.exhaustivesearch.event.ExhaustiveSearchListener;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableEntity;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableSolution;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableValue;
import org.optaplanner.core.impl.testdata.score.director.TestdataComparableDifferentValuesCalculator;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ExhaustiveSearchTest {

    private final ExhaustiveSearchType exhaustiveSearchType;
    private final NodeExplorationType nodeExplorationType;
    private final EntitySorterManner entitySorterManner;
    private final ValueSorterManner valueSorterManner;
    private final List<String> steps;
    private TestdataComparableSolution solution;
    private SolverConfig solverConfig;

    public ExhaustiveSearchTest(ExhaustiveSearchType exhaustiveSearchType, NodeExplorationType nodeExplorationType,
                                EntitySorterManner entitySorterManner, ValueSorterManner valueSorterManner, List<String> steps) {
        this.exhaustiveSearchType = exhaustiveSearchType;
        this.nodeExplorationType = nodeExplorationType;
        this.entitySorterManner = entitySorterManner;
        this.valueSorterManner = valueSorterManner;
        this.steps = steps;
    }

    /**
     * Initialize combination of input parameters.
     *
     * @return collection of combination of input parameters
     */
    @Parameterized.Parameters(name = "{0}, NodeExplorationType-{1}, EntitySorterManner-{2}, ValueSorterManner-{3}")
    public static Collection<Object[]> params() {
        Collection<Object[]> col = new ArrayList<>();

        // -------------------------------
        // BRANCH AND BOUND :: Depth First
        // -------------------------------
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.DEPTH_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "13--", "132-", "13-2", "12--", "123-", "12-3", "1-3-", "123-", "1321")});
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.DEPTH_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "32--", "321-", "32-1", "31--", "312-", "31-2", "3-2-", "312-", "3213")});
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.DEPTH_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "12--", "123-", "12-3", "13--", "132-", "13-2", "1-2-", "132-", "1231")});
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.DEPTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "1-3-", "123-", "1-32", "1-2-", "132-", "1-23", "13--", "132-", "1231")});
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.DEPTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "3-2-", "312-", "3-21", "3-1-", "321-", "3-12", "32--", "321-", "3123")});
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.DEPTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "1-2-", "132-", "1-23", "1-3-", "123-", "1-32", "12--", "123-", "1321")});

        // ---------------------------------
        // BRANCH AND BOUND :: Breadth First
        // ---------------------------------
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.BREADTH_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "3---", "2---", "-1--", "-3--", "-2--", "--1-", "--3-", "--2-", "----")});
        col.add(new Object[]{
                ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.BREADTH_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "2---", "1---", "-3--", "-2--", "-1--", "--3-", "--2-", "--1-", "----")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.BREADTH_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "2---", "3---", "-1--", "-2--", "-3--", "--1-", "--2-", "--3-", "----")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.BREADTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "3---", "2---", "--1-", "--3-", "--2-", "-1--", "-3--", "-2--", "----")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.BREADTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "2---", "1---", "--3-", "--2-", "--1-", "-3--", "-2--", "-1--", "----")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.BREADTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "2---", "3---", "--1-", "--2-", "--3-", "-1--", "-2--", "-3--", "----")});

        // -------------------------------
        // BRANCH AND BOUND :: Score First
        // -------------------------------
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.SCORE_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "13--", "132-", "13-2", "12--", "123-", "12-3", "1-3-", "123-", "1321")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.SCORE_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "32--", "321-", "32-1", "31--", "312-", "31-2", "3-2-", "312-", "3213")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.SCORE_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "12--", "123-", "12-3", "13--", "132-", "13-2", "1-2-", "132-", "1231")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.SCORE_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "1-3-", "123-", "1-32", "1-2-", "132-", "1-23", "13--", "132-", "1231")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.SCORE_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "3-2-", "312-", "3-21", "3-1-", "321-", "3-12", "32--", "321-", "3123")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.SCORE_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "1-2-", "132-", "1-23", "1-3-", "123-", "1-32", "12--", "123-", "1321")});
        // ------------------------------------------
        // BRANCH AND BOUND :: Optimistic Bound First
        // ------------------------------------------
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.OPTIMISTIC_BOUND_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "13--", "132-", "13-2", "12--", "123-", "12-3", "1-3-", "123-", "1321")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.OPTIMISTIC_BOUND_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "32--", "321-", "32-1", "31--", "312-", "31-2", "3-2-", "312-", "3213")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.OPTIMISTIC_BOUND_FIRST,
                EntitySorterManner.NONE,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "12--", "123-", "12-3", "13--", "132-", "13-2", "1-2-", "132-", "1231")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.OPTIMISTIC_BOUND_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "1-3-", "123-", "1-32", "1-2-", "132-", "1-23", "13--", "132-", "1231")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.OPTIMISTIC_BOUND_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "3-2-", "312-", "3-21", "3-1-", "321-", "3-12", "32--", "321-", "3123")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.OPTIMISTIC_BOUND_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "1-2-", "132-", "1-23", "1-3-", "123-", "1-32", "12--", "123-", "1321")});

        // ----------------------------------
        // BRANCH AND BOUND :: Original Order
        // ----------------------------------
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.ORIGINAL_ORDER,
                EntitySorterManner.NONE,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "11--", "111-", "113-", "13--", "132-", "13-2", "12--", "123-", "1132")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.ORIGINAL_ORDER,
                EntitySorterManner.NONE,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "33--", "333-", "332-", "32--", "321-", "32-1", "31--", "312-", "3321")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.ORIGINAL_ORDER,
                EntitySorterManner.NONE,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "11--", "111-", "112-", "12--", "123-", "12-3", "13--", "132-", "1123")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.ORIGINAL_ORDER,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "1-1-", "111-", "131-", "1-3-", "123-", "1-32", "1-2-", "132-", "1312")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.ORIGINAL_ORDER,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "3-3-", "333-", "323-", "3-2-", "312-", "3-21", "3-1-", "321-", "3231")});
        col.add(new Object[]{ExhaustiveSearchType.BRANCH_AND_BOUND,
                NodeExplorationType.ORIGINAL_ORDER,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "1-1-", "111-", "121-", "1-2-", "132-", "1-23", "1-3-", "123-", "1213")});
        // -----------
        // BRUTE FORCE
        // -----------
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                null,
                EntitySorterManner.NONE,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "11--", "111-", "113-", "112-", "11-1", "11-3", "11-2", "13--", "1132")});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                null,
                EntitySorterManner.NONE,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "33--", "333-", "332-", "331-", "33-3", "33-2", "33-1", "32--", "3321")});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                null,
                EntitySorterManner.NONE,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "11--", "111-", "112-", "113-", "11-1", "11-2", "11-3", "12--", "1123")});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                null,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.NONE,
                Arrays.asList("----", "1---", "1-1-", "111-", "131-", "121-", "1-11", "1-13", "1-12", "1-3-", "1312")});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                null,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                Arrays.asList("----", "3---", "3-3-", "333-", "323-", "313-", "3-33", "3-32", "3-31", "3-2-", "3231")});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                null,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.INCREASING_STRENGTH,
                Arrays.asList("----", "1---", "1-1-", "111-", "121-", "131-", "1-11", "1-12", "1-13", "1-2-", "1213")});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                NodeExplorationType.DEPTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                null});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                NodeExplorationType.BREADTH_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                null});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                NodeExplorationType.SCORE_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                null});
        col.add(new Object[]{ExhaustiveSearchType.BRUTE_FORCE,
                NodeExplorationType.OPTIMISTIC_BOUND_FIRST,
                EntitySorterManner.DECREASING_DIFFICULTY,
                ValueSorterManner.DECREASING_STRENGTH,
                null});

        return col;
    }

    @Before
    public void setUp() {
        solverConfig = PlannerTestUtils.buildSolverConfig(
                TestdataComparableSolution.class, TestdataComparableEntity.class);

        EntitySelectorConfig entitySelectorConfig = new EntitySelectorConfig();
        entitySelectorConfig.setSelectionOrder(SelectionOrder.SORTED);
        entitySelectorConfig.setCacheType(SelectionCacheType.PHASE);
        entitySelectorConfig.setSorterManner(entitySorterManner);

        ValueSelectorConfig valueSelectorConfig = new ValueSelectorConfig();
        valueSelectorConfig.setSelectionOrder(SelectionOrder.SORTED);
        valueSelectorConfig.setCacheType(SelectionCacheType.PHASE);
        valueSelectorConfig.setSorterManner(valueSorterManner);

        ChangeMoveSelectorConfig moveSelectorConfig = new ChangeMoveSelectorConfig();
        moveSelectorConfig.setEntitySelectorConfig(entitySelectorConfig);
        moveSelectorConfig.setValueSelectorConfig(valueSelectorConfig);

        ExhaustiveSearchPhaseConfig exhaustiveSearchPhaseConfig = new ExhaustiveSearchPhaseConfig();
        exhaustiveSearchPhaseConfig.setExhaustiveSearchType(exhaustiveSearchType);
        exhaustiveSearchPhaseConfig.setNodeExplorationType(nodeExplorationType);
        exhaustiveSearchPhaseConfig.setMoveSelectorConfig(moveSelectorConfig);
        exhaustiveSearchPhaseConfig.setTerminationConfig(new TerminationConfig().withStepCountLimit(10));

        solverConfig.setPhaseConfigList(Collections.singletonList(exhaustiveSearchPhaseConfig));
        solverConfig.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig()
                                                           .withEasyScoreCalculatorClass(TestdataComparableDifferentValuesCalculator.class)
                                                           .withInitializingScoreTrend("ONLY_DOWN"));

        solution = new TestdataComparableSolution("solution");
        // Intentionally not sorted, the string is used for sorting in cases it applies.
        solution.setEntityList(Arrays.asList(new TestdataComparableEntity("entity4"),
                                             new TestdataComparableEntity("entity2"),
                                             new TestdataComparableEntity("entity3"),
                                             new TestdataComparableEntity("entity1")));
        solution.setValueList(Arrays.asList(new TestdataComparableValue("1"),
                                            new TestdataComparableValue("3"),
                                            new TestdataComparableValue("2")));
    }

    @Test
    public void verifyExhaustiveSearchSteps() {
        SolverFactory<TestdataComparableSolution> solverFactory = SolverFactory.create(solverConfig);

        if (exhaustiveSearchType == ExhaustiveSearchType.BRUTE_FORCE && nodeExplorationType != null) {
            Assertions.assertThatIllegalArgumentException()
                    .isThrownBy(solverFactory::buildSolver)
                    .withMessage("The phaseConfig (ExhaustiveSearchPhaseConfig) has an "
                                         + "nodeExplorationType (" + nodeExplorationType.name()
                                         + ") which is not compatible with its exhaustiveSearchType (BRUTE_FORCE).");
        } else {
            Solver<TestdataComparableSolution> solver = solverFactory.buildSolver();

            ExhaustiveSearchListener listener = new ExhaustiveSearchListener();
            ((DefaultSolver<TestdataComparableSolution>) solver).addPhaseLifecycleListener(listener);

            solver.solve(solution);

            assertThat(listener.getTestdataConfigurations()).containsExactlyElementsOf(steps);
        }
    }
}
