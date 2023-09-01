/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.list;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonListInverseVariableDemand;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class RandomSubListSelector<Solution_> extends AbstractSelector<Solution_> implements SubListSelector<Solution_> {

    private final EntitySelector<Solution_> entitySelector;
    private final EntityIndependentValueSelector<Solution_> valueSelector;
    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final int minimumSubListSize;
    private final int maximumSubListSize;

    private TriangleElementFactory triangleElementFactory;
    private SingletonInverseVariableSupply inverseVariableSupply;

    public RandomSubListSelector(
            EntitySelector<Solution_> entitySelector,
            EntityIndependentValueSelector<Solution_> valueSelector,
            int minimumSubListSize, int maximumSubListSize) {
        this.entitySelector = entitySelector;
        this.valueSelector = valueSelector;
        this.listVariableDescriptor = (ListVariableDescriptor<Solution_>) valueSelector.getVariableDescriptor();
        if (minimumSubListSize < 1) {
            throw new IllegalArgumentException(
                    "The minimumSubListSize (" + minimumSubListSize + ") must be greater than 0.");
        }
        if (minimumSubListSize > maximumSubListSize) {
            throw new IllegalArgumentException("The minimumSubListSize (" + minimumSubListSize
                    + ") must be less than or equal to the maximumSubListSize (" + maximumSubListSize + ").");
        }
        this.minimumSubListSize = minimumSubListSize;
        this.maximumSubListSize = maximumSubListSize;

        phaseLifecycleSupport.addEventListener(entitySelector);
        phaseLifecycleSupport.addEventListener(valueSelector);
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        triangleElementFactory = new TriangleElementFactory(minimumSubListSize, maximumSubListSize, workingRandom);
        inverseVariableSupply = solverScope.getScoreDirector().getSupplyManager()
                .demand(new SingletonListInverseVariableDemand<>(listVariableDescriptor));
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        inverseVariableSupply = null;
    }

    @Override
    public ListVariableDescriptor<Solution_> getVariableDescriptor() {
        return listVariableDescriptor;
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
        long subListCount = 0;
        for (Object entity : ((Iterable<Object>) entitySelector::endingIterator)) {
            int listSize = listVariableDescriptor.getListSize(entity);
            // Add subLists bigger than minimum subList size.
            if (listSize >= minimumSubListSize) {
                subListCount += TriangularNumbers.nthTriangle(listSize - minimumSubListSize + 1);
                // Subtract moves with subLists bigger than maximum subList size.
                if (listSize > maximumSubListSize) {
                    subListCount -= TriangularNumbers.nthTriangle(listSize - maximumSubListSize);
                }
            }
        }
        return subListCount;
    }

    @Override
    public Iterator<Object> endingValueIterator() {
        // Child value selector is entity independent, so passing null entity is OK.
        return valueSelector.endingIterator(null);
    }

    @Override
    public long getValueCount() {
        return valueSelector.getSize();
    }

    @Override
    public Iterator<SubList> iterator() {
        // TODO make this incremental https://issues.redhat.com/browse/PLANNER-2507
        int biggestListSize = 0;
        for (Object entity : ((Iterable<Object>) entitySelector::endingIterator)) {
            biggestListSize = Math.max(biggestListSize, listVariableDescriptor.getListSize(entity));
        }
        if (biggestListSize < minimumSubListSize) {
            return new UpcomingSelectionIterator<>() {
                @Override
                protected SubList createUpcomingSelection() {
                    return noUpcomingSelection();
                }
            };
        }
        return new RandomSubListIterator(valueSelector.iterator());
    }

    private final class RandomSubListIterator extends UpcomingSelectionIterator<SubList> {

        private final Iterator<Object> valueIterator;

        private RandomSubListIterator(Iterator<Object> valueIterator) {
            this.valueIterator = valueIterator;
        }

        @Override
        protected SubList createUpcomingSelection() {
            Object sourceEntity = null;
            int listSize = 0;

            while (listSize < minimumSubListSize) {
                if (!valueIterator.hasNext()) {
                    throw new IllegalStateException("The valueIterator (" + valueIterator + ") should never end.");
                }
                // Using valueSelector instead of entitySelector is more fair because entities with bigger list variables
                // will be selected more often.
                sourceEntity = inverseVariableSupply.getInverseSingleton(valueIterator.next());
                listSize = listVariableDescriptor.getListSize(sourceEntity);
            }

            TriangleElementFactory.TriangleElement triangleElement = triangleElementFactory.nextElement(listSize);
            int subListLength = listSize - triangleElement.getLevel() + 1;
            int sourceIndex = triangleElement.getIndexOnLevel() - 1;

            return new SubList(sourceEntity, sourceIndex, subListLength);
        }
    }

    public int getMinimumSubListSize() {
        return minimumSubListSize;
    }

    public int getMaximumSubListSize() {
        return maximumSubListSize;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + valueSelector + ")";
    }
}
