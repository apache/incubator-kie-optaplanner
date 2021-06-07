/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.common.experimental.impl;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.optaplanner.examples.common.experimental.api.Sequence;

public class SequenceImpl<ValueType_, DifferenceType_ extends Comparable<DifferenceType_>>
        implements Sequence<ValueType_, DifferenceType_> {
    private NavigableSet<ValueType_> consecutiveItemsSet;
    private ValueType_ firstItem;
    private ValueType_ lastItem;
    private final ConsecutiveSetTree<ValueType_, ?, DifferenceType_> sourceTree;

    protected SequenceImpl(ConsecutiveSetTree<ValueType_, ?, DifferenceType_> sourceTree, ValueType_ item) {
        this.sourceTree = sourceTree;
        this.consecutiveItemsSet = sourceTree.getItemSet().subSet(item, true, item, true);
        this.firstItem = item;
        this.lastItem = item;
    }

    protected SequenceImpl(ConsecutiveSetTree<ValueType_, ?, DifferenceType_> sourceTree,
            NavigableSet<ValueType_> consecutiveItemsSet) {
        this.sourceTree = sourceTree;
        this.consecutiveItemsSet = consecutiveItemsSet;
        this.firstItem = consecutiveItemsSet.first();
        this.lastItem = consecutiveItemsSet.last();
    }

    @Override
    public ValueType_ getFirstItem() {
        return firstItem;
    }

    @Override
    public ValueType_ getLastItem() {
        return lastItem;
    }

    @Override
    public NavigableSet<ValueType_> getItems() {
        return consecutiveItemsSet;
    }

    @Override
    public int getCount() {
        return consecutiveItemsSet.size();
    }

    @Override
    public DifferenceType_ getLength() {
        return sourceTree.getSequenceLength(this);
    }

    protected boolean isEmpty() {
        return consecutiveItemsSet.isEmpty();
    }

    protected void addBeforeStart(ValueType_ item) {
        firstItem = item;
        consecutiveItemsSet = sourceTree.getItemSet().subSet(item, true, lastItem, true);
    }

    protected void addAfterEnd(ValueType_ item) {
        lastItem = item;
        consecutiveItemsSet = sourceTree.getItemSet().subSet(firstItem, true, item, true);
    }

    // Called when start or end are removed; since the set
    // is backed by the underlying set that already have the
    // point removed, consecutiveItemsSet.first()/consecutiveItemsSet.last()
    // change to reflect that, and we update the bounds
    protected void updateEndpoints() {
        firstItem = consecutiveItemsSet.first();
        lastItem = consecutiveItemsSet.last();
        consecutiveItemsSet = consecutiveItemsSet.subSet(firstItem, true, lastItem, true);
    }

    protected SequenceImpl<ValueType_, DifferenceType_> split(ValueType_ fromElement) {
        NavigableSet<ValueType_> newSequenceConsecutiveItemSet = consecutiveItemsSet.subSet(
                consecutiveItemsSet.higher(fromElement), true,
                lastItem, true);
        lastItem = consecutiveItemsSet.lower(fromElement);
        consecutiveItemsSet = consecutiveItemsSet.subSet(firstItem, true,
                lastItem, true);
        return new SequenceImpl<>(sourceTree, newSequenceConsecutiveItemSet);
    }

    // This Sequence is ALWAYS before other Sequence
    protected void merge(SequenceImpl<ValueType_, DifferenceType_> other) {
        lastItem = other.lastItem;
        consecutiveItemsSet =
                sourceTree.getItemSet().subSet(firstItem, true, other.lastItem, true);
    }

    @Override
    public String toString() {
        return consecutiveItemsSet.stream().map(Objects::toString).collect(Collectors.joining(", ", "Sequence [", "]"));
    }
}
