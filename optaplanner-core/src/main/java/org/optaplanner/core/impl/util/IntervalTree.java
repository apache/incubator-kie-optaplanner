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
import java.util.TreeSet;
import java.util.function.Function;

public class IntervalTree<_IntervalValue, _PointValue extends Comparable<_PointValue>> {
    final TreeSet<IntervalSplitPoint<_IntervalValue, _PointValue>> splitPointSet;
    final Function<_IntervalValue, _PointValue> startMapping;
    final Function<_IntervalValue, _PointValue> endMapping;
    final ConsecutiveIntervalData<_IntervalValue, _PointValue> consecutiveIntervalData;

    public IntervalTree(Function<_IntervalValue, _PointValue> startMapping,
            Function<_IntervalValue, _PointValue> endMapping) {
        this.startMapping = startMapping;
        this.endMapping = endMapping;
        splitPointSet = new TreeSet<>();
        consecutiveIntervalData = new ConsecutiveIntervalData<>(splitPointSet);
    }

    private Interval<_IntervalValue, _PointValue> getInterval(_IntervalValue intervalValue) {
        return new Interval<>(intervalValue, startMapping, endMapping);
    }

    public boolean isEmpty() {
        return splitPointSet.isEmpty();
    }

    public boolean contains(_IntervalValue o) {
        if (null == o || splitPointSet.isEmpty()) {
            return false;
        }
        Interval<_IntervalValue, _PointValue> interval = getInterval(o);
        IntervalSplitPoint<_IntervalValue, _PointValue> floorStartSplitPoint =
                splitPointSet.floor(interval.getStartSplitPoint());
        if (floorStartSplitPoint == null) {
            return false;
        }
        return floorStartSplitPoint.containsIntervalStarting(interval);
    }

    public Iterator<_IntervalValue> iterator() {
        return new IntervalTreeIterator<>(splitPointSet);
    }

    public boolean add(_IntervalValue o) {
        Interval<_IntervalValue, _PointValue> interval = getInterval(o);
        IntervalSplitPoint<_IntervalValue, _PointValue> startSplitPoint = interval.getStartSplitPoint();
        IntervalSplitPoint<_IntervalValue, _PointValue> endSplitPoint = interval.getEndSplitPoint();
        boolean isChanged;

        IntervalSplitPoint<_IntervalValue, _PointValue> flooredStartSplitPoint = splitPointSet.floor(startSplitPoint);
        IntervalSplitPoint<_IntervalValue, _PointValue> ceilingEndSplitPoint = splitPointSet.ceiling(endSplitPoint);
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

    public boolean remove(_IntervalValue o) {
        if (null == o) {
            return false;
        }
        Interval<_IntervalValue, _PointValue> interval = getInterval(o);
        IntervalSplitPoint<_IntervalValue, _PointValue> startSplitPoint = interval.getStartSplitPoint();
        IntervalSplitPoint<_IntervalValue, _PointValue> endSplitPoint = interval.getEndSplitPoint();

        IntervalSplitPoint<_IntervalValue, _PointValue> flooredStartSplitPoint = splitPointSet.floor(startSplitPoint);
        if (flooredStartSplitPoint == null || !flooredStartSplitPoint.containsIntervalStarting(interval)) {
            return false;
        }

        flooredStartSplitPoint.removeIntervalStartingAtSplitPoint(interval);
        if (flooredStartSplitPoint.isEmpty()) {
            splitPointSet.remove(flooredStartSplitPoint);
        }

        IntervalSplitPoint<_IntervalValue, _PointValue> ceilEndSplitPoint = splitPointSet.ceiling(endSplitPoint);
        // Not null since the start point contained the interval
        ceilEndSplitPoint.removeIntervalEndingAtSplitPoint(interval);
        if (ceilEndSplitPoint.isEmpty()) {
            splitPointSet.remove(ceilEndSplitPoint);
        }

        consecutiveIntervalData.removalInterval(interval);
        return true;
    }

    public ConsecutiveIntervalData<_IntervalValue, _PointValue> getConsecutiveIntervalData() {
        return consecutiveIntervalData;
    }
}
