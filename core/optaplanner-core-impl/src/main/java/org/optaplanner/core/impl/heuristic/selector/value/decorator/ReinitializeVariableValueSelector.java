package org.optaplanner.core.impl.heuristic.selector.value.decorator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.AbstractDemandEnabledSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

/**
 * Prevents reassigning of already initialized variables during Construction Heuristics and Exhaustive Search.
 * <p>
 * Returns no values for an entity's variable if the variable is not reinitializable.
 * <p>
 * Does not implement {@link EntityIndependentValueSelector} because if used like that,
 * it shouldn't be added during configuration in the first place.
 */
public final class ReinitializeVariableValueSelector<Solution_>
        extends AbstractDemandEnabledSelector<Solution_>
        implements ValueSelector<Solution_> {

    private final ValueSelector<Solution_> childValueSelector;

    public ReinitializeVariableValueSelector(ValueSelector<Solution_> childValueSelector) {
        this.childValueSelector = childValueSelector;
        phaseLifecycleSupport.addEventListener(childValueSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public GenuineVariableDescriptor<Solution_> getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    @Override
    public boolean isCountable() {
        return childValueSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return childValueSelector.isNeverEnding();
    }

    @Override
    public long getSize(Object entity) {
        if (isReinitializable(entity)) {
            return childValueSelector.getSize(entity);
        }
        return 0L;
    }

    @Override
    public Iterator<Object> iterator(Object entity) {
        if (isReinitializable(entity)) {
            return childValueSelector.iterator(entity);
        }
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Object> endingIterator(Object entity) {
        if (isReinitializable(entity)) {
            return childValueSelector.endingIterator(entity);
        }
        return Collections.emptyIterator();
    }

    private boolean isReinitializable(Object entity) {
        return childValueSelector.getVariableDescriptor().isReinitializable(entity);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        ReinitializeVariableValueSelector<?> that = (ReinitializeVariableValueSelector<?>) other;
        return Objects.equals(childValueSelector, that.childValueSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childValueSelector);
    }

    @Override
    public String toString() {
        return "Reinitialize(" + childValueSelector + ")";
    }

}
