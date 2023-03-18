package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.impl.heuristic.HeuristicConfigPolicyTestUtils.buildHeuristicConfigPolicy;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSwapMoveSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;

class SubListSwapMoveSelectorFactoryTest {

    @Test
    void buildBaseMoveSelector() {
        SubListSwapMoveSelectorConfig config = new SubListSwapMoveSelectorConfig();
        SubListSwapMoveSelectorFactory<TestdataListSolution> factory =
                new SubListSwapMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor());

        RandomSubListSwapMoveSelector<TestdataListSolution> selector =
                (RandomSubListSwapMoveSelector<TestdataListSolution>) factory.buildBaseMoveSelector(heuristicConfigPolicy,
                        SelectionCacheType.JUST_IN_TIME, true);

        assertThat(selector.isCountable()).isTrue();
        assertThat(selector.isNeverEnding()).isTrue();
        assertThat(selector.isSelectReversingMoveToo()).isTrue();
    }

    @Test
    void disableSelectReversingMoveToo() {
        SubListSwapMoveSelectorConfig config = new SubListSwapMoveSelectorConfig();
        config.setSelectReversingMoveToo(false);
        SubListSwapMoveSelectorFactory<TestdataListSolution> factory =
                new SubListSwapMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor());

        RandomSubListSwapMoveSelector<TestdataListSolution> selector =
                (RandomSubListSwapMoveSelector<TestdataListSolution>) factory.buildBaseMoveSelector(heuristicConfigPolicy,
                        SelectionCacheType.JUST_IN_TIME, true);

        assertThat(selector.isSelectReversingMoveToo()).isFalse();
    }
}
