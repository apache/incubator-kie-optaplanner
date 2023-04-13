package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.RuinMoveSelectorConfig;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.AbstractMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;

public class RuinMoveSelectorFactory<Solution_>
        extends AbstractMoveSelectorFactory<Solution_, RuinMoveSelectorConfig> {

    public RuinMoveSelectorFactory(RuinMoveSelectorConfig moveSelectorConfig) {
        super(moveSelectorConfig);
    }

    @Override
    protected MoveSelector<Solution_> buildBaseMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        EntitySelectorConfig entitySelectorConfig =
                Objects.requireNonNullElseGet(config.getEntitySelectorConfig(), EntitySelectorConfig::new);
        EntitySelectorConfig secondaryEntitySelectorConfig =
                Objects.requireNonNullElseGet(config.getSecondaryEntitySelectorConfig(), EntitySelectorConfig::new);

        SelectionOrder selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);
        EntitySelector<Solution_> leftEntitySelector =
                EntitySelectorFactory.<Solution_> create(entitySelectorConfig)
                        .buildEntitySelector(configPolicy, minimumCacheType, selectionOrder);
        EntitySelector<Solution_> rightEntitySelector =
                EntitySelectorFactory.<Solution_> create(secondaryEntitySelectorConfig)
                        .buildEntitySelector(configPolicy, minimumCacheType, selectionOrder);
        EntityDescriptor<Solution_> entityDescriptor = leftEntitySelector.getEntityDescriptor();

        List<GenuineVariableDescriptor<Solution_>> variableDescriptorList =
                deduceVariableDescriptorList(entityDescriptor, config.getVariableNameIncludeList());

        Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptorList =
                deduceShadowVariableDescriptorList(entityDescriptor, config.getVariableNameIncludeList());
        return new CompositeRuinMoveSelector<>(leftEntitySelector, rightEntitySelector, variableDescriptorList,
                shadowVariableDescriptorList, config.getPercentageToRuin());
    }
}
