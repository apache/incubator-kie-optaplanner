package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSwapMoveSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;

class SubListSwapMoveSelectorFactoryTest {

    @Test
    void buildBaseMoveSelector() {
        SubListSwapMoveSelectorConfig config = new SubListSwapMoveSelectorConfig();
        SubListSwapMoveSelectorFactory<TestdataListSolution> factory = new SubListSwapMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy = mock(HeuristicConfigPolicy.class);
        when(heuristicConfigPolicy.getSolutionDescriptor()).thenReturn(TestdataListSolution.buildSolutionDescriptor());

        RandomSubListSwapMoveSelector<TestdataListSolution> selector =
                (RandomSubListSwapMoveSelector<TestdataListSolution>) factory.buildBaseMoveSelector(heuristicConfigPolicy,
                        SelectionCacheType.JUST_IN_TIME, true);

        assertThat(selector.isCountable()).isTrue();
        assertThat(selector.isNeverEnding()).isTrue();
    }

    @Test
    void requiresListVariable() {
        SubListSwapMoveSelectorConfig config = new SubListSwapMoveSelectorConfig();
        SubListSwapMoveSelectorFactory<TestdataSolution> factory = new SubListSwapMoveSelectorFactory<>(config);

        HeuristicConfigPolicy<TestdataSolution> heuristicConfigPolicy = mock(HeuristicConfigPolicy.class);
        when(heuristicConfigPolicy.getSolutionDescriptor()).thenReturn(TestdataSolution.buildSolutionDescriptor());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> factory.buildBaseMoveSelector(heuristicConfigPolicy, SelectionCacheType.JUST_IN_TIME, true))
                .withMessageContaining("@" + PlanningListVariable.class.getSimpleName());
    }
}
