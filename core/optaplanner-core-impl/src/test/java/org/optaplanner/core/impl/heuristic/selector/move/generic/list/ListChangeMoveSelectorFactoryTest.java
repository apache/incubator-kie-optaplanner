package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.optaplanner.core.impl.heuristic.HeuristicConfigPolicyTestUtils.buildHeuristicConfigPolicy;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.ListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelectorFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.mixed.TestdataMixedVariablesEntity;
import org.optaplanner.core.impl.testdata.domain.list.mixed.TestdataMixedVariablesSolution;

class ListChangeMoveSelectorFactoryTest {

    @Test
    void noUnfolding() {
        SolutionDescriptor<TestdataListSolution> solutionDescriptor = TestdataListSolution.buildSolutionDescriptor();
        ListChangeMoveSelectorConfig moveSelectorConfig = new ListChangeMoveSelectorConfig()
                .withValueSelectorConfig(new ValueSelectorConfig("valueList"))
                .withEntitySelectorConfig(new EntitySelectorConfig(TestdataListEntity.class));
        MoveSelector<TestdataListSolution> moveSelector =
                MoveSelectorFactory.<TestdataListSolution> create(moveSelectorConfig).buildMoveSelector(
                        buildHeuristicConfigPolicy(solutionDescriptor), SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);
        assertThat(moveSelector).isInstanceOf(ListChangeMoveSelector.class);
    }

    @Test
    void unfoldedSingleListVariable() {
        SolutionDescriptor<TestdataListSolution> solutionDescriptor = TestdataListSolution.buildSolutionDescriptor();
        ListChangeMoveSelectorConfig moveSelectorConfig = new ListChangeMoveSelectorConfig();
        MoveSelector<TestdataListSolution> moveSelector =
                MoveSelectorFactory.<TestdataListSolution> create(moveSelectorConfig).buildMoveSelector(
                        buildHeuristicConfigPolicy(solutionDescriptor), SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);
        assertThat(moveSelector).isInstanceOf(ListChangeMoveSelector.class);
    }

    @Test
    void unfoldingSkipsBasicVariablesGracefully() {
        // TODO is this supposed to fail because mixing is not yet supported?
        SolutionDescriptor<TestdataMixedVariablesSolution> solutionDescriptor =
                TestdataMixedVariablesSolution.buildSolutionDescriptor();
        ListChangeMoveSelectorConfig moveSelectorConfig = new ListChangeMoveSelectorConfig();
        MoveSelector<TestdataMixedVariablesSolution> moveSelector =
                MoveSelectorFactory.<TestdataMixedVariablesSolution> create(moveSelectorConfig).buildMoveSelector(
                        buildHeuristicConfigPolicy(solutionDescriptor), SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);
        assertThat(moveSelector).isInstanceOf(ListChangeMoveSelector.class);
    }

    @Test
    void unfoldingFailsIfThereIsNoListVariable() {
        ListChangeMoveSelectorConfig config = new ListChangeMoveSelectorConfig();
        ListChangeMoveSelectorFactory<TestdataSolution> moveSelectorFactory = new ListChangeMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataSolution.buildSolutionDescriptor());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> moveSelectorFactory.buildMoveSelector(heuristicConfigPolicy,
                        SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM))
                .withMessageContaining("cannot unfold");
    }

    @Test
    void explicitConfigMustUseListVariable() {
        ListChangeMoveSelectorConfig config = new ListChangeMoveSelectorConfig()
                .withValueSelectorConfig(new ValueSelectorConfig("value"))
                .withEntitySelectorConfig(new EntitySelectorConfig(TestdataMixedVariablesEntity.class));

        ListChangeMoveSelectorFactory<TestdataMixedVariablesSolution> moveSelectorFactory =
                new ListChangeMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataMixedVariablesSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataMixedVariablesSolution.buildSolutionDescriptor());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> moveSelectorFactory.buildMoveSelector(heuristicConfigPolicy,
                        SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM))
                .withMessageContaining("not a list planning variable");
    }
}
