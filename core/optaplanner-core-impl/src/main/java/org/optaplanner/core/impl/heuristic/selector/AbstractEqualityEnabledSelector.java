package org.optaplanner.core.impl.heuristic.selector;

/**
 * It is expected that if two instances share the same properties,
 * they are {@link Object#equals(Object) equal} to one another.
 * This is necessary for proper performance of caches, such as pillar cache or nearby distance matrix cache.
 *
 * @param <Solution_>
 */
public abstract class AbstractEqualityEnabledSelector<Solution_> extends AbstractSelector<Solution_> {

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

}
