/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet.common.index.overlapping.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.core.impl.util.Pair;

public final class IntervalTree<Interval_, Point_ extends Comparable<Point_>, Difference_ extends Comparable<Difference_>> {

    private final TreeSet<IntervalSplitPoint<Interval_, Point_>> splitPointSet;
    private final ConsecutiveIntervalInfoImpl<Interval_, Point_, Difference_> consecutiveIntervalData;

    private final Map<Pair<Point_, Point_>, Interval<Interval_, Point_>> intervalMap;

    public IntervalTree(BiFunction<Point_, Point_, Difference_> differenceFunction) {
        this.splitPointSet = new TreeSet<>();
        this.consecutiveIntervalData = new ConsecutiveIntervalInfoImpl<>(splitPointSet, differenceFunction);
        this.intervalMap = new HashMap<>();
    }

    public Interval<Interval_, Point_> getInterval(Interval_ intervalValue, Point_ start, Point_ end) {
        return new Interval<>(intervalValue, start, end);
    }

    public Interval<Interval_, Point_> getIntervalByRange(Point_ start, Point_ end) {
        return intervalMap.get(Pair.of(start, end));
    }

    public Interval<Interval_, Point_> computeIfAbsent(Point_ start, Point_ end, Supplier<Interval_> intervalValueMapper) {
        return intervalMap.computeIfAbsent(Pair.of(start, end), key -> {
            Interval<Interval_, Point_> out = getInterval(intervalValueMapper.get(), start, end);
            if (start.compareTo(end) < 0) {
                add(out);
            }
            return out;
        });
    }

    public boolean isEmpty() {
        return splitPointSet.isEmpty();
    }

    public Iterator<Interval_> iterator() {
        return new IntervalTreeIterator<>(splitPointSet);
    }

    public boolean add(Interval<Interval_, Point_> interval) {
        IntervalSplitPoint<Interval_, Point_> startSplitPoint = interval.getStartSplitPoint();
        IntervalSplitPoint<Interval_, Point_> endSplitPoint = interval.getEndSplitPoint();
        boolean isChanged;

        IntervalSplitPoint<Interval_, Point_> flooredStartSplitPoint = splitPointSet.floor(startSplitPoint);
        if (flooredStartSplitPoint == null || !flooredStartSplitPoint.equals(startSplitPoint)) {
            splitPointSet.add(startSplitPoint);
            startSplitPoint.createCollections();
            isChanged = startSplitPoint.addIntervalStartingAtSplitPoint(interval);
        } else {
            isChanged = flooredStartSplitPoint.addIntervalStartingAtSplitPoint(interval);
        }

        IntervalSplitPoint<Interval_, Point_> ceilingEndSplitPoint = splitPointSet.ceiling(endSplitPoint);
        if (ceilingEndSplitPoint == null || !ceilingEndSplitPoint.equals(endSplitPoint)) {
            splitPointSet.add(endSplitPoint);
            endSplitPoint.createCollections();
            isChanged |= endSplitPoint.addIntervalEndingAtSplitPoint(interval);
        } else {
            isChanged |= ceilingEndSplitPoint.addIntervalEndingAtSplitPoint(interval);
        }

        if (isChanged) {
            consecutiveIntervalData.addInterval(interval);
        }
        return true;
    }

    public boolean remove(IndexProperties indexProperties, Interval interval) {
        IntervalSplitPoint<Interval_, Point_> startSplitPoint = interval.getStartSplitPoint();
        IntervalSplitPoint<Interval_, Point_> endSplitPoint = interval.getEndSplitPoint();

        IntervalSplitPoint<Interval_, Point_> flooredStartSplitPoint = splitPointSet.floor(startSplitPoint);
        if (flooredStartSplitPoint == null || !flooredStartSplitPoint.containsIntervalStarting(interval)) {
            return false;
        }

        flooredStartSplitPoint.removeIntervalStartingAtSplitPoint(interval);
        if (flooredStartSplitPoint.isEmpty()) {
            splitPointSet.remove(flooredStartSplitPoint);
        }

        IntervalSplitPoint<Interval_, Point_> ceilEndSplitPoint = splitPointSet.ceiling(endSplitPoint);
        // Not null since the start point contained the interval
        ceilEndSplitPoint.removeIntervalEndingAtSplitPoint(interval);
        if (ceilEndSplitPoint.isEmpty()) {
            splitPointSet.remove(ceilEndSplitPoint);
        }

        consecutiveIntervalData.removeInterval(interval);
        intervalMap.remove(indexProperties);
        return true;
    }

    public void visit(Interval interval, Consumer<Interval_> consumer) {
        consecutiveIntervalData.visit(interval, consumer);
    }

    public ConsecutiveIntervalInfoImpl<Interval_, Point_, Difference_> getConsecutiveIntervalData() {
        return consecutiveIntervalData;
    }
}
