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

import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.apache.commons.lang3.ObjectUtils;
import org.optaplanner.examples.common.experimental.api.ConsecutiveIntervalInfo;
import org.optaplanner.examples.common.experimental.api.IntervalBreak;
import org.optaplanner.examples.common.experimental.api.IntervalCluster;

public class ConsecutiveIntervalInfoImpl<IntervalType_, PointType_ extends Comparable<PointType_>, DifferenceType_ extends Comparable<DifferenceType_>>
        implements
        ConsecutiveIntervalInfo<IntervalType_, PointType_, DifferenceType_> {
    private final NavigableMap<IntervalSplitPoint<IntervalType_, PointType_>, IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>> clusterStartSplitPointToCluster;
    private final NavigableSet<IntervalSplitPoint<IntervalType_, PointType_>> splitPointSet;
    private final NavigableMap<IntervalSplitPoint<IntervalType_, PointType_>, IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_>> clusterStartSplitPointToNextBreak;
    private final Iterable<IntervalCluster<IntervalType_, PointType_, DifferenceType_>> intervalClusterIterable;
    private final BiFunction<PointType_, PointType_, DifferenceType_> differenceFunction;
    private final Iterable<IntervalBreak<IntervalType_, PointType_, DifferenceType_>> breaksIterable;

    public ConsecutiveIntervalInfoImpl(TreeSet<IntervalSplitPoint<IntervalType_, PointType_>> splitPointSet,
            BiFunction<PointType_, PointType_, DifferenceType_> differenceFunction) {
        clusterStartSplitPointToCluster = new TreeMap<>();
        clusterStartSplitPointToNextBreak = new TreeMap<>();
        intervalClusterIterable = new MapValuesIterable<>(clusterStartSplitPointToCluster);
        breaksIterable = new MapValuesIterable<>(clusterStartSplitPointToNextBreak);
        this.splitPointSet = splitPointSet;
        this.differenceFunction = differenceFunction;
    }

    protected void addInterval(Interval<IntervalType_, PointType_> interval) {
        NavigableMap<IntervalSplitPoint<IntervalType_, PointType_>, IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>> intersectedIntervalClusterMap =
                clusterStartSplitPointToCluster.subMap(
                        ObjectUtils.defaultIfNull(clusterStartSplitPointToCluster.floorKey(interval.getStartSplitPoint()),
                                interval.getStartSplitPoint()),
                        true, interval.getEndSplitPoint(), true);

        // Case: the interval cluster before this interval does not intersect this interval
        if (!intersectedIntervalClusterMap.isEmpty()
                && intersectedIntervalClusterMap.get(intersectedIntervalClusterMap.firstKey()).getEndSplitPoint()
                        .compareTo(interval.getStartSplitPoint()) < 0) {
            intersectedIntervalClusterMap = intersectedIntervalClusterMap.subMap(intersectedIntervalClusterMap.firstKey(),
                    false, intersectedIntervalClusterMap.lastKey(), true);
        }

        if (intersectedIntervalClusterMap.isEmpty()) {
            IntervalSplitPoint<IntervalType_, PointType_> start = splitPointSet.floor(interval.getStartSplitPoint());
            IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_> newCluster =
                    new IntervalClusterImpl<>(splitPointSet, differenceFunction, start);
            clusterStartSplitPointToCluster.put(start, newCluster);
            Map.Entry<IntervalSplitPoint<IntervalType_, PointType_>, IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>> nextClusterEntry =
                    clusterStartSplitPointToCluster.higherEntry(start);
            if (nextClusterEntry != null) {
                clusterStartSplitPointToNextBreak.put(start,
                        new IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_>(newCluster,
                                nextClusterEntry.getValue(), differenceFunction.apply(
                                        newCluster.getEnd(), nextClusterEntry.getValue().getStart())));
            }
            Map.Entry<IntervalSplitPoint<IntervalType_, PointType_>, IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>> previousClusterEntry =
                    clusterStartSplitPointToCluster.lowerEntry(start);
            if (previousClusterEntry != null) {
                clusterStartSplitPointToNextBreak.put(previousClusterEntry.getKey(),
                        new IntervalBreakImpl<>(previousClusterEntry.getValue(), newCluster, differenceFunction.apply(
                                previousClusterEntry.getValue().getEnd(), newCluster.getStart())));
            }
            return;
        }

        NavigableMap<IntervalSplitPoint<IntervalType_, PointType_>, IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_>> intersectedIntervalBreakMap =
                clusterStartSplitPointToNextBreak.subMap(
                        ObjectUtils.defaultIfNull(clusterStartSplitPointToNextBreak.floorKey(interval.getStartSplitPoint()),
                                interval.getStartSplitPoint()),
                        true, interval.getEndSplitPoint(), true);
        IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_> intervalCluster =
                intersectedIntervalClusterMap.get(intersectedIntervalClusterMap.firstKey());
        IntervalSplitPoint<IntervalType_, PointType_> oldStart = intervalCluster.getStartSplitPoint();
        intervalCluster.addInterval(interval);
        intersectedIntervalClusterMap.tailMap(intersectedIntervalClusterMap.firstKey(), false).values()
                .forEach(intervalCluster::mergeIntervalCluster);
        intersectedIntervalClusterMap.tailMap(intersectedIntervalClusterMap.firstKey(), false).clear();

        if (intersectedIntervalBreakMap.size() > 0) {
            if (interval.getStartSplitPoint().compareTo(intersectedIntervalBreakMap.firstKey()) <= 0) {
                if (interval.getEndSplitPoint().compareTo(intersectedIntervalBreakMap.lastKey()) >= 0) {
                    intersectedIntervalBreakMap.clear();
                } else {
                    IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_> finalBreak =
                            intersectedIntervalBreakMap.lastEntry().getValue();
                    finalBreak.setPreviousCluster(intervalCluster);
                    finalBreak.setLength(
                            differenceFunction.apply(intervalCluster.getEnd(), finalBreak.getNextIntervalClusterStart()));
                    intersectedIntervalBreakMap.clear();
                    clusterStartSplitPointToNextBreak.put(intervalCluster.getStartSplitPoint(), finalBreak);
                }
            } else if (interval.getEndSplitPoint().compareTo(intersectedIntervalBreakMap.lastKey()) >= 0) {
                Map.Entry<IntervalSplitPoint<IntervalType_, PointType_>, IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_>> previousBreakEntry =
                        intersectedIntervalBreakMap.firstEntry();
                IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_> previousBreak = previousBreakEntry.getValue();
                previousBreak.setNextCluster(intervalCluster);
                previousBreak.setLength(
                        differenceFunction.apply(previousBreak.getPreviousIntervalClusterEnd(), intervalCluster.getStart()));
                intersectedIntervalBreakMap.tailMap(previousBreakEntry.getKey(), false).clear();
            } else {
                IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_> finalBreak =
                        intersectedIntervalBreakMap.lastEntry().getValue();
                finalBreak.setPreviousCluster(intervalCluster);
                finalBreak.setLength(
                        differenceFunction.apply(intervalCluster.getEnd(), finalBreak.getNextIntervalClusterStart()));

                Map.Entry<IntervalSplitPoint<IntervalType_, PointType_>, IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_>> previousBreakEntry =
                        intersectedIntervalBreakMap.firstEntry();
                IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_> previousBreak = previousBreakEntry.getValue();
                previousBreak.setNextCluster(intervalCluster);
                previousBreak.setLength(
                        differenceFunction.apply(previousBreak.getPreviousIntervalClusterEnd(), intervalCluster.getStart()));

                intersectedIntervalBreakMap.clear();
                clusterStartSplitPointToNextBreak.put(previousBreakEntry.getKey(), previousBreak);
                clusterStartSplitPointToNextBreak.put(intervalCluster.getStartSplitPoint(), finalBreak);
            }
        }
        if (oldStart.compareTo(intervalCluster.getStartSplitPoint()) > 0) {
            clusterStartSplitPointToCluster.remove(oldStart);
            clusterStartSplitPointToCluster.put(intervalCluster.getStartSplitPoint(), intervalCluster);
        }
    }

    protected void removalInterval(Interval<IntervalType_, PointType_> interval) {
        Map.Entry<IntervalSplitPoint<IntervalType_, PointType_>, IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>> intervalClusterEntry =
                clusterStartSplitPointToCluster.floorEntry(interval.getStartSplitPoint());
        IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_> intervalCluster = intervalClusterEntry.getValue();
        clusterStartSplitPointToCluster.remove(intervalClusterEntry.getKey());
        Map.Entry<IntervalSplitPoint<IntervalType_, PointType_>, IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_>> previousBreakEntry =
                clusterStartSplitPointToNextBreak.lowerEntry(intervalClusterEntry.getKey());
        Map.Entry<IntervalSplitPoint<IntervalType_, PointType_>, IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>> nextIntervalClusterEntry =
                clusterStartSplitPointToCluster.higherEntry(intervalClusterEntry.getKey());
        clusterStartSplitPointToNextBreak.remove(intervalClusterEntry.getKey());

        IntervalBreakImpl<IntervalType_, PointType_, DifferenceType_> previousBreak = null;
        if (previousBreakEntry != null) {
            previousBreak = previousBreakEntry.getValue();
        }
        for (IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_> newIntervalCluster : intervalCluster
                .removeInterval(interval)) {
            if (previousBreak != null) {
                previousBreak.setNextCluster(newIntervalCluster);
                previousBreak.setLength(differenceFunction.apply(previousBreak.getPreviousIntervalCluster().getEnd(),
                        newIntervalCluster.getStart()));
            }
            previousBreak = new IntervalBreakImpl<>(newIntervalCluster, null, null);
            clusterStartSplitPointToCluster.put(newIntervalCluster.getStartSplitPoint(), newIntervalCluster);
        }

        if (nextIntervalClusterEntry != null) {
            previousBreak.setNextCluster(nextIntervalClusterEntry.getValue());
            previousBreak.setLength(differenceFunction.apply(previousBreak.getPreviousIntervalCluster().getEnd(),
                    nextIntervalClusterEntry.getValue().getStart()));
            clusterStartSplitPointToNextBreak.put(
                    ((IntervalClusterImpl<IntervalType_, PointType_, DifferenceType_>) (previousBreak.getNextIntervalCluster()))
                            .getStartSplitPoint(),
                    previousBreak);
        } else if (previousBreakEntry != null && previousBreak == previousBreakEntry.getValue()) {
            clusterStartSplitPointToNextBreak.remove(previousBreakEntry.getKey());
        }
    }

    @Override
    public Iterable<IntervalCluster<IntervalType_, PointType_, DifferenceType_>> getIntervalClusters() {
        return intervalClusterIterable;
    }

    @Override
    public Iterable<IntervalBreak<IntervalType_, PointType_, DifferenceType_>> getBreaks() {
        return breaksIterable;
    }

    @Override
    public String toString() {
        return "ConsecutiveIntervalData{" +
                "intervalClusters=" + intervalClusterIterable +
                ", breaks=" + breaksIterable +
                '}';
    }
}
