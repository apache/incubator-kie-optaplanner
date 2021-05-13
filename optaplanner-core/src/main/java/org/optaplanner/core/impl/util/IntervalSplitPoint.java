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

package org.optaplanner.core.impl.util;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class IntervalSplitPoint<_IntervalValue, _PointValue extends Comparable<_PointValue>>
        implements Comparable<IntervalSplitPoint<_IntervalValue, _PointValue>> {
    final _PointValue splitPoint;
    Map<_IntervalValue, Integer> startIntervalToCountMap;
    Map<_IntervalValue, Integer> endIntervalToCountMap;
    TreeSet<Interval<_IntervalValue, _PointValue>> intervalsStartingAtSplitPointSet;
    TreeSet<Interval<_IntervalValue, _PointValue>> intervalsEndingAtSplitPointSet;

    public IntervalSplitPoint(_PointValue splitPoint) {
        this.splitPoint = splitPoint;
    }

    protected void createCollections() {
        startIntervalToCountMap = new IdentityHashMap<>();
        endIntervalToCountMap = new IdentityHashMap<>();
        intervalsStartingAtSplitPointSet = new TreeSet<>(
                Comparator.<Interval<_IntervalValue, _PointValue>, _PointValue> comparing(Interval::getEnd)
                        .thenComparingInt(interval -> System.identityHashCode(interval.value)));
        intervalsEndingAtSplitPointSet = new TreeSet<>(
                Comparator.<Interval<_IntervalValue, _PointValue>, _PointValue> comparing(Interval::getStart)
                        .thenComparingInt(interval -> System.identityHashCode(interval.value)));
    }

    public boolean addIntervalStartingAtSplitPoint(Interval<_IntervalValue, _PointValue> interval) {
        startIntervalToCountMap.merge(interval.value, 1, Integer::sum);
        return intervalsStartingAtSplitPointSet.add(interval);
    }

    public void removeIntervalStartingAtSplitPoint(Interval<_IntervalValue, _PointValue> interval) {
        Integer newCount = startIntervalToCountMap.computeIfPresent(interval.value, (key, count) -> {
            if (count > 1) {
                return count - 1;
            }
            return null;
        });
        if (null == newCount) {
            intervalsStartingAtSplitPointSet.remove(interval);
        }
    }

    public boolean addIntervalEndingAtSplitPoint(Interval<_IntervalValue, _PointValue> interval) {
        endIntervalToCountMap.merge(interval.value, 1, Integer::sum);
        return intervalsEndingAtSplitPointSet.add(interval);
    }

    public void removeIntervalEndingAtSplitPoint(Interval<_IntervalValue, _PointValue> interval) {
        Integer newCount = endIntervalToCountMap.computeIfPresent(interval.value, (key, count) -> {
            if (count > 1) {
                return count - 1;
            }
            return null;
        });
        if (null == newCount) {
            intervalsEndingAtSplitPointSet.remove(interval);
        }
    }

    public boolean containsIntervalStarting(Interval<_IntervalValue, _PointValue> interval) {
        return intervalsStartingAtSplitPointSet.contains(interval);
    }

    public boolean containsIntervalEnding(Interval<_IntervalValue, _PointValue> interval) {
        return intervalsEndingAtSplitPointSet.contains(interval);
    }

    public Iterator<_IntervalValue> getValuesStartingFromSplitPointIterator() {
        return intervalsStartingAtSplitPointSet.stream()
                .flatMap(interval -> IntStream.range(0, startIntervalToCountMap.get(interval.value))
                        .mapToObj((index) -> interval.value))
                .iterator();
    }

    public boolean isEmpty() {
        return intervalsStartingAtSplitPointSet.isEmpty() && intervalsEndingAtSplitPointSet.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IntervalSplitPoint<?, ?> that = (IntervalSplitPoint<?, ?>) o;
        return splitPoint.equals(that.splitPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(splitPoint);
    }

    @Override
    public int compareTo(IntervalSplitPoint<_IntervalValue, _PointValue> other) {
        return splitPoint.compareTo(other.splitPoint);
    }

    @Override
    public String toString() {
        return "IntervalSplitPoint{" +
                "splitPoint=" + splitPoint +
                ", intervalsStartingAtSplitPointSet=" + intervalsStartingAtSplitPointSet +
                ", intervalsEndingAtSplitPointSet=" + intervalsEndingAtSplitPointSet +
                '}';
    }
}
