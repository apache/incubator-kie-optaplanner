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
import java.util.TreeMap;

public class TreeMapValueList<KeyType_, ValueType_> implements Iterable<ValueType_> {
    private final TreeMap<KeyType_, ValueType_> sourceMap;

    public TreeMapValueList(TreeMap<KeyType_, ValueType_> sourceMap) {
        this.sourceMap = sourceMap;
    }

    @Override
    public Iterator<ValueType_> iterator() {
        return sourceMap.values().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TreeMapValueList<?, ?> that = (TreeMapValueList<?, ?>) o;
        return sourceMap.values().containsAll(that.sourceMap.values())
                && that.sourceMap.values().containsAll(sourceMap.values());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceMap.values());
    }

    @Override
    public String toString() {
        return "TreeMapValueList{" +
                "sourceMap=" + sourceMap +
                '}';
    }
}
