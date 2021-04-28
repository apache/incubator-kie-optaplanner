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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConsecutiveData<T, D extends Comparable<D>> {
    private final ConsecutiveSetTree<T, ?, D> sourceTree;

    protected ConsecutiveData(ConsecutiveSetTree<T, ?, D> sourceTree) {
        this.sourceTree = sourceTree;
    }

    public List<Sequence<T>> getConsecutiveSequences() {
        return sourceTree.getConsecutiveSequences();
    }

    public List<D> getBreaks() {
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
        return getConsecutiveSequences().stream().map(Sequence::toString)
                .collect(Collectors.joining("; ", "ConsecutiveData [", "]"));
    }
}
