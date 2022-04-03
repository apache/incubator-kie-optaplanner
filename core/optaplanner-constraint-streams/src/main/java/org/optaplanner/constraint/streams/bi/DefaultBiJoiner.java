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

package org.optaplanner.constraint.streams.bi;

import java.util.Arrays;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.AbstractJoiner;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.impl.score.stream.JoinerType;

public final class DefaultBiJoiner<A, B> extends AbstractJoiner<B> implements BiJoiner<A, B> {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final BiJoiner NONE = new DefaultBiJoiner(new Function[0], new JoinerType[0], new Function[0]);

    private final Function<A, ?>[] leftMappings;
    // To support Bavet node sharing, this needs to be cached so that the joiner always returns the same thing.
    private final Function<A, Object[]> combinedLeftMapping;

    public <Property_> DefaultBiJoiner(Function<A, Property_> leftMapping, JoinerType joinerType,
            Function<B, Property_> rightMapping) {
        super(rightMapping, joinerType);
        this.leftMappings = new Function[] { leftMapping };
        this.combinedLeftMapping = combineLeftMappings(leftMapping);
    }

    private <Property_> DefaultBiJoiner(Function<A, Property_>[] leftMappings, JoinerType[] joinerTypes,
            Function<B, Property_>[] rightMappings) {
        super(rightMappings, joinerTypes);
        this.leftMappings = leftMappings;
        this.combinedLeftMapping = combineLeftMappings(leftMappings);
    }

    private <Property_> Function<A, Object[]> combineLeftMappings(Function<A, Property_>... leftMappings) {
        int joinerCount = leftMappings.length;
        if (joinerCount == 0) {
            return (A a) -> EMPTY_OBJECT_ARRAY;
        } else if (joinerCount == 1) {
            Function<A, Property_> mapping = leftMappings[0];
            return (A a) -> new Object[] { mapping.apply(a) };
        } else {
            return (A a) -> {
                Object[] result = new Object[joinerCount];
                for (int i = 0; i < joinerCount; i++) {
                    result[i] = leftMappings[i].apply(a);
                }
                return result;
            };
        }
    }

    @Override
    public DefaultBiJoiner<A, B> and(BiJoiner<A, B> otherJoiner) {
        DefaultBiJoiner<A, B> castJoiner = (DefaultBiJoiner<A, B>) otherJoiner;
        int joinerCount = getJoinerCount();
        int castJoinerCount = castJoiner.getJoinerCount();
        int newJoinerCount = joinerCount + castJoinerCount;
        JoinerType[] newJoinerTypes = Arrays.copyOf(this.joinerTypes, newJoinerCount);
        Function[] newLeftMappings = Arrays.copyOf(this.leftMappings, newJoinerCount);
        Function[] newRightMappings = Arrays.copyOf(this.rightMappings, newJoinerCount);
        for (int i = 0; i < castJoinerCount; i++) {
            int newJoinerIndex = i + joinerCount;
            newJoinerTypes[newJoinerIndex] = castJoiner.getJoinerType(i);
            newLeftMappings[newJoinerIndex] = castJoiner.getLeftMapping(i);
            newRightMappings[newJoinerIndex] = castJoiner.getRightMapping(i);
        }
        return new DefaultBiJoiner<>(newLeftMappings, newJoinerTypes, newRightMappings);
    }

    public Function<A, Object> getLeftMapping(int index) {
        return (Function<A, Object>) leftMappings[index];
    }

    public Function<A, Object[]> getCombinedLeftMapping() {
        return combinedLeftMapping;
    }

    public boolean matches(A a, B b) {
        int joinerCount = getJoinerCount();
        for (int i = 0; i < joinerCount; i++) {
            JoinerType joinerType = getJoinerType(i);
            Object leftMapping = getLeftMapping(i).apply(a);
            Object rightMapping = getRightMapping(i).apply(b);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }

}
