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

import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

public class ConsecutiveIntervalData<_IntervalValue, _PointValue extends Comparable<_PointValue>> {
    private final TreeMap<IntervalSplitPoint<_IntervalValue,_PointValue>, IntervalCluster<_IntervalValue,_PointValue>> clusterStartSplitPointToCluster;
    private final TreeSet<IntervalSplitPoint<_IntervalValue,_PointValue>> splitPointSet;
    private final List<IntervalCluster<_IntervalValue,_PointValue>> valueList;

    public ConsecutiveIntervalData(TreeSet<IntervalSplitPoint<_IntervalValue,_PointValue>> splitPointSet) {
        clusterStartSplitPointToCluster = new TreeMap<>();
        valueList = new TreeMapValueList<>(clusterStartSplitPointToCluster);
        this.splitPointSet = splitPointSet;
    }

    protected void addInterval(Interval<_IntervalValue,_PointValue> interval) {
        NavigableMap<IntervalSplitPoint<_IntervalValue,_PointValue>,IntervalCluster<_IntervalValue,_PointValue>>
                intersectedIntervalClusterMap = clusterStartSplitPointToCluster.subMap(
                        ObjectUtils.defaultIfNull(clusterStartSplitPointToCluster.floorKey(interval.getStartSplitPoint()),
                                interval.getStartSplitPoint()),
                true, interval.getEndSplitPoint(), true);

        // Case: the interval cluster before this interval does not intersect this interval
        if (!intersectedIntervalClusterMap.isEmpty() && intersectedIntervalClusterMap.get(intersectedIntervalClusterMap.firstKey()).getEndSplitPoint().compareTo(interval.getStartSplitPoint()) < 0) {
            intersectedIntervalClusterMap = intersectedIntervalClusterMap.subMap(intersectedIntervalClusterMap.firstKey(), false, intersectedIntervalClusterMap.lastKey(), true);
        }

        if (intersectedIntervalClusterMap.isEmpty()) {
            IntervalSplitPoint<_IntervalValue,_PointValue> start = splitPointSet.floor(interval.getStartSplitPoint());
            clusterStartSplitPointToCluster.put(start, new IntervalCluster<>(splitPointSet, start));
            return;
        }
        IntervalCluster<_IntervalValue,_PointValue> intervalCluster = intersectedIntervalClusterMap.get(intersectedIntervalClusterMap.firstKey());
        IntervalSplitPoint<_IntervalValue,_PointValue> oldStart = intervalCluster.getStartSplitPoint();
        intervalCluster.addInterval(interval);
        intersectedIntervalClusterMap.tailMap(intersectedIntervalClusterMap.firstKey(), false).values().forEach(intervalCluster::mergeIntervalCluster);
        intersectedIntervalClusterMap.tailMap(intersectedIntervalClusterMap.firstKey(), false).clear();
        if (oldStart.compareTo(intervalCluster.getStartSplitPoint()) > 0) {
            clusterStartSplitPointToCluster.remove(oldStart);
            clusterStartSplitPointToCluster.put(intervalCluster.getStartSplitPoint(), intervalCluster);
        }
    }

    protected void removalInterval(Interval<_IntervalValue,_PointValue> interval) {
        Map.Entry<IntervalSplitPoint<_IntervalValue,_PointValue>, IntervalCluster<_IntervalValue,_PointValue>> intervalClusterEntry = clusterStartSplitPointToCluster.floorEntry(interval.getStartSplitPoint());
        IntervalCluster<_IntervalValue,_PointValue> intervalCluster = intervalClusterEntry.getValue();
        clusterStartSplitPointToCluster.remove(intervalClusterEntry.getKey());
        for (IntervalCluster<_IntervalValue,_PointValue> newIntervalCluster : intervalCluster.removeInterval(interval)) {
            clusterStartSplitPointToCluster.put(newIntervalCluster.getStartSplitPoint(), newIntervalCluster);
        }
    }

    public List<IntervalCluster<_IntervalValue,_PointValue>> getIntervalClusters() {
        return valueList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConsecutiveIntervalData<?, ?> that = (ConsecutiveIntervalData<?, ?>) o;
        return valueList.equals(that.valueList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueList);
    }

    @Override public String toString() {
        return "ConsecutiveIntervalData{" +
                "valueList=" + valueList +
                '}';
    }
}
