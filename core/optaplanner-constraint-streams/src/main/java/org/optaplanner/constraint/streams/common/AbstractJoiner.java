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

package org.optaplanner.constraint.streams.common;

import java.util.Objects;
import java.util.function.Function;

import org.optaplanner.core.impl.score.stream.JoinerType;

public abstract class AbstractJoiner<Right_> {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    protected final Function<Right_, Object>[] rightMappings;
    // To support Bavet node sharing, this needs to be cached so that the joiner always returns the same thing.
    protected final Function<Right_, Object[]> combinedRightMapping;
    protected final JoinerType[] joinerTypes;

    protected <Property_> AbstractJoiner(Function<Right_, Property_> rightMapping, JoinerType joinerType) {
        this(new Function[] { rightMapping }, new JoinerType[] { joinerType });
    }

    protected <Property_> AbstractJoiner(Function<Right_, Property_>[] rightMappings, JoinerType[] joinerTypes) {
        this.rightMappings = (Function<Right_, Object>[]) Objects.requireNonNull(rightMappings);
        this.combinedRightMapping = combineRightMappings(rightMappings);
        this.joinerTypes = Objects.requireNonNull(joinerTypes);
    }

    private <Property_> Function<Right_, Object[]> combineRightMappings(Function<Right_, Property_>[] rightMappings) {
        int joinerCount = rightMappings.length;
        if (joinerCount == 0) {
            return (Right_ x) -> EMPTY_OBJECT_ARRAY;
        } else if (joinerCount == 1) {
            Function<Right_, Property_> mapping = rightMappings[0];
            return (Right_ x) -> new Object[] { mapping.apply(x) };
        } else {
            return (Right_ x) -> {
                Object[] result = new Object[joinerCount];
                for (int i = 0; i < joinerCount; i++) {
                    result[i] = rightMappings[i].apply(x);
                }
                return result;
            };
        }
    }

    public final Function<Right_, Object> getRightMapping(int index) {
        return rightMappings[index];
    }

    public final Function<Right_, Object[]> getCombinedRightMapping() {
        return combinedRightMapping;
    }

    public final int getJoinerCount() {
        return joinerTypes.length;
    }

    public final JoinerType getJoinerType(int index) {
        return joinerTypes[index];
    }

}
