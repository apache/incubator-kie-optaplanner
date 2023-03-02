package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.optaplanner.core.impl.heuristic.HeuristicConfigPolicyTestUtils.buildHeuristicConfigPolicy;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.DestinationSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.mixed.TestdataMixedVariablesEntity;
import org.optaplanner.core.impl.testdata.domain.list.mixed.TestdataMixedVariablesSolution;

class SubListChangeMoveSelectorFactoryTest {

    @Test
    void buildMoveSelector() {
        SubListChangeMoveSelectorConfig config = new SubListChangeMoveSelectorConfig();
        SubListChangeMoveSelectorFactory<TestdataListSolution> moveSelectorFactory =
                new SubListChangeMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor());

        RandomSubListChangeMoveSelector<TestdataListSolution> selector =
                (RandomSubListChangeMoveSelector<TestdataListSolution>) moveSelectorFactory
                        .buildMoveSelector(heuristicConfigPolicy, SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);

        assertThat(selector.isCountable()).isTrue();
        assertThat(selector.isNeverEnding()).isTrue();
        assertThat(selector.isSelectReversingMoveToo()).isTrue();
    }

    @Test
    void disableSelectReversingMoveToo() {
        SubListChangeMoveSelectorConfig config = new SubListChangeMoveSelectorConfig();
        config.setSelectReversingMoveToo(false);
        SubListChangeMoveSelectorFactory<TestdataListSolution> moveSelectorFactory =
                new SubListChangeMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor());

        RandomSubListChangeMoveSelector<TestdataListSolution> selector =
                (RandomSubListChangeMoveSelector<TestdataListSolution>) moveSelectorFactory
                        .buildMoveSelector(heuristicConfigPolicy, SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);

        assertThat(selector.isSelectReversingMoveToo()).isFalse();
    }

    @Test
    void unfoldingFailsIfThereIsNoListVariable() {
        SubListChangeMoveSelectorConfig config = new SubListChangeMoveSelectorConfig();
        SubListChangeMoveSelectorFactory<TestdataSolution> moveSelectorFactory = new SubListChangeMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataSolution.buildSolutionDescriptor());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> moveSelectorFactory.buildMoveSelector(heuristicConfigPolicy, SelectionCacheType.JUST_IN_TIME,
                        SelectionOrder.RANDOM))
                .withMessageContaining("cannot unfold");
    }

    @Test
    void explicitConfigMustUseListVariable() {
        SubListChangeMoveSelectorConfig config = new SubListChangeMoveSelectorConfig()
                .withSubListSelectorConfig(new SubListSelectorConfig()
                        .withValueSelectorConfig(new ValueSelectorConfig("value")))
                .withDestinationSelectorConfig(new DestinationSelectorConfig()
                        .withEntitySelectorConfig(new EntitySelectorConfig(TestdataMixedVariablesEntity.class))
                        .withValueSelectorConfig(new ValueSelectorConfig("value")));

        SubListChangeMoveSelectorFactory<TestdataMixedVariablesSolution> moveSelectorFactory =
                new SubListChangeMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataMixedVariablesSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataMixedVariablesSolution.buildSolutionDescriptor());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> moveSelectorFactory.buildMoveSelector(heuristicConfigPolicy, SelectionCacheType.JUST_IN_TIME,
                        SelectionOrder.RANDOM))
                .withMessageContaining("not a list planning variable");
    }
}
