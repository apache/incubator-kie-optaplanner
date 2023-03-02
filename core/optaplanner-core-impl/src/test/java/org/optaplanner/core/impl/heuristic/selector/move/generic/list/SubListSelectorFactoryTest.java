package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.heuristic.HeuristicConfigPolicyTestUtils.buildHeuristicConfigPolicy;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.MimicRecordingSubListSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.MimicReplayingSubListSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.SubListMimicRecorder;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils;

class SubListSelectorFactoryTest {

    @Test
    void buildSubListSelector() {
        SubListSelectorConfig config = new SubListSelectorConfig()
                .withMinimumSubListSize(2)
                .withMaximumSubListSize(3)
                .withValueSelectorConfig(new ValueSelectorConfig());

        SubListSelectorFactory<TestdataListSolution> selectorFactory = SubListSelectorFactory.create(config);

        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor());

        ListVariableDescriptor<TestdataListSolution> listVariableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();
        EntitySelector<TestdataListSolution> entitySelector = TestdataListUtils.mockEntitySelector();
        when(entitySelector.getEntityDescriptor()).thenReturn(listVariableDescriptor.getEntityDescriptor());

        RandomSubListSelector<TestdataListSolution> subListSelector =
                (RandomSubListSelector<TestdataListSolution>) selectorFactory.buildSubListSelector(heuristicConfigPolicy,
                        entitySelector, SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);

        assertThat(subListSelector.getMinimumSubListSize()).isEqualTo(config.getMinimumSubListSize());
        assertThat(subListSelector.getMaximumSubListSize()).isEqualTo(config.getMaximumSubListSize());
    }

    @Test
    void buildMimicRecordingSelector() {
        SubListSelectorConfig config = new SubListSelectorConfig()
                .withId("someSelectorId")
                .withMinimumSubListSize(3)
                .withMaximumSubListSize(10)
                .withValueSelectorConfig(new ValueSelectorConfig());

        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor());

        ListVariableDescriptor<TestdataListSolution> listVariableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();
        EntitySelector<TestdataListSolution> entitySelector = TestdataListUtils.mockEntitySelector();
        when(entitySelector.getEntityDescriptor()).thenReturn(listVariableDescriptor.getEntityDescriptor());

        SubListSelectorFactory<TestdataListSolution> selectorFactory = SubListSelectorFactory.create(config);
        SubListSelector<TestdataListSolution> subListSelector = selectorFactory.buildSubListSelector(heuristicConfigPolicy,
                entitySelector, SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);

        assertThat(subListSelector).isInstanceOf(MimicRecordingSubListSelector.class);
    }

    @Test
    void buildMimicReplayingSelector() {
        String selectorId = "someSelectorId";
        SubListSelectorConfig config = new SubListSelectorConfig()
                .withMimicSelectorRef(selectorId);

        ListVariableDescriptor<TestdataListSolution> listVariableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();
        EntitySelector<TestdataListSolution> entitySelector = TestdataListUtils.mockEntitySelector();
        when(entitySelector.getEntityDescriptor()).thenReturn(listVariableDescriptor.getEntityDescriptor());

        SubListMimicRecorder<TestdataListSolution> subListMimicRecorder = mock(SubListMimicRecorder.class);
        HeuristicConfigPolicy<TestdataListSolution> heuristicConfigPolicy =
                buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor());
        heuristicConfigPolicy.addSubListMimicRecorder(selectorId, subListMimicRecorder);
        when(subListMimicRecorder.getVariableDescriptor()).thenReturn(listVariableDescriptor);

        SubListSelectorFactory<TestdataListSolution> selectorFactory = SubListSelectorFactory.create(config);
        SubListSelector<TestdataListSolution> subListSelector = selectorFactory.buildSubListSelector(heuristicConfigPolicy,
                entitySelector, SelectionCacheType.JUST_IN_TIME, SelectionOrder.RANDOM);

        assertThat(subListSelector).isInstanceOf(MimicReplayingSubListSelector.class);
    }

    @Test
    void failFast_ifMimicRecordingIsUsedWithOtherProperty() {
        SubListSelectorConfig config = new SubListSelectorConfig()
                .withMaximumSubListSize(10)
                .withMimicSelectorRef("someSelectorId");

        assertThatIllegalArgumentException().isThrownBy(
                () -> SubListSelectorFactory.<TestdataListSolution> create(config)
                        .buildMimicReplaying(buildHeuristicConfigPolicy(TestdataListSolution.buildSolutionDescriptor())))
                .withMessageContaining("has another property");
    }
}
