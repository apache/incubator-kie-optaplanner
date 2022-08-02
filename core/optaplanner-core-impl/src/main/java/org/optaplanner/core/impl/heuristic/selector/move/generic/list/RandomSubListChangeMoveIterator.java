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
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.TriangleElementFactory.TriangleElement;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.util.Pair;

class RandomSubListChangeMoveIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final SingletonInverseVariableSupply inverseVariableSupply;
    private final Iterator<Object> valueIterator;
    private final TriangleElementFactory triangleElementFactory;
    private final Random workingRandom;
    private final NavigableMap<Integer, Object> indexToDestinationEntityMap;
    private final int destinationIndexRange;

    RandomSubListChangeMoveIterator(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            EntityIndependentValueSelector<Solution_> valueSelector,
            EntitySelector<Solution_> entitySelector,
            int minimumSubListSize, int maximumSubListSize,
            Random workingRandom) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.inverseVariableSupply = inverseVariableSupply;
        this.valueIterator = valueSelector.iterator();
        this.workingRandom = workingRandom;
        this.triangleElementFactory = new TriangleElementFactory(minimumSubListSize, maximumSubListSize, workingRandom);

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

        TriangleElement triangleElement = triangleElementFactory.nextElement(listSize);
        int subListLength = listSize - triangleElement.getLevel() + 1;
        int sourceIndex = triangleElement.getIndexOnLevel() - 1;

        Pair<Object, Integer> destination = entityAndIndexFromGlobalIndex(workingRandom.nextInt(destinationIndexRange));

        return new SubListChangeMove<>(listVariableDescriptor, sourceEntity, sourceIndex, subListLength, destination.getKey(),
                destination.getValue());
    }

    Pair<Object, Integer> entityAndIndexFromGlobalIndex(int index) {
        Map.Entry<Integer, Object> entry = indexToDestinationEntityMap.floorEntry(index);
        return Pair.of(entry.getValue(), index - entry.getKey());
    }
}
