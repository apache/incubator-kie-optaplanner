package org.optaplanner.core.impl.heuristic.selector.value.chained;

import java.util.List;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.util.CollectionUtils;

/**
 * A subList out of a single chain.
 * <p>
 * Never includes an anchor.
 */
public class SubChain {

    private final List<Object> entityList;

    public SubChain(List<Object> entityList) {
        this.entityList = entityList;
    }

    public List<Object> getEntityList() {
        return entityList;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public Object getFirstEntity() {
        if (entityList.isEmpty()) {
            return null;
        }
        return entityList.get(0);
    }

    public Object getLastEntity() {
        if (entityList.isEmpty()) {
            return null;
        }
        return entityList.get(entityList.size() - 1);
    }

    public int getSize() {
        return entityList.size();
    }

    public SubChain reverse() {
        return new SubChain(CollectionUtils.copy(entityList, true));
    }

    public SubChain subChain(int fromIndex, int toIndex) {
        return new SubChain(entityList.subList(fromIndex, toIndex));
    }

    public SubChain rebase(ScoreDirector<?> destinationScoreDirector) {
        return new SubChain(AbstractMove.rebaseList(entityList, destinationScoreDirector));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SubChain) {
            SubChain other = (SubChain) o;
            return entityList.equals(other.entityList);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return entityList.hashCode();
    }

    @Override
    public String toString() {
        return entityList.toString();
    }

    public String toDottedString() {
        return "[" + getFirstEntity() + ".." + getLastEntity() + "]";
    }

}
