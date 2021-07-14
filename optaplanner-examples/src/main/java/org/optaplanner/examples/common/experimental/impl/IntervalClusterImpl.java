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

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.function.BiFunction;

import org.optaplanner.examples.common.experimental.api.IntervalCluster;

public class IntervalClusterImpl<IntervalType_, PointType_ extends Comparable<PointType_>, DifferenceType_ extends Comparable<DifferenceType_>>
        implements
        IntervalCluster<IntervalType_, PointType_, DifferenceType_> {
    IntervalSplitPoint<IntervalType_, PointType_> startSplitPoint;
    IntervalSplitPoint<IntervalType_, PointType_> endSplitPoint;

    int count;
    boolean hasOverlap;
    final NavigableSet<IntervalSplitPoint<IntervalType_, PointType_>> splitPointSet;
    final BiFunction<PointType_, PointType_, DifferenceType_> differenceFunction;

    public IntervalClusterImpl(NavigableSet<IntervalSplitPoint<IntervalType_, PointType_>> splitPointSet,
            BiFunction<PointType_, PointType_, DifferenceType_> differenceFunction,
            IntervalSplitPoint<IntervalType_, PointType_> start) {
        if (start == null) {
            throw new IllegalArgumentException("start (" + start + ") is null");
        }
        if (differenceFunction == null) {
            throw new IllegalArgumentException("differenceFunction (" + differenceFunction + ") is null");
        }
        this.splitPointSet = splitPointSet;
        this.startSplitPoint = start;
        this.endSplitPoint = start;
        this.differenceFunction = differenceFunction;
        int activeIntervals = 0;
        count = 0;
        boolean anyOverlap = false;
        IntervalSplitPoint<IntervalType_, PointType_> current = start;
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

    public IntervalClusterImpl(NavigableSet<IntervalSplitPoint<IntervalType_, PointType_>> splitPointSet,
            BiFunction<PointType_, PointType_, DifferenceType_> differenceFunction,
            IntervalSplitPoint<IntervalType_, PointType_> start,
            IntervalSplitPoint<IntervalType_, PointType_> end, int count, boolean hasOverlap) {
        this.splitPointSet = splitPointSet;
        this.startSplitPoint = start;
        this.endSplitPoint = end;
        this.differenceFunction = differenceFunction;
        this.count = count;
        this.hasOverlap = hasOverlap;
    }

    public IntervalSplitPoint<IntervalType_, PointType_> getStartSplitPoint() {
        return startSplitPoint;
    }

    public IntervalSplitPoint<IntervalType_, PointType_> getEndSplitPoint() {
        return endSplitPoint;
    }

    public void addInterval(Interval<IntervalType_, PointType_> interval) {
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

    public Iterable<IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>>
            removeInterval(Interval<IntervalType_, PointType_> interval) {
        // TODO: Make this incremental by only checking between the interval's
        //       start and end points
        return () -> new Iterator<IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>>() {

            IntervalSplitPoint<IntervalType_, PointType_> current = getStart(startSplitPoint);

            private IntervalSplitPoint<IntervalType_, PointType_>
                    getStart(IntervalSplitPoint<IntervalType_, PointType_> start) {
                while (start != null && start.isEmpty()) {
                    start = splitPointSet.higher(start);
                }
                return start;
            }

            @Override
            public boolean hasNext() {
                return current != null && current.compareTo(endSplitPoint) <= 0 && !splitPointSet.isEmpty();
            }

            @Override
            public IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_> next() {
                IntervalSplitPoint<IntervalType_, PointType_> start = current;
                IntervalSplitPoint<IntervalType_, PointType_> end;
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
                    current = getStart(current);
                } else {
                    end = splitPointSet.last();
                }

                return new IntervalClusterImpl<>(splitPointSet, differenceFunction, start, end, count, hasOverlap);
            }
        };
    }

    public void mergeIntervalCluster(IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_> laterIntervalCluster) {
        if (endSplitPoint.compareTo(laterIntervalCluster.startSplitPoint) > 0) {
            hasOverlap = true;
        }
        if (endSplitPoint.compareTo(laterIntervalCluster.endSplitPoint) < 0) {
            endSplitPoint = laterIntervalCluster.endSplitPoint;
        }
        count += laterIntervalCluster.count;
        hasOverlap |= laterIntervalCluster.hasOverlap;
    }

    public Iterator<IntervalType_> iterator() {
        return new IntervalTreeIterator<>(splitPointSet.subSet(startSplitPoint, true, endSplitPoint, true));
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public boolean hasOverlap() {
        return hasOverlap;
    }

    @Override
    public PointType_ getStart() {
        return startSplitPoint.splitPoint;
    }

    @Override
    public PointType_ getEnd() {
        return endSplitPoint.splitPoint;
    }

    @Override
    public DifferenceType_ getLength() {
        return differenceFunction.apply(startSplitPoint.splitPoint, endSplitPoint.splitPoint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IntervalClusterImpl<?, ?, ?> that = (IntervalClusterImpl<?, ?, ?>) o;
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
