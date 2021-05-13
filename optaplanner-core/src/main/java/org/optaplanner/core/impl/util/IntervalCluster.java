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

import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

public class IntervalCluster<_IntervalValue, _PointValue extends Comparable<_PointValue>> implements Iterable<_IntervalValue> {
    IntervalSplitPoint<_IntervalValue, _PointValue> startSplitPoint;
    IntervalSplitPoint<_IntervalValue, _PointValue> endSplitPoint;

    int count;
    boolean hasOverlap;
    final TreeSet<IntervalSplitPoint<_IntervalValue, _PointValue>> splitPointSet;

    public IntervalCluster(TreeSet<IntervalSplitPoint<_IntervalValue, _PointValue>> splitPointSet,
            IntervalSplitPoint<_IntervalValue, _PointValue> start) {
        if (start == null) {
            throw new IllegalArgumentException("start (" + start + ") is null");
        }
        this.splitPointSet = splitPointSet;
        this.startSplitPoint = start;
        int activeIntervals = 0;
        count = 0;
        boolean anyOverlap = false;
        IntervalSplitPoint<_IntervalValue, _PointValue> current = start;
        do {
            count += current.intervalsStartingAtSplitPointSet.size();
            activeIntervals += current.intervalsStartingAtSplitPointSet.size() - current.intervalsEndingAtSplitPointSet.size();
            if (activeIntervals > 1) {
                anyOverlap = true;
            }
            current = splitPointSet.higher(current);
        } while (activeIntervals > 0 && current != null);
        hasOverlap = anyOverlap;

        if (current != null) {
            endSplitPoint = splitPointSet.lower(current);
        } else {
            endSplitPoint = splitPointSet.last();
        }
    }

    public IntervalCluster(TreeSet<IntervalSplitPoint<_IntervalValue, _PointValue>> splitPointSet,
            IntervalSplitPoint<_IntervalValue, _PointValue> start,
            IntervalSplitPoint<_IntervalValue, _PointValue> end, int count, boolean hasOverlap) {
        this.splitPointSet = splitPointSet;
        this.startSplitPoint = start;
        this.endSplitPoint = end;
        this.count = count;
        this.hasOverlap = hasOverlap;
    }

    public IntervalSplitPoint<_IntervalValue, _PointValue> getStartSplitPoint() {
        return startSplitPoint;
    }

    public IntervalSplitPoint<_IntervalValue, _PointValue> getEndSplitPoint() {
        return endSplitPoint;
    }

    public void addInterval(Interval<_IntervalValue, _PointValue> interval) {
        if (interval.getEndSplitPoint().compareTo(getStartSplitPoint()) > 0
                && interval.getStartSplitPoint().compareTo(getEndSplitPoint()) < 0) {
            hasOverlap = true;
        }
        if (interval.getStartSplitPoint().compareTo(startSplitPoint) < 0) {
            startSplitPoint = splitPointSet.floor(interval.getStartSplitPoint());
        }
        if (interval.getEndSplitPoint().compareTo(endSplitPoint) > 0) {
            endSplitPoint = splitPointSet.ceiling(interval.getEndSplitPoint());
        }
        count++;
    }

    public Iterable<IntervalCluster<_IntervalValue, _PointValue>>
            removeInterval(Interval<_IntervalValue, _PointValue> interval) {
        return () -> new Iterator<IntervalCluster<_IntervalValue, _PointValue>>() {

            IntervalSplitPoint<_IntervalValue, _PointValue> current = startSplitPoint;

            @Override
            public boolean hasNext() {
                return current != null && current.compareTo(endSplitPoint) < 0 && !splitPointSet.isEmpty();
            }

            @Override
            public IntervalCluster<_IntervalValue, _PointValue> next() {
                IntervalSplitPoint<_IntervalValue, _PointValue> start = current;
                IntervalSplitPoint<_IntervalValue, _PointValue> end;
                int activeIntervals = 0;
                count = 0;
                boolean anyOverlap = false;
                do {
                    count += current.intervalsStartingAtSplitPointSet.size();
                    activeIntervals +=
                            current.intervalsStartingAtSplitPointSet.size() - current.intervalsEndingAtSplitPointSet.size();
                    if (activeIntervals > 1) {
                        anyOverlap = true;
                    }
                    current = splitPointSet.higher(current);
                } while (activeIntervals > 0 && current != null);
                hasOverlap = anyOverlap;

                if (current != null) {
                    end = splitPointSet.lower(current);
                } else {
                    end = splitPointSet.last();
                }
                return new IntervalCluster<>(splitPointSet, start, end, count, hasOverlap);
            }
        };
    }

    public void mergeIntervalCluster(IntervalCluster<_IntervalValue, _PointValue> laterIntervalCluster) {
        count += laterIntervalCluster.count;
        endSplitPoint = laterIntervalCluster.endSplitPoint;
        hasOverlap |= laterIntervalCluster.hasOverlap;
    }

    public Iterator<_IntervalValue> iterator() {
        return new IntervalTreeIterator<>(splitPointSet.subSet(startSplitPoint, true, endSplitPoint, true));
    }

    public int size() {
        return count;
    }

    public boolean hasOverlap() {
        return hasOverlap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IntervalCluster<?, ?> that = (IntervalCluster<?, ?>) o;
        return startSplitPoint.equals(that.startSplitPoint) && endSplitPoint.equals(that.endSplitPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startSplitPoint, endSplitPoint);
    }

    @Override
    public String toString() {
        return "IntervalCluster{" +
                "startSplitPoint=" + startSplitPoint +
                ", endSplitPoint=" + endSplitPoint +
                ", count=" + count +
                ", hasOverlap=" + hasOverlap +
                '}';
    }
}
