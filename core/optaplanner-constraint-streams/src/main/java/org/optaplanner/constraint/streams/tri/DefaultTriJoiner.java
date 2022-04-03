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

package org.optaplanner.constraint.streams.tri;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.AbstractJoiner;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.impl.score.stream.JoinerType;

public final class DefaultTriJoiner<A, B, C> extends AbstractJoiner<C> implements TriJoiner<A, B, C> {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final TriJoiner NONE = new DefaultTriJoiner(new BiFunction[0], new JoinerType[0], new Function[0]);

    private final BiFunction<A, B, ?>[] leftMappings;
    // To support Bavet node sharing, this needs to be cached so that the joiner always returns the same thing.
    private final BiFunction<A, B, Object[]> combinedLeftMapping;

    public <Property_> DefaultTriJoiner(BiFunction<A, B, Property_> leftMapping, JoinerType joinerType,
            Function<C, Property_> rightMapping) {
        super(rightMapping, joinerType);
        this.leftMappings = new BiFunction[] { leftMapping };
        this.combinedLeftMapping = combineLeftMappings(leftMapping);
    }

    private <Property_> DefaultTriJoiner(BiFunction<A, B, Property_>[] leftMappings, JoinerType[] joinerTypes,
            Function<C, Property_>[] rightMappings) {
        super(rightMappings, joinerTypes);
        this.leftMappings = leftMappings;
        this.combinedLeftMapping = combineLeftMappings(leftMappings);
    }

    private <Property_> BiFunction<A, B, Object[]> combineLeftMappings(BiFunction<A, B, Property_>... leftMappings) {
        int joinerCount = leftMappings.length;
        if (joinerCount == 0) {
            return (A a, B b) -> EMPTY_OBJECT_ARRAY;
        } else if (joinerCount == 1) {
            BiFunction<A, B, Property_> mapping = leftMappings[0];
            return (A a, B b) -> new Object[] { mapping.apply(a, b) };
        } else {
            return (A a, B b) -> {
                Object[] result = new Object[joinerCount];
                for (int i = 0; i < joinerCount; i++) {
                    result[i] = leftMappings[i].apply(a, b);
                }
                return result;
            };
        }
    }

    @Override
    public DefaultTriJoiner<A, B, C> and(TriJoiner<A, B, C> otherJoiner) {
        DefaultTriJoiner<A, B, C> castJoiner = (DefaultTriJoiner<A, B, C>) otherJoiner;
        int joinerCount = getJoinerCount();
        int castJoinerCount = castJoiner.getJoinerCount();
        int newJoinerCount = joinerCount + castJoinerCount;
        JoinerType[] newJoinerTypes = Arrays.copyOf(this.joinerTypes, newJoinerCount);
        BiFunction[] newLeftMappings = Arrays.copyOf(this.leftMappings, newJoinerCount);
        Function[] newRightMappings = Arrays.copyOf(this.rightMappings, newJoinerCount);
        for (int i = 0; i < castJoinerCount; i++) {
            int newJoinerIndex = i + joinerCount;
            newJoinerTypes[newJoinerIndex] = castJoiner.getJoinerType(i);
            newLeftMappings[newJoinerIndex] = castJoiner.getLeftMapping(i);
            newRightMappings[newJoinerIndex] = castJoiner.getRightMapping(i);
        }
        return new DefaultTriJoiner<>(newLeftMappings, newJoinerTypes, newRightMappings);
    }

    public BiFunction<A, B, Object> getLeftMapping(int index) {
        return (BiFunction<A, B, Object>) leftMappings[index];
    }

    public BiFunction<A, B, Object[]> getCombinedLeftMapping() {
        return combinedLeftMapping;
    }

    public boolean matches(A a, B b, C c) {
        int joinerCount = getJoinerCount();
        for (int i = 0; i < joinerCount; i++) {
            JoinerType joinerType = getJoinerType(i);
            Object leftMapping = getLeftMapping(i).apply(a, b);
            Object rightMapping = getRightMapping(i).apply(c);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }
}
