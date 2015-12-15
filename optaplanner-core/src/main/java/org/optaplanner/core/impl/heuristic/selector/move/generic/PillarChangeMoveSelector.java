/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.pillar.PillarSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

public class PillarChangeMoveSelector extends GenericMoveSelector {

    protected final PillarSelector pillarSelector;
    protected final ValueSelector valueSelector;
    protected final boolean randomSelection;

    public PillarChangeMoveSelector(PillarSelector pillarSelector, ValueSelector valueSelector,
            boolean randomSelection) {
        this.pillarSelector = pillarSelector;
        this.valueSelector = valueSelector;
        this.randomSelection = randomSelection;
        GenuineVariableDescriptor variableDescriptor = valueSelector.getVariableDescriptor();
        if (variableDescriptor.isChained()) {
            throw new IllegalStateException("The selector (" + this
                    + ") has a variableDescriptor (" + variableDescriptor
                    + ") which is chained (" + variableDescriptor.isChained() + ").");
        }
        phaseLifecycleSupport.addEventListener(pillarSelector);
        phaseLifecycleSupport.addEventListener(valueSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isCountable() {
        return pillarSelector.isCountable() && valueSelector.isCountable();
    }

    public boolean isNeverEnding() {
        return randomSelection || pillarSelector.isNeverEnding() || valueSelector.isNeverEnding();
    }

    public long getSize() {
        if (!(valueSelector instanceof EntityIndependentValueSelector)) {
            throw new IllegalArgumentException("To use the method getSize(), the moveSelector (" + this
                    + ") needs to be based on an EntityIndependentValueSelector (" + valueSelector + ")."
                    + " Check your @" + ValueRangeProvider.class.getSimpleName() + " annotations.");
        }
        return pillarSelector.getSize() * ((EntityIndependentValueSelector) valueSelector).getSize();
    }

    public Iterator<Move> iterator() {
        if (!randomSelection) {
            return new OriginalPillarChangeMoveIterator();
        } else {
            return new RandomPillarChangeMoveIterator();
        }
    }

    private class OriginalPillarChangeMoveIterator extends UpcomingSelectionIterator<Move> {

        private Iterator<List<Object>> pillarIterator;
        private Iterator<Object> valueIterator;

        private List<Object> upcomingPillar;

        private OriginalPillarChangeMoveIterator() {
            pillarIterator = pillarSelector.iterator();
            // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
            valueIterator = Iterators.emptyIterator();
        }

        @Override
        protected Move createUpcomingSelection() {
            if (!valueIterator.hasNext()) {
                if (!pillarIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                upcomingPillar = pillarIterator.next();
                valueIterator = valueSelector.iterator(upcomingPillar.get(0));
                if (!valueIterator.hasNext()) {
                    // valueSelector is completely empty
                    return noUpcomingSelection();
                }
            }
            Object toValue = valueIterator.next();

            return new PillarChangeMove(upcomingPillar, valueSelector.getVariableDescriptor(), toValue);
        }

    }

    private class RandomPillarChangeMoveIterator extends UpcomingSelectionIterator<Move> {

        private Iterator<List<Object>> pillarIterator;
        private Iterator<Object> valueIterator;

        private RandomPillarChangeMoveIterator() {
            pillarIterator = pillarSelector.iterator();
            // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
            valueIterator = Iterators.emptyIterator();
        }

        @Override
        protected Move createUpcomingSelection() {
            // Ideally, this code should have read:
            //     Object pillar = pillarIterator.next();
            //     Object toValue = valueIterator.next();
            // But empty selectors and ending selectors (such as non-random or shuffled) make it more complex
            if (!pillarIterator.hasNext()) {
                pillarIterator = pillarSelector.iterator();
                if (!pillarIterator.hasNext()) {
                    // pillarSelector is completely empty
                    return noUpcomingSelection();
                }
            }
            List<Object> pillar = pillarIterator.next();

            if (!valueIterator.hasNext()) {
                valueIterator = valueSelector.iterator(pillar.get(0));
                if (!valueIterator.hasNext()) {
                    // valueSelector is completely empty
                    return noUpcomingSelection();
                }
            }
            Object toValue = valueIterator.next();

            return new PillarChangeMove(pillar, valueSelector.getVariableDescriptor(), toValue);
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + pillarSelector + ", " + valueSelector + ")";
    }

}
