/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.value.chained;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.optaplanner.core.impl.domain.variable.PlanningVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;
import org.optaplanner.core.impl.heuristic.selector.common.SelectionCacheLifecycleBridge;
import org.optaplanner.core.impl.heuristic.selector.common.SelectionCacheLifecycleListener;
import org.optaplanner.core.impl.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.util.RandomUtils;

/**
 * This is the common {@link SubChainSelector} implementation.
 */
public class DefaultSubChainSelector extends AbstractSelector
        implements SubChainSelector, SelectionCacheLifecycleListener {

    protected static final SelectionCacheType CACHE_TYPE = SelectionCacheType.STEP;

    protected final EntityIndependentValueSelector valueSelector;
    protected final boolean randomSelection;

    protected final int minimumSubChainSize;
    protected final int maximumSubChainSize;

    protected List<SubChain> anchorTrailingChainList = null;

    public DefaultSubChainSelector(EntityIndependentValueSelector valueSelector, boolean randomSelection,
            int minimumSubChainSize, int maximumSubChainSize) {
        this.valueSelector = valueSelector;
        this.randomSelection = randomSelection;
        this.minimumSubChainSize = minimumSubChainSize;
        this.maximumSubChainSize = maximumSubChainSize;
        if (!valueSelector.getVariableDescriptor().isChained()) {
            throw new IllegalArgumentException("The selector (" + this
                    + ")'s valueSelector (" + valueSelector
                    + ") must have a chained variableDescriptor chained ("
                    + valueSelector.getVariableDescriptor().isChained() + ").");
        }
        if (valueSelector.isNeverEnding()) {
            throw new IllegalStateException("The selector (" + this
                    + ") has a valueSelector (" + valueSelector
                    + ") with neverEnding (" + valueSelector.isNeverEnding() + ").");
        }
        solverPhaseLifecycleSupport.addEventListener(valueSelector);
        solverPhaseLifecycleSupport.addEventListener(new SelectionCacheLifecycleBridge(CACHE_TYPE, this));
        if (minimumSubChainSize < 1) {
            throw new IllegalStateException("The selector (" + this
                    + ")'s minimumSubChainSize (" + minimumSubChainSize
                    + ") must be at least 1.");
        }
        if (minimumSubChainSize > maximumSubChainSize) {
            throw new IllegalStateException("The minimumSubChainSize (" + minimumSubChainSize
                    + ") must be at least maximumSubChainSize (" + maximumSubChainSize + ").");
        }
    }

    public PlanningVariableDescriptor getVariableDescriptor() {
        return valueSelector.getVariableDescriptor();
    }

    @Override
    public SelectionCacheType getCacheType() {
        return CACHE_TYPE;
    }

    // ************************************************************************
    // Cache lifecycle methods
    // ************************************************************************

    public void constructCache(DefaultSolverScope solverScope) {
        ScoreDirector scoreDirector = solverScope.getScoreDirector();
        PlanningVariableDescriptor variableDescriptor = valueSelector.getVariableDescriptor();
        Class<?> entityClass = variableDescriptor.getEntityDescriptor().getPlanningEntityClass();
        long valueSize = valueSelector.getSize();
        // Fail-fast when anchorTrailingChainList.size() could ever be too big
        if (valueSize > (long) Integer.MAX_VALUE) {
            throw new IllegalStateException("The selector (" + this
                    + ") has a valueSelector (" + valueSelector
                    + ") with valueSize (" + valueSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        // Temporary LinkedList to avoid using a bad initialCapacity
        List<Object> anchorList = new LinkedList<Object>();
        for (Object value : valueSelector) {
            if (!entityClass.isAssignableFrom(value.getClass())) {
                anchorList.add(value);
            }
        }
        anchorTrailingChainList = new ArrayList<SubChain>(anchorList.size());
        int anchorChainInitialCapacity = ((int) valueSize / anchorList.size()) + 1;
        for (Object anchor : anchorList) {
            List<Object> anchorChain = new ArrayList<Object>(anchorChainInitialCapacity);
            Object trailingEntity = scoreDirector.getTrailingEntity(variableDescriptor, anchor);
            while (trailingEntity != null) {
                anchorChain.add(trailingEntity);
                trailingEntity = scoreDirector.getTrailingEntity(variableDescriptor, trailingEntity);
            }
            if (anchorChain.size() >= minimumSubChainSize) {
                anchorTrailingChainList.add(new SubChain(anchorChain));
            }
        }
    }

    public void disposeCache(DefaultSolverScope solverScope) {
        anchorTrailingChainList = null;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isContinuous() {
        return false;
    }

    public boolean isNeverEnding() {
        return randomSelection;
    }

    public long getSize() {
        long selectionSize = 0L;
        for (SubChain anchorTrailingChain : anchorTrailingChainList) {
            selectionSize += calculateSubChainSelectionSize(anchorTrailingChain);
        }
        return selectionSize;
    }

    private long calculateSubChainSelectionSize(SubChain anchorTrailingChain) {
        long anchorTrailingChainSize = (long) anchorTrailingChain.getSize();
        long n = anchorTrailingChainSize - (long) minimumSubChainSize + 1L;
        long m = (maximumSubChainSize >= anchorTrailingChainSize)
                ? 0L : anchorTrailingChainSize - (long) maximumSubChainSize;
        return (n * (n + 1L) / 2L) - (m * (m + 1L) / 2L);
    }

    public Iterator<SubChain> iterator() {
        if (!randomSelection) {
            return new OriginalSubChainIterator(anchorTrailingChainList.iterator());
        } else {
            return new RandomSubChainIterator();
        }
    }

    public ListIterator<SubChain> listIterator() {
        if (!randomSelection) {
            return new OriginalSubChainIterator(anchorTrailingChainList.iterator());
        } else {
            throw new IllegalStateException("The selector (" + this
                    + ") does not support a ListIterator with randomSelection (" + randomSelection + ").");
        }
    }

    public ListIterator<SubChain> listIterator(int index) {
        if (!randomSelection) {
            // TODO Implement more efficient ListIterator https://issues.jboss.org/browse/PLANNER-37
            OriginalSubChainIterator it = new OriginalSubChainIterator(anchorTrailingChainList.iterator());
            for (int i = 0; i < index; i++) {
                it.next();
            }
            return it;
        } else {
            throw new IllegalStateException("The selector (" + this
                    + ") does not support a ListIterator with randomSelection (" + randomSelection + ").");
        }
    }

    private class OriginalSubChainIterator extends UpcomingSelectionIterator<SubChain>
            implements ListIterator<SubChain> {

        private final Iterator<SubChain> anchorTrailingChainIterator;
        private List<Object> anchorTrailingChain;
        private int fromIndex; // Inclusive
        private int toIndex; // Exclusive

        private int nextListIteratorIndex;

        public OriginalSubChainIterator(Iterator<SubChain> anchorTrailingChainIterator) {
            this.anchorTrailingChainIterator = anchorTrailingChainIterator;
            fromIndex = 0;
            toIndex = 1;
            anchorTrailingChain = Collections.emptyList();
            nextListIteratorIndex = 0;
            createUpcomingSelection();
        }

        @Override
        protected void createUpcomingSelection() {
            toIndex++;
            if (toIndex - fromIndex > maximumSubChainSize || toIndex > anchorTrailingChain.size()) {
                fromIndex++;
                toIndex = fromIndex + minimumSubChainSize;
                // minimumSubChainSize <= maximumSubChainSize so (toIndex - fromIndex > maximumSubChainSize) is true
                while (toIndex > anchorTrailingChain.size()) {
                    if (!anchorTrailingChainIterator.hasNext()) {
                        upcomingSelection = null;
                        return;
                    }
                    anchorTrailingChain = anchorTrailingChainIterator.next().getEntityList();
                    fromIndex = 0;
                    toIndex = fromIndex + minimumSubChainSize;
                }
            }
            upcomingSelection = new SubChain(anchorTrailingChain.subList(fromIndex, toIndex));
        }

        @Override
        public SubChain next() {
            nextListIteratorIndex++;
            return super.next();
        }

        public int nextIndex() {
            return nextListIteratorIndex;
        }

        public boolean hasPrevious() {
            throw new UnsupportedOperationException("The operation hasPrevious() is not supported."
                    + " See https://issues.jboss.org/browse/PLANNER-37");
        }

        public SubChain previous() {
            throw new UnsupportedOperationException("The operation previous() is not supported."
                    + " See https://issues.jboss.org/browse/PLANNER-37");
        }

        public int previousIndex() {
            throw new UnsupportedOperationException("The operation previousIndex() is not supported."
                    + " See https://issues.jboss.org/browse/PLANNER-37");
        }

        public void set(SubChain subChain) {
            throw new UnsupportedOperationException("The optional operation set() is not supported.");
        }

        public void add(SubChain subChain) {
            throw new UnsupportedOperationException("The optional operation add() is not supported.");
        }
    }

    private class RandomSubChainIterator extends UpcomingSelectionIterator<SubChain> {

        private RandomSubChainIterator() {
            if (anchorTrailingChainList.isEmpty()) {
                upcomingSelection = null;
            } else {
                createUpcomingSelection();
            }
        }

        @Override
        protected void createUpcomingSelection() {
            SubChain anchorTrailingChain = selectAnchorTrailingChain();
            // Every SubChain must have same probability. A random fromIndex and random toIndex would not be fair.
            long selectionSize = calculateSubChainSelectionSize(anchorTrailingChain);
            long selectionIndex = RandomUtils.nextLong(workingRandom, selectionSize);
            // Black magic to translate selectionIndex into fromIndex and toIndex
            long fromIndex = selectionIndex;
            long subChainSize = minimumSubChainSize;
            long countInThatSize = anchorTrailingChain.getSize() - subChainSize + 1;
            while (fromIndex >= countInThatSize) {
                fromIndex -= countInThatSize;
                subChainSize++;
                countInThatSize--;
                if (countInThatSize <= 0) {
                    throw new IllegalStateException("Impossible if calculateSubChainSelectionSize() works correctly.");
                }
            }
            upcomingSelection = anchorTrailingChain.subChain((int) fromIndex, (int) (fromIndex + subChainSize));
        }

        private SubChain selectAnchorTrailingChain() {
            // TODO support SelectionProbabilityWeightFactory, such as FairSelectorProbabilityWeightFactory too
            int anchorTrailingChainListIndex = workingRandom.nextInt(anchorTrailingChainList.size());
            return anchorTrailingChainList.get(anchorTrailingChainListIndex);
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + valueSelector + ")";
    }

}
