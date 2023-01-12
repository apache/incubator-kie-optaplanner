package org.optaplanner.core.impl.heuristic.selector.entity;

import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;

public abstract class AbstractEntitySelector<Solution_> extends AbstractSelector<Solution_>
        implements EntitySelector<Solution_> {

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

}
