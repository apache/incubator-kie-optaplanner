package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.optaplanner.core.impl.heuristic.selector.move.generic.list.TriangularNumbers.nthTriangle;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;

public class RandomSubListChangeMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final EntitySelector<Solution_> entitySelector;
    private final EntityIndependentValueSelector<Solution_> valueSelector;
    private final int minimumSubListSize;
    private final int maximumSubListSize;
    private final RandomSubListSelector<Solution_> subListSelector;

    public RandomSubListChangeMoveSelector(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            EntitySelector<Solution_> entitySelector,
            EntityIndependentValueSelector<Solution_> valueSelector,
            int minimumSubListSize, int maximumSubListSize) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.entitySelector = entitySelector;
        this.valueSelector = valueSelector;
        this.minimumSubListSize = minimumSubListSize;
        this.maximumSubListSize = maximumSubListSize;
        subListSelector = new RandomSubListSelector<>(listVariableDescriptor, entitySelector, valueSelector,
                minimumSubListSize, maximumSubListSize);
        phaseLifecycleSupport.addEventListener(entitySelector);
        phaseLifecycleSupport.addEventListener(valueSelector);
        phaseLifecycleSupport.addEventListener(subListSelector);
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        return new RandomSubListChangeMoveIterator<>(listVariableDescriptor, subListSelector, entitySelector, workingRandom);
    }

    @Override
    public boolean isCountable() {
        return true;
    }

    @Override
    public boolean isNeverEnding() {
        return true;
    }

    @Override
    public long getSize() {
        long destinationRange = entitySelector.getSize() + valueSelector.getSize();
        long moveCount = 0;
        for (Object entity : (Iterable<?>) entitySelector::endingIterator) {
            int listSize = listVariableDescriptor.getListSize(entity);
            if (listSize < minimumSubListSize) {
                continue;
            }
            // Add moves with subLists bigger than minimum subList size.
            moveCount += moveCount(destinationRange, listSize, listSize - minimumSubListSize + 1);
            if (listSize > maximumSubListSize) {
                // Subtract moves with subLists bigger than maximum subList size.
                moveCount -= moveCount(destinationRange, listSize, listSize - maximumSubListSize);
            }
        }
        return moveCount;
    }

    private static long moveCount(long destinationRange, int listSize, int n) {
        int triangle = nthTriangle(n);
        int pyramid = triangle * (2 * n + 1) / 3;
        return triangle * (destinationRange - listSize - 2) + pyramid;
    }
}
