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

package org.optaplanner.examples.common.experimental;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class Sequence<ValueType_> {
    private NavigableSet<ValueType_> consecutiveItemsSet;
    private final ConsecutiveSetTree<ValueType_, ?, ?> sourceTree;

    protected Sequence(ConsecutiveSetTree<ValueType_, ?, ?> sourceTree, ValueType_ item) {
        this.sourceTree = sourceTree;
        this.consecutiveItemsSet = sourceTree.getItemSet().subSet(item, true, item, true);
    }

    protected Sequence(ConsecutiveSetTree<ValueType_, ?, ?> sourceTree, NavigableSet<ValueType_> consecutiveItemsSet) {
        this.sourceTree = sourceTree;
        this.consecutiveItemsSet = consecutiveItemsSet;
    }

    public NavigableSet<ValueType_> getItems() {
        return consecutiveItemsSet;
    }

    public int getLength() {
        return consecutiveItemsSet.size();
    }

    protected boolean isEmpty() {
        return consecutiveItemsSet.isEmpty();
    }

    protected void addBeforeStart(ValueType_ item) {
        consecutiveItemsSet = sourceTree.getItemSet().subSet(item, true, consecutiveItemsSet.last(), true);
    }

    protected void addAfterEnd(ValueType_ item) {
        consecutiveItemsSet = sourceTree.getItemSet().subSet(consecutiveItemsSet.first(), true, item, true);
    }

    // Called when start or end are removed; since the set
    // is backed by the underlying set that already have the
    // point removed, consecutiveItemsSet.first()/consecutiveItemsSet.last()
    // change to reflect that, and we update the bounds
    protected void updateEndpoints() {
        consecutiveItemsSet = consecutiveItemsSet.subSet(consecutiveItemsSet.first(), true, consecutiveItemsSet.last(), true);
    }

    protected Sequence<ValueType_> split(ValueType_ fromElement) {
        NavigableSet<ValueType_> newSequenceConsecutiveItemSet = consecutiveItemsSet.subSet(
                consecutiveItemsSet.higher(fromElement), true,
                consecutiveItemsSet.last(), true);
        consecutiveItemsSet = consecutiveItemsSet.subSet(consecutiveItemsSet.first(), true,
                consecutiveItemsSet.lower(fromElement), true);
        return new Sequence<>(sourceTree, newSequenceConsecutiveItemSet);
    }

    protected void merge(Sequence<ValueType_> other) {
        consecutiveItemsSet =
                sourceTree.getItemSet().subSet(consecutiveItemsSet.first(), true, other.consecutiveItemsSet.last(), true);
    }

    @Override
    public String toString() {
        return consecutiveItemsSet.stream().map(Objects::toString).collect(Collectors.joining(", ", "Sequence [", "]"));
    }
}
