/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.common;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class AbstractJoiner<Right> {

    protected void assertMappingIndex(int index) {
        int mappingCount = getJoinerTypes().length;
        if (index >= mappingCount) {
            throw new IllegalArgumentException("Joiner only has (" + mappingCount + ") mappings, requested index (" + index + ")");
        }
    }

    public abstract JoinerType[] getJoinerTypes();

    public abstract Function<Right, Object> getRightMapping(int index);

    public Function<Right, Object[]> getRightCombinedMapping() {
        Function<Right, Object>[] mappings = IntStream.range(0, getJoinerTypes().length)
                .mapToObj(this::getRightMapping)
                .toArray(Function[]::new);
        return (Right right) -> Arrays.stream(mappings)
                .map(f -> f.apply(right))
                .toArray();
    }

}
