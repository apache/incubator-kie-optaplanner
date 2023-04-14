package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.FilteringEntitySelector;

public class NearbyCompositeRuinMoveSelector<Solution_> extends SimpleCompositeRuinMoveSelector<Solution_> {
    private final FilteringEntitySelector<Solution_> rightEntitySelector;

    public NearbyCompositeRuinMoveSelector(EntitySelector<Solution_> leftEntitySelector,
            EntitySelector<Solution_> rightEntitySelector,
            List<GenuineVariableDescriptor<Solution_>> variableDescriptorList,
            Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors, Integer percentageToBeRuined) {
        super(leftEntitySelector, variableDescriptorList, shadowVariableDescriptors, percentageToBeRuined);
        this.rightEntitySelector =
                new FilteringEntitySelector<>(rightEntitySelector, Collections.singletonList(initializedFilter));

        if (leftEntitySelector != rightEntitySelector) {
            phaseLifecycleSupport.addEventListener(rightEntitySelector);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        return super.isCountable() && rightEntitySelector.isCountable();
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        long numberOfRuinableEntities = getSize();
        return new NearbyRuinMoveIterator<>(originEntitySelector, rightEntitySelector,
                variableDescriptorList,
                shadowVariableDescriptors, variableDescriptorToInverseVariableSupply, numberOfRuinableEntities,
                percentageToBeRuined * numberOfRuinableEntities / 100);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + originEntitySelector + ", " + rightEntitySelector + ")";
    }
}
