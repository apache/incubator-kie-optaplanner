package org.optaplanner.core.impl.heuristic.selector.entity;

import java.util.Arrays;

import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;

/**
 * Abstract superclass for {@link EntitySelector}.
 *
 * @see EntitySelector
 */
public abstract class AbstractEntitySelector<Solution_> extends AbstractSelector<Solution_>
        implements EntitySelector<Solution_> {

    /**
     * Entity selectors need to implement equality,
     * so that things like pillar cache can work properly.
     * In order to enforce the correct equality checks on all entity selectors,
     * they are required to override this method.
     * {@link #equals(Object)} and {@link #hashCode()} are then implemented here, and made final.
     *
     * @return never null, objects to be used as basis for the equality check
     */
    protected abstract Object[] getEqualityRequirements();

    @Override
    public final boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        AbstractEntitySelector<?> that = (AbstractEntitySelector<?>) other;
        return Arrays.deepEquals(getEqualityRequirements(), that.getEqualityRequirements());
    }

    @Override
    public final int hashCode() {
        return Arrays.deepHashCode(getEqualityRequirements());
    }

}
