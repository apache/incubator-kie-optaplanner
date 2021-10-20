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

package org.optaplanner.persistence.minizinc.backend;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class IntSet implements Comparable<IntSet> {
    private final TreeSet<Integer> backingSet;

    public IntSet(int... elements) {
        backingSet = new TreeSet<>();
        for (int element : elements) {
            backingSet.add(element);
        }
    }

    public IntSet(Collection<Integer> backingCollection) {
        this.backingSet = new TreeSet<>(backingCollection);
    }

    private IntSet(TreeSet<Integer> backingSet) {
        this.backingSet = backingSet;
    }

    @SuppressWarnings("unchecked")
    private TreeSet<Integer> getBackingSetClone() {
        return (TreeSet<Integer>) backingSet.clone();
    }

    public int size() {
        return backingSet.size();
    }

    public boolean contains(int value) {
        return backingSet.contains(value);
    }

    public boolean containsAll(Collection<Integer> items) {
        return backingSet.containsAll(items);
    }

    public boolean containsOnly(Collection<Integer> items) {
        return items.containsAll(backingSet);
    }

    public boolean containsAll(IntSet other) {
        return backingSet.containsAll(other.backingSet);
    }

    public boolean isSubSetOf(IntSet other) {
        return other.containsAll(this);
    }

    public boolean isSuperSetOf(IntSet other) {
        return containsAll(other);
    }

    public IntSet withIntSetElements(IntSet other) {
        TreeSet<Integer> backingSetClone = getBackingSetClone();
        backingSetClone.addAll(other.backingSet);
        return new IntSet(backingSetClone);
    }

    public IntSet withoutIntSetElements(IntSet other) {
        TreeSet<Integer> backingSetClone = getBackingSetClone();
        backingSetClone.removeAll(other.backingSet);
        return new IntSet(backingSetClone);
    }

    public IntSet intersectingIntSetElements(IntSet other) {
        TreeSet<Integer> backingSetClone = getBackingSetClone();
        backingSetClone.retainAll(other.backingSet);
        return new IntSet(backingSetClone);
    }

    public IntSet symmetricDifference(IntSet other) { // also known as XOR
        // Definition of symmetric difference:
        // (A - B) + (B - A)
        // i.e. the elements of A and B that are not in both A and B
        TreeSet<Integer> backingSetClone = getBackingSetClone();
        backingSetClone.addAll(other.backingSet);
        backingSetClone.removeIf(element -> backingSet.contains(element) && other.backingSet.contains(element));
        return new IntSet(backingSetClone);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntSet intSet = (IntSet) o;
        return backingSet.equals(intSet.backingSet);
    }

    @Override
    public int hashCode() {
        return backingSet.hashCode();
    }

    // lexicographic comparison based on sorted list of elements
    @Override
    public int compareTo(IntSet other) {
        Iterator<Integer> myAscendingIterator = backingSet.iterator();
        Iterator<Integer> theirAscendingIterator = other.backingSet.iterator();

        while (myAscendingIterator.hasNext()) {
            if (!theirAscendingIterator.hasNext()) {
                return 1;
            }
            int myNextSmallestItem = myAscendingIterator.next();
            int theirNextSmallestItem = theirAscendingIterator.next();
            int difference = myNextSmallestItem - theirNextSmallestItem;
            if (difference != 0) {
                return difference;
            }
        }
        if (theirAscendingIterator.hasNext()) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return backingSet.stream().map(Object::toString).collect(Collectors.joining(", ", "{ ", " }"));
    }
}