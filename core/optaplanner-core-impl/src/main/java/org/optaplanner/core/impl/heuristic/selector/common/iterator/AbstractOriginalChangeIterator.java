package org.optaplanner.core.impl.heuristic.selector.common.iterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

public abstract class AbstractOriginalChangeIterator<Solution_, Move_ extends Move<Solution_>>
        extends UpcomingSelectionIterator<Move_> {

    private final ValueSelector<Solution_> valueSelector;

    private final Iterator<Object> entityIterator;
    private Iterator<Object> valueIterator;

    private Object upcomingEntity;

    public AbstractOriginalChangeIterator(EntitySelector<Solution_> entitySelector,
            ValueSelector<Solution_> valueSelector) {
        this.valueSelector = valueSelector;
        entityIterator = entitySelector.iterator();
        // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
        valueIterator = Collections.emptyIterator();
    }

    @Override
    protected Move_ createUpcomingSelection() {
        VariableDescriptor<Solution_> variableDescriptor = valueSelector.getVariableDescriptor();
        while (true) {
            while (!valueIterator.hasNext()) {
                if (!entityIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                upcomingEntity = entityIterator.next();
                valueIterator = valueSelector.iterator(upcomingEntity);
            }
            Object toValue = valueIterator.next();
            Object currentValue = variableDescriptor.getValue(upcomingEntity);
            if (Objects.equals(toValue, currentValue)) { // Don't generate change move that doesn't do anything.
                continue;
            }
            return newChangeSelection(upcomingEntity, toValue);
        }
    }

    protected abstract Move_ newChangeSelection(Object entity, Object toValue);

}
