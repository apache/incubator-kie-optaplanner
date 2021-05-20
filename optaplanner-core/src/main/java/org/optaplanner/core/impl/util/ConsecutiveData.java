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

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsecutiveData<PointType_, DifferenceType_ extends Comparable<DifferenceType_>> {
    private final ConsecutiveSetTree<PointType_, ?, DifferenceType_> sourceTree;

    protected ConsecutiveData(ConsecutiveSetTree<PointType_, ?, DifferenceType_> sourceTree) {
        this.sourceTree = sourceTree;
    }

    public Iterable<Sequence<PointType_>> getConsecutiveSequences() {
        return sourceTree.getConsecutiveSequences();
    }

    public Iterable<Break<PointType_, DifferenceType_>> getBreaks() {
        return sourceTree.getBreaks();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConsecutiveData<?, ?> that = (ConsecutiveData<?, ?>) o;
        return Objects.equals(sourceTree, that.sourceTree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTree);
    }

    public String toString() {
        Stream.Builder<Sequence<PointType_>> streamBuilder = Stream.builder();
        for (Sequence<PointType_> sequence : getConsecutiveSequences()) {
            streamBuilder.add(sequence);
        }

        return streamBuilder.build().map(Sequence::toString)
                .collect(Collectors.joining("; ", "ConsecutiveData [", "]"));
    }
}
