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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class IntervalTreeTest {
    private static class Interval {
        final int start;
        final int end;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Interval interval = (Interval) o;
            return start == interval.start && end == interval.end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {
            return "(" + start + ", " + end + ")";
        }
    }

    private IntervalTree<Interval, Integer> getIntegerIntervalTree() {
        return new IntervalTree<>(Interval::getStart, Interval::getEnd);
    }

    @Test
    public void testNonConsecutiveIntervals() {
        IntervalTree<Interval, Integer> tree = getIntegerIntervalTree();
        tree.add(new Interval(0,2));
        tree.add(new Interval(3,4));
        tree.add(new Interval(5,7));

        List<IntervalCluster<Interval, Integer>> clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(3);
        assertThat(clusterList.get(0)).containsExactly(new Interval(0,2));
        assertThat(clusterList.get(0).hasOverlap()).isFalse();
        assertThat(clusterList.get(1)).containsExactly(new Interval(3,4));
        assertThat(clusterList.get(1).hasOverlap()).isFalse();
        assertThat(clusterList.get(2)).containsExactly(new Interval(5,7));
        assertThat(clusterList.get(2).hasOverlap()).isFalse();
    }

    @Test
    public void testConsecutiveIntervals() {
        IntervalTree<Interval, Integer> tree = getIntegerIntervalTree();
        tree.add(new Interval(0,2));
        tree.add(new Interval(2,4));
        tree.add(new Interval(4,7));

        List<IntervalCluster<Interval, Integer>> clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(1);
        assertThat(clusterList.get(0)).containsExactly(new Interval(0,2), new Interval(2,4), new Interval(4,7));
    }

    @Test
    public void testDuplicateIntervals() {
        IntervalTree<Interval, Integer> tree = getIntegerIntervalTree();
        Interval a = new Interval(0,2);
        Interval b = new Interval(4,7);
        tree.add(a);
        tree.add(a);
        tree.add(b);

        List<IntervalCluster<Interval, Integer>> clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(2);
        assertThat(clusterList.get(0)).containsExactly(a,a);
        assertThat(clusterList.get(1)).containsExactly(b);
    }

    @Test
    public void testIntervalRemoval() {
        IntervalTree<Interval, Integer> tree = getIntegerIntervalTree();
        Interval a = new Interval(0,2);
        Interval b = new Interval(2,4);
        Interval c = new Interval(4,7);
        tree.add(a);
        tree.add(b);
        tree.add(c);

        tree.remove(b);

        List<IntervalCluster<Interval, Integer>> clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(2);
        assertThat(clusterList.get(0)).containsExactly(new Interval(0,2));
        assertThat(clusterList.get(1)).containsExactly(new Interval(4,7));
    }

    @Test
    public void testOverlappingInterval() {
        IntervalTree<Interval, Integer> tree = getIntegerIntervalTree();
        Interval a = new Interval(0,2);
        Interval b = new Interval(1,3);
        Interval c = new Interval(2,4);

        Interval d = new Interval(5,6);

        Interval e = new Interval(7,9);
        Interval f = new Interval(7,9);

        tree.add(a);
        tree.add(b);
        tree.add(c);
        tree.add(d);
        tree.add(e);
        tree.add(f);

        List<IntervalCluster<Interval, Integer>> clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(3);
        assertThat(clusterList.get(0)).containsExactly(a,b,c);
        assertThat(clusterList.get(0).hasOverlap()).isTrue();
        assertThat(clusterList.get(1)).containsExactly(d);
        assertThat(clusterList.get(1).hasOverlap()).isFalse();
        assertThat(clusterList.get(2)).containsExactly(e,f);
        assertThat(clusterList.get(2).hasOverlap()).isTrue();

        tree.remove(b);
        clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(3);
        assertThat(clusterList.get(0)).containsExactly(a,c);
        assertThat(clusterList.get(0).hasOverlap()).isFalse();
        assertThat(clusterList.get(1)).containsExactly(d);
        assertThat(clusterList.get(1).hasOverlap()).isFalse();
        assertThat(clusterList.get(2)).containsExactly(e,f);
        assertThat(clusterList.get(2).hasOverlap()).isTrue();

        tree.remove(f);
        clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(3);
        assertThat(clusterList.get(0)).containsExactly(a,c);
        assertThat(clusterList.get(0).hasOverlap()).isFalse();
        assertThat(clusterList.get(1)).containsExactly(d);
        assertThat(clusterList.get(1).hasOverlap()).isFalse();
        assertThat(clusterList.get(2)).containsExactly(e);
        assertThat(clusterList.get(2).hasOverlap()).isFalse();

        Interval g = new Interval(6,7);
        tree.add(g);
        clusterList = tree.getConsecutiveIntervalData().getIntervalClusters();
        assertThat(clusterList).hasSize(2);
        assertThat(clusterList.get(0)).containsExactly(a,c);
        assertThat(clusterList.get(0).hasOverlap()).isFalse();
        assertThat(clusterList.get(1)).containsExactly(d,g,e);
        assertThat(clusterList.get(1).hasOverlap()).isFalse();
    }

}
