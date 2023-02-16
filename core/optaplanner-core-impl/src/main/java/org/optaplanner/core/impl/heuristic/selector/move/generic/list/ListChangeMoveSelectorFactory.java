package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.ListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.AbstractMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelectorFactory;

public class ListChangeMoveSelectorFactory<Solution_>
        extends AbstractMoveSelectorFactory<Solution_, ListChangeMoveSelectorConfig> {

    public ListChangeMoveSelectorFactory(ListChangeMoveSelectorConfig moveSelectorConfig) {
        super(moveSelectorConfig);
    }

    @Override
    protected MoveSelector<Solution_> buildBaseMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        if (config.getEntitySelectorConfig() == null) {
            throw new IllegalStateException("The entitySelectorConfig (" + config.getEntitySelectorConfig()
                    + ") should haven been initialized during unfolding.");
        }
        if (config.getValueSelectorConfig() == null) {
            throw new IllegalStateException("The valueSelectorConfig (" + config.getValueSelectorConfig()
                    + ") should haven been initialized during unfolding.");
        }
        SelectionOrder selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);
        EntitySelector<Solution_> entitySelector = EntitySelectorFactory
                .<Solution_> create(config.getEntitySelectorConfig())
                .buildEntitySelector(configPolicy, minimumCacheType, selectionOrder);
        ValueSelector<Solution_> sourceValueSelector = ValueSelectorFactory
                .<Solution_> create(config.getValueSelectorConfig())
                .buildValueSelector(configPolicy, entitySelector.getEntityDescriptor(), minimumCacheType, selectionOrder);
        // TODO review on all list move selectors (should be impossible because it *should* be checked during unfolding.
        if (!sourceValueSelector.getVariableDescriptor().isListVariable()) {
            throw new IllegalArgumentException("The listChangeMoveSelector (" + config
                    + ") can only be used when the domain model has a list variable."
                    + " Check your @" + PlanningEntity.class.getSimpleName()
                    + " and make sure it has a @" + PlanningListVariable.class.getSimpleName() + ".");
        }
        if (!(sourceValueSelector instanceof EntityIndependentValueSelector)) {
            throw new IllegalArgumentException("The listChangeMoveSelector (" + config
                    + ") for a list variable needs to be based on an "
                    + EntityIndependentValueSelector.class.getSimpleName() + " (" + sourceValueSelector + ")."
                    + " Check your valueSelectorConfig.");
        }

        ValueSelector<Solution_> destinationValueSelector = ValueSelectorFactory
                // This must not be the config.valueSelector because that is the source value selector, which, in CH,
                // is the replaying value selector. But we're building the destination selector here. That must not be replaying.
                // FIXME maybe find a better way to avoid the mistake. Unfolding? DestinationSelectorConfig/Factory?
                .<Solution_> create(new ValueSelectorConfig(config.getValueSelectorConfig().getVariableName()))
                .buildValueSelector(configPolicy, entitySelector.getEntityDescriptor(), minimumCacheType, selectionOrder,
                        // Do not override reinitializeVariableFilterEnabled.
                        configPolicy.isReinitializeVariableFilterEnabled(),
                        /*
                         * Filter assigned values (but only if this filtering type is allowed by the configPolicy).
                         *
                         * The destination selector requires the child value selector to only select assign values.
                         * To guarantee this during CH where not all values are assigned, the UnassignedValueSelector filter
                         * must be applied.
                         *
                         * In the LS phase, not only is the filter redundant because there are no unassigned values,
                         * but it would also crash if the base value selector inherits random selection order,
                         * because the filter cannot work on a never-ending child value selector.
                         * Therefore, it must not be applied even though it is requested here. This is accomplished by
                         * the configPolicy that only allows this filtering type in the CH phase.
                         */
                        ValueSelectorFactory.ListValueFilteringType.ACCEPT_ASSIGNED);

        ListVariableDescriptor<Solution_> listVariableDescriptor =
                (ListVariableDescriptor<Solution_>) sourceValueSelector.getVariableDescriptor();

        ElementDestinationSelector<Solution_> destinationSelector = new ElementDestinationSelector<>(
                listVariableDescriptor,
                entitySelector,
                ((EntityIndependentValueSelector<Solution_>) destinationValueSelector),
                randomSelection);

        return new ListChangeMoveSelector<>(
                listVariableDescriptor,
                (EntityIndependentValueSelector<Solution_>) sourceValueSelector,
                destinationSelector,
                randomSelection);
    }

    @Override
    protected MoveSelectorConfig<?> buildUnfoldedMoveSelectorConfig(HeuristicConfigPolicy<Solution_> configPolicy) {
        Collection<EntityDescriptor<Solution_>> entityDescriptors;
        EntityDescriptor<Solution_> onlyEntityDescriptor = config.getEntitySelectorConfig() == null ? null
                : EntitySelectorFactory.<Solution_> create(config.getEntitySelectorConfig())
                        .extractEntityDescriptor(configPolicy);
        if (onlyEntityDescriptor != null) {
            entityDescriptors = Collections.singletonList(onlyEntityDescriptor);
        } else {
            entityDescriptors = configPolicy.getSolutionDescriptor().getGenuineEntityDescriptors();
        }
        List<ListVariableDescriptor<Solution_>> variableDescriptorList = new ArrayList<>();
        for (EntityDescriptor<Solution_> entityDescriptor : entityDescriptors) {
            GenuineVariableDescriptor<Solution_> onlyVariableDescriptor = config.getValueSelectorConfig() == null ? null
                    : ValueSelectorFactory.<Solution_> create(config.getValueSelectorConfig())
                            .extractVariableDescriptor(configPolicy, entityDescriptor);
            if (onlyVariableDescriptor != null) {
                if (!onlyVariableDescriptor.isListVariable()) {
                    throw new IllegalArgumentException("The listChangeMoveSelector (" + config
                            + ") is configured to use a planning variable (" + onlyVariableDescriptor
                            + "), which is not a list planning variable."
                            + " Either fix your annotations and use a @" + PlanningListVariable.class.getSimpleName()
                            + " on the variable to make it work with listChangeMoveSelector"
                            + " or use a changeMoveSelector instead.");
                }
                if (onlyEntityDescriptor != null) {
                    // No need for unfolding or deducing
                    return null;
                }
                variableDescriptorList.add((ListVariableDescriptor<Solution_>) onlyVariableDescriptor);
            } else {
                variableDescriptorList.addAll(
                        entityDescriptor.getGenuineVariableDescriptorList().stream()
                                .filter(GenuineVariableDescriptor::isListVariable)
                                .map(variableDescriptor -> ((ListVariableDescriptor<Solution_>) variableDescriptor))
                                .collect(Collectors.toList()));

                if (variableDescriptorList.isEmpty()) {
                    throw new IllegalArgumentException("The listChangeMoveSelector (" + config
                            + ") cannot unfold because there are no list planning variables for the entitySelector ("
                            + config.getEntitySelectorConfig()
                            + ") or no list planning variables at all.");
                }
            }
        }
        return buildUnfoldedMoveSelectorConfig(variableDescriptorList);
    }

    protected MoveSelectorConfig<?> buildUnfoldedMoveSelectorConfig(
            List<ListVariableDescriptor<Solution_>> variableDescriptorList) {
        List<MoveSelectorConfig> moveSelectorConfigList = variableDescriptorList.stream()
                .map(variableDescriptor -> buildChildMoveSelectorConfig(
                        variableDescriptor, config.getEntitySelectorConfig(), config.getValueSelectorConfig()))
                .collect(Collectors.toList());

        MoveSelectorConfig unfoldedMoveSelectorConfig;
        if (moveSelectorConfigList.size() == 1) {
            unfoldedMoveSelectorConfig = moveSelectorConfigList.get(0);
        } else {
            unfoldedMoveSelectorConfig = new UnionMoveSelectorConfig(moveSelectorConfigList);
        }
        unfoldedMoveSelectorConfig.inheritFolded(config);
        return unfoldedMoveSelectorConfig;
    }

    public static ListChangeMoveSelectorConfig buildChildMoveSelectorConfig(
            ListVariableDescriptor<?> variableDescriptor,
            EntitySelectorConfig inheritedEntitySelectorConfig,
            ValueSelectorConfig inheritedValueSelectorConfig) {
        // No childMoveSelectorConfig.inherit() because of unfoldedMoveSelectorConfig.inheritFolded()
        ListChangeMoveSelectorConfig childMoveSelectorConfig = new ListChangeMoveSelectorConfig();
        // Different EntitySelector per child because it is a union
        EntitySelectorConfig childEntitySelectorConfig = new EntitySelectorConfig(inheritedEntitySelectorConfig);
        if (childEntitySelectorConfig.getMimicSelectorRef() == null) {
            childEntitySelectorConfig.setEntityClass(variableDescriptor.getEntityDescriptor().getEntityClass());
        }
        childMoveSelectorConfig.setEntitySelectorConfig(childEntitySelectorConfig);
        ValueSelectorConfig childValueSelectorConfig = new ValueSelectorConfig(inheritedValueSelectorConfig);
        if (childValueSelectorConfig.getMimicSelectorRef() == null) {
            childValueSelectorConfig.setVariableName(variableDescriptor.getVariableName());
        }
        childMoveSelectorConfig.setValueSelectorConfig(childValueSelectorConfig);
        return childMoveSelectorConfig;
    }
}
