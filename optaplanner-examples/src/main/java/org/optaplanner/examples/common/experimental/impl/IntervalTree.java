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
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IntervalTree<IntervalType_, PointType_ extends Comparable<PointType_>, DifferenceType_ extends Comparable<DifferenceType_>> {
    final TreeSet<IntervalSplitPoint<IntervalType_, PointType_>> splitPointSet;
    final Function<IntervalType_, PointType_> startMapping;
    final Function<IntervalType_, PointType_> endMapping;
    final ConsecutiveIntervalInfoImpl<IntervalType_, PointType_, DifferenceType_> consecutiveIntervalData;

    public IntervalTree(Function<IntervalType_, PointType_> startMapping,
            Function<IntervalType_, PointType_> endMapping,
            BiFunction<PointType_, PointType_, DifferenceType_> differenceFunction) {
        this.startMapping = startMapping;
        this.endMapping = endMapping;
        splitPointSet = new TreeSet<>();
        consecutiveIntervalData = new ConsecutiveIntervalInfoImpl<>(splitPointSet, differenceFunction);
    }

    private Interval<IntervalType_, PointType_> getInterval(IntervalType_ intervalValue) {
        return new Interval<>(intervalValue, startMapping, endMapping);
    }

    public boolean isEmpty() {
        return splitPointSet.isEmpty();
    }

    public boolean contains(IntervalType_ o) {
        if (null == o || splitPointSet.isEmpty()) {
            return false;
        }
        Interval<IntervalType_, PointType_> interval = getInterval(o);
        IntervalSplitPoint<IntervalType_, PointType_> floorStartSplitPoint =
                splitPointSet.floor(interval.getStartSplitPoint());
        if (floorStartSplitPoint == null) {
            return false;
        }
        return floorStartSplitPoint.containsIntervalStarting(interval);
    }

    public Iterator<IntervalType_> iterator() {
        return new IntervalTreeIterator<>(splitPointSet);
    }

    public boolean add(IntervalType_ o) {
        Interval<IntervalType_, PointType_> interval = getInterval(o);
        IntervalSplitPoint<IntervalType_, PointType_> startSplitPoint = interval.getStartSplitPoint();
        IntervalSplitPoint<IntervalType_, PointType_> endSplitPoint = interval.getEndSplitPoint();
        boolean isChanged;

        IntervalSplitPoint<IntervalType_, PointType_> flooredStartSplitPoint = splitPointSet.floor(startSplitPoint);
        IntervalSplitPoint<IntervalType_, PointType_> ceilingEndSplitPoint = splitPointSet.ceiling(endSplitPoint);
        if (flooredStartSplitPoint == null || !flooredStartSplitPoint.equals(startSplitPoint)) {
            splitPointSet.add(startSplitPoint);
            startSplitPoint.createCollections();
            isChanged = startSplitPoint.addIntervalStartingAtSplitPoint(interval);
        } else {
            isChanged = flooredStartSplitPoint.addIntervalStartingAtSplitPoint(interval);
        }

        if (ceilingEndSplitPoint == null || !ceilingEndSplitPoint.equals(endSplitPoint)) {
            splitPointSet.add(endSplitPoint);
            endSplitPoint.createCollections();
            endSplitPoint.addIntervalEndingAtSplitPoint(interval);
        } else {
            ceilingEndSplitPoint.addIntervalEndingAtSplitPoint(interval);
        }

        if (isChanged) {
            consecutiveIntervalData.addInterval(interval);
        }
        return true;
    }

    public boolean remove(IntervalType_ o) {
        if (null == o) {
            return false;
        }
        Interval<IntervalType_, PointType_> interval = getInterval(o);
        IntervalSplitPoint<IntervalType_, PointType_> startSplitPoint = interval.getStartSplitPoint();
        IntervalSplitPoint<IntervalType_, PointType_> endSplitPoint = interval.getEndSplitPoint();

        IntervalSplitPoint<IntervalType_, PointType_> flooredStartSplitPoint = splitPointSet.floor(startSplitPoint);
        if (flooredStartSplitPoint == null || !flooredStartSplitPoint.containsIntervalStarting(interval)) {
            return false;
        }

        flooredStartSplitPoint.removeIntervalStartingAtSplitPoint(interval);
        if (flooredStartSplitPoint.isEmpty()) {
            splitPointSet.remove(flooredStartSplitPoint);
        }

        IntervalSplitPoint<IntervalType_, PointType_> ceilEndSplitPoint = splitPointSet.ceiling(endSplitPoint);
        // Not null since the start point contained the interval
        ceilEndSplitPoint.removeIntervalEndingAtSplitPoint(interval);
        if (ceilEndSplitPoint.isEmpty()) {
            splitPointSet.remove(ceilEndSplitPoint);
        }

        consecutiveIntervalData.removalInterval(interval);
        return true;
    }

    public ConsecutiveIntervalInfoImpl<IntervalType_, PointType_, DifferenceType_> getConsecutiveIntervalData() {
        return consecutiveIntervalData;
    }
}
