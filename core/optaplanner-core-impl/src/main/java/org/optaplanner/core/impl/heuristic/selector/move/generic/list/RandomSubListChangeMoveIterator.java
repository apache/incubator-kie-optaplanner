package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.util.Pair;

class RandomSubListChangeMoveIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final SingletonInverseVariableSupply inverseVariableSupply;
    private final Iterator<Object> valueIterator;
    private final Random workingRandom;
    private final NavigableMap<Integer, Object> indexToDestinationEntityMap;
    private final int destinationIndexRange;

    RandomSubListChangeMoveIterator(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            EntityIndependentValueSelector<Solution_> valueSelector,
            EntitySelector<Solution_> entitySelector,
            Random workingRandom) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.inverseVariableSupply = inverseVariableSupply;
        this.valueIterator = valueSelector.iterator();
        this.workingRandom = workingRandom;

        // TODO optimize this (don't rebuild the whole map at the beginning of each step).
        //  https://issues.redhat.com/browse/PLANNER-2507
        indexToDestinationEntityMap = new TreeMap<>();
        int cumulativeDestinationListSize = 0;
        for (Object entity : ((Iterable<Object>) entitySelector::endingIterator)) {
            indexToDestinationEntityMap.put(cumulativeDestinationListSize, entity);
            cumulativeDestinationListSize += (listVariableDescriptor.getListSize(entity) + 1);
        }
        this.destinationIndexRange = cumulativeDestinationListSize;
    }

    @Override
    protected Move<Solution_> createUpcomingSelection() {
        if (!valueIterator.hasNext() || destinationIndexRange == 0) {
            return noUpcomingSelection();
        }

        Object sourceEntity = inverseVariableSupply.getInverseSingleton(valueIterator.next());
        int listSize = listVariableDescriptor.getListSize(sourceEntity);
        int subListCount = TriangularNumbers.nth(listSize);
        int subListIndex = workingRandom.nextInt(subListCount) + 1; // Triangle elements are indexed from 1.
        TriangleElement triangleElement = TriangleElement.valueOf(subListIndex);
        int length = listSize - triangleElement.nthTriangle + 1;
        int sourceIndex = triangleElement.remainder - 1;

        Pair<Object, Integer> destination = entityAndIndexFromGlobalIndex(workingRandom.nextInt(destinationIndexRange));

        return new SubListChangeMove<>(listVariableDescriptor, sourceEntity, sourceIndex, length, destination.getKey(),
                destination.getValue());
    }

    Pair<Object, Integer> entityAndIndexFromGlobalIndex(int index) {
        Map.Entry<Integer, Object> entry = indexToDestinationEntityMap.floorEntry(index);
        return Pair.of(entry.getValue(), index - entry.getKey());
    }

    static final class TriangleElement {

        private final int index;
        private final int nthTriangle;
        private final int remainder;

        private TriangleElement(int index, int nthTriangle, int remainder) {
            this.index = index;
            this.nthTriangle = nthTriangle;
            this.remainder = remainder;
        }

        static TriangleElement valueOf(int index) {
            int rootCeil = TriangularNumbers.rootCeil(index);
            return new TriangleElement(index, rootCeil, index - TriangularNumbers.nth(rootCeil - 1));
        }

        public int getIndex() {
            return index;
        }

        public int getNthTriangle() {
            return nthTriangle;
        }

        public int getRemainder() {
            return remainder;
        }
    }
}
