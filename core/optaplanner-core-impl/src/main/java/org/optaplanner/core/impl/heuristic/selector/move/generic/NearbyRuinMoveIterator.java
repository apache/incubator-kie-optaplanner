package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;

class NearbyRuinMoveIterator<Solution_> extends SimpleRuinMoveIterator<Solution_> {

    private final EntitySelector<Solution_> rightEntitySelector;

    public NearbyRuinMoveIterator(EntitySelector<Solution_> originEntitySelector, EntitySelector<Solution_> rightEntitySelector,
            List<GenuineVariableDescriptor<Solution_>> variableDescriptorList,
            Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors,
            HashMap<GenuineVariableDescriptor<Solution_>, SingletonInverseVariableSupply> variableDescriptorToInverseVariableSupply,
            long numberOfRuinableEntities, long numberOfEntitiesToBeRuined) {
        super(originEntitySelector, variableDescriptorList, shadowVariableDescriptors,
                variableDescriptorToInverseVariableSupply, numberOfRuinableEntities, numberOfEntitiesToBeRuined);
        this.rightEntitySelector = rightEntitySelector;
    }

    @Override
    protected Set<Object> chooseEntitiesToBeRuined() {
        Set<Object> entities = new HashSet<>();
        long checkedEntitiesCounter = 0;
        while (entities.isEmpty() && originIterator.hasNext()) {
            Object originSelection = originIterator.next();
            if (validateSelectionNotNull(originSelection)) {
                entities.add(originSelection);
            }

            if (++checkedEntitiesCounter >= numberOfRuinableEntities) {
                return entities;
            }
        }

        Iterator<Object> rightIterator = rightEntitySelector.iterator();
        checkedEntitiesCounter = 0;
        while (entities.size() < numberOfEntitiesToBeRuined && rightIterator.hasNext()
                && checkedEntitiesCounter < numberOfRuinableEntities) {
            Object nearbySelection = rightIterator.next();
            if (validateSelectionNotNull(nearbySelection)) {
                entities.add(nearbySelection);
            }

            ++checkedEntitiesCounter;
        }
        return entities;
    }
}
