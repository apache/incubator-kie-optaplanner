package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Objects;

import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.common.nearby.NearbySelectionConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.AbstractFromConfigFactory;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandomFactory;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.MimicRecordingSubListSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.MimicReplayingSubListSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.SubListMimicRecorder;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelectorFactory;

public final class SubListSelectorFactory<Solution_> extends AbstractFromConfigFactory<Solution_, SubListSelectorConfig> {

    private static final int DEFAULT_MINIMUM_SUB_LIST_SIZE = 1;
    private static final int DEFAULT_MAXIMUM_SUB_LIST_SIZE = Integer.MAX_VALUE;

    private SubListSelectorFactory(SubListSelectorConfig config) {
        super(config);
    }

    public static <Solution_> SubListSelectorFactory<Solution_> create(SubListSelectorConfig subListSelectorConfig) {
        return new SubListSelectorFactory<>(subListSelectorConfig);
    }

    public SubListSelector<Solution_> buildSubListSelector(
            HeuristicConfigPolicy<Solution_> configPolicy,
            EntitySelector<Solution_> entitySelector,
            SelectionCacheType minimumCacheType,
            SelectionOrder inheritedSelectionOrder) {
        if (config.getMimicSelectorRef() != null) {
            return buildMimicReplaying(configPolicy);
        }
        if (inheritedSelectionOrder != SelectionOrder.RANDOM) {
            throw new IllegalArgumentException("The subListSelector (" + config
                    + ") has an inheritedSelectionOrder(" + inheritedSelectionOrder
                    + ") which is not supported. SubListSelector only supports random selection order.");
        }

        EntityIndependentValueSelector<Solution_> valueSelector = buildEntityIndependentValueSelector(configPolicy,
                entitySelector.getEntityDescriptor(), minimumCacheType, inheritedSelectionOrder);

        // TODO move this to constructor (all list move selectors)
        ListVariableDescriptor<Solution_> listVariableDescriptor =
                (ListVariableDescriptor<Solution_>) valueSelector.getVariableDescriptor();

        int minimumSubListSize = Objects.requireNonNullElse(config.getMinimumSubListSize(), DEFAULT_MINIMUM_SUB_LIST_SIZE);
        int maximumSubListSize = Objects.requireNonNullElse(config.getMaximumSubListSize(), DEFAULT_MAXIMUM_SUB_LIST_SIZE);
        RandomSubListSelector<Solution_> baseSubListSelector =
                new RandomSubListSelector<>(listVariableDescriptor, entitySelector, valueSelector,
                        minimumSubListSize, maximumSubListSize);

        SubListSelector<Solution_> subListSelector =
                applyNearbySelection(configPolicy, minimumCacheType, inheritedSelectionOrder, baseSubListSelector);

        subListSelector = applyMimicRecording(configPolicy, subListSelector);

        return subListSelector;
    }

    SubListSelector<Solution_> buildMimicReplaying(HeuristicConfigPolicy<Solution_> configPolicy) {
        if (config.getId() != null
                || config.getMinimumSubListSize() != null
                || config.getMaximumSubListSize() != null
                || config.getValueSelectorConfig() != null
                || config.getNearbySelectionConfig() != null) {
            throw new IllegalArgumentException("The subListSelectorConfig (" + config
                    + ") with mimicSelectorRef (" + config.getMimicSelectorRef()
                    + ") has another property that is not null.");
        }
        SubListMimicRecorder<Solution_> subListMimicRecorder =
                configPolicy.getSubListMimicRecorder(config.getMimicSelectorRef());
        if (subListMimicRecorder == null) {
            throw new IllegalArgumentException("The subListSelectorConfig (" + config
                    + ") has a mimicSelectorRef (" + config.getMimicSelectorRef()
                    + ") for which no subListSelector with that id exists (in its solver phase).");
        }
        return new MimicReplayingSubListSelector<>(subListMimicRecorder);
    }

    private SubListSelector<Solution_> applyMimicRecording(HeuristicConfigPolicy<Solution_> configPolicy,
            SubListSelector<Solution_> subListSelector) {
        if (config.getId() != null) {
            if (config.getId().isEmpty()) {
                throw new IllegalArgumentException("The subListSelectorConfig (" + config
                        + ") has an empty id (" + config.getId() + ").");
            }
            MimicRecordingSubListSelector<Solution_> mimicRecordingSubListSelector =
                    new MimicRecordingSubListSelector<>(subListSelector);
            configPolicy.addSubListMimicRecorder(config.getId(), mimicRecordingSubListSelector);
            subListSelector = mimicRecordingSubListSelector;
        }
        return subListSelector;
    }

    private SubListSelector<Solution_> applyNearbySelection(
            HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType,
            SelectionOrder resolvedSelectionOrder,
            RandomSubListSelector<Solution_> subListSelector) {
        NearbySelectionConfig nearbySelectionConfig = config.getNearbySelectionConfig();
        if (nearbySelectionConfig == null) {
            return subListSelector;
        }

        nearbySelectionConfig.validateNearby(minimumCacheType, resolvedSelectionOrder);

        boolean randomSelection = resolvedSelectionOrder.toRandomSelectionBoolean();

        NearbyDistanceMeter<?, ?> nearbyDistanceMeter =
                configPolicy.getClassInstanceCache().newInstance(nearbySelectionConfig,
                        "nearbyDistanceMeterClass", nearbySelectionConfig.getNearbyDistanceMeterClass());
        // TODO Check nearbyDistanceMeterClass.getGenericInterfaces() to confirm generic type S is an entityClass
        NearbyRandom nearbyRandom = NearbyRandomFactory.create(nearbySelectionConfig).buildNearbyRandom(randomSelection);

        if (nearbySelectionConfig.getOriginSubListSelectorConfig() == null) {
            throw new IllegalStateException("TODO");
        }
        SubListSelector<Solution_> replayingOriginSubListSelector = SubListSelectorFactory
                .<Solution_> create(nearbySelectionConfig.getOriginSubListSelectorConfig())
                // Entity selector not needed for replaying selector.
                .buildSubListSelector(configPolicy, null, minimumCacheType, resolvedSelectionOrder);
        return new NearSubListNearbySubListSelector<>(
                subListSelector,
                replayingOriginSubListSelector,
                nearbyDistanceMeter,
                nearbyRandom);
    }

    private EntityIndependentValueSelector<Solution_> buildEntityIndependentValueSelector(
            HeuristicConfigPolicy<Solution_> configPolicy, EntityDescriptor<Solution_> entityDescriptor,
            SelectionCacheType minimumCacheType, SelectionOrder inheritedSelectionOrder) {
        ValueSelectorConfig valueSelectorConfig =
                Objects.requireNonNullElseGet(config.getValueSelectorConfig(), ValueSelectorConfig::new);
        ValueSelector<Solution_> valueSelector = ValueSelectorFactory
                .<Solution_> create(valueSelectorConfig)
                .buildValueSelector(configPolicy, entityDescriptor, minimumCacheType, inheritedSelectionOrder);
        if (!(valueSelector instanceof EntityIndependentValueSelector)) {
            throw new IllegalArgumentException("The subListSelector (" + config
                    + ") for a list variable needs to be based on an "
                    + EntityIndependentValueSelector.class.getSimpleName() + " (" + valueSelector + ")."
                    + " Check your @" + ValueRangeProvider.class.getSimpleName() + " annotations.");

        }
        return (EntityIndependentValueSelector<Solution_>) valueSelector;
    }
}
