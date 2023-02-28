package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.common.nearby.NearbySelectionConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.DestinationSelectorConfig;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandomFactory;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelectorFactory;

public final class DestinationSelectorFactory<Solution_> extends AbstractSelectorFactory<Solution_, DestinationSelectorConfig> {

    public static <Solution_> DestinationSelectorFactory<Solution_>
            create(DestinationSelectorConfig destinationSelectorConfig) {
        return new DestinationSelectorFactory<>(destinationSelectorConfig);
    }

    private DestinationSelectorFactory(DestinationSelectorConfig destinationSelectorConfig) {
        super(destinationSelectorConfig);
    }

    public DestinationSelector<Solution_> buildDestinationSelector(
            HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType,
            boolean randomSelection) {
        SelectionOrder selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);

        ElementDestinationSelector<Solution_> baseDestinationSelector =
                buildBaseDestinationSelector(configPolicy, minimumCacheType, selectionOrder);

        DestinationSelector<Solution_> destinationSelector =
                applyNearbySelection(configPolicy, minimumCacheType, selectionOrder, baseDestinationSelector);

        return destinationSelector;
    }

    private ElementDestinationSelector<Solution_> buildBaseDestinationSelector(
            HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType,
            SelectionOrder selectionOrder) {
        EntitySelector<Solution_> entitySelector = EntitySelectorFactory
                .<Solution_> create(config.getEntitySelectorConfig())
                .buildEntitySelector(configPolicy, minimumCacheType, selectionOrder);

        ValueSelector<Solution_> valueSelector = ValueSelectorFactory
                .<Solution_> create(config.getValueSelectorConfig())
                .buildValueSelector(configPolicy, entitySelector.getEntityDescriptor(), minimumCacheType, selectionOrder,
                        // Do not override reinitializeVariableFilterEnabled.
                        configPolicy.isReinitializeVariableFilterEnabled(),
                        /*
                         * Filter assigned values (but only if this filtering type is allowed by the configPolicy).
                         *
                         * The destination selector requires the child value selector to only select assigned values.
                         * To guarantee this during CH, where not all values are assigned, the UnassignedValueSelector filter
                         * must be applied.
                         *
                         * In the LS phase, not only is the filter redundant because there are no unassigned values,
                         * but it would also crash if the base value selector inherits random selection order,
                         * because the filter cannot work on a never-ending child value selector.
                         * Therefore, it must not be applied even though it is requested here. This is accomplished by
                         * the configPolicy that only allows this filtering type in the CH phase.
                         */
                        ValueSelectorFactory.ListValueFilteringType.ACCEPT_ASSIGNED);

        // TODO move this to constructor (all list move selectors)
        ListVariableDescriptor<Solution_> listVariableDescriptor =
                (ListVariableDescriptor<Solution_>) valueSelector.getVariableDescriptor();

        return new ElementDestinationSelector<>(
                listVariableDescriptor,
                entitySelector,
                ((EntityIndependentValueSelector<Solution_>) valueSelector),
                selectionOrder.toRandomSelectionBoolean());
    }

    private DestinationSelector<Solution_> applyNearbySelection(
            HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType,
            SelectionOrder resolvedSelectionOrder,
            ElementDestinationSelector<Solution_> destinationSelector) {
        NearbySelectionConfig nearbySelectionConfig = config.getNearbySelectionConfig();
        if (nearbySelectionConfig == null) {
            return destinationSelector;
        }

        nearbySelectionConfig.validateNearby(minimumCacheType, resolvedSelectionOrder);

        boolean randomSelection = resolvedSelectionOrder.toRandomSelectionBoolean();

        ValueSelector<Solution_> originValueSelector = ValueSelectorFactory
                .<Solution_> create(nearbySelectionConfig.getOriginValueSelectorConfig())
                .buildValueSelector(configPolicy, destinationSelector.getEntityDescriptor(), minimumCacheType,
                        resolvedSelectionOrder);

        NearbyDistanceMeter<?, ?> nearbyDistanceMeter =
                configPolicy.getClassInstanceCache().newInstance(nearbySelectionConfig,
                        "nearbyDistanceMeterClass", nearbySelectionConfig.getNearbyDistanceMeterClass());
        // TODO Check nearbyDistanceMeterClass.getGenericInterfaces() to confirm generic type S is an entityClass
        NearbyRandom nearbyRandom = NearbyRandomFactory.create(nearbySelectionConfig).buildNearbyRandom(randomSelection);
        return new NearValueNearbyDestinationSelector<>(
                destinationSelector,
                ((EntityIndependentValueSelector<Solution_>) originValueSelector),
                nearbyDistanceMeter,
                nearbyRandom,
                randomSelection);
    }
}
