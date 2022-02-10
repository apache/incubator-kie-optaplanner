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

package org.optaplanner.core.impl.score.stream.tri;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.impl.score.stream.common.AbstractJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public final class DefaultTriJoiner<A, B, C> extends AbstractJoiner<C> implements TriJoiner<A, B, C> {

    public static final TriJoiner NONE = new DefaultTriJoiner(new BiFunction[0], new JoinerType[0], new Function[0]);

    private final JoinerType[] joinerTypes;
    private final BiFunction<A, B, ?>[] leftMappings;
    private final Function<C, ?>[] rightMappings;

    public <Property_> DefaultTriJoiner(BiFunction<A, B, Property_> leftMapping, JoinerType joinerType,
            Function<C, Property_> rightMapping) {
        this(new BiFunction[] { leftMapping }, new JoinerType[] { joinerType }, new Function[] { rightMapping });
    }

    private <Property_> DefaultTriJoiner(BiFunction<A, B, Property_>[] leftMappings, JoinerType[] joinerTypes,
            Function<C, Property_>[] rightMappings) {
        this.joinerTypes = joinerTypes;
        this.leftMappings = leftMappings;
        this.rightMappings = rightMappings;
    }

    @Override
    public DefaultTriJoiner<A, B, C> and(TriJoiner<A, B, C> otherJoiner) {
        DefaultTriJoiner<A, B, C> castJoiner = (DefaultTriJoiner<A, B, C>) otherJoiner;
        int joinerCount = this.joinerTypes.length;
        int newJoinerCount = joinerCount + castJoiner.getJoinerCount();
        JoinerType[] newJoinerTypes = Arrays.copyOf(this.joinerTypes, newJoinerCount);
        BiFunction[] newLeftMappings = Arrays.copyOf(this.leftMappings, newJoinerCount);
        Function[] newRightMappings = Arrays.copyOf(this.rightMappings, newJoinerCount);
        for (int i = 0; i < castJoiner.getJoinerCount(); i++) {
            int newJoinerIndex = i + joinerCount;
            newJoinerTypes[newJoinerIndex] = castJoiner.getJoinerTypes()[i];
            newLeftMappings[newJoinerIndex] = castJoiner.getLeftMapping(i);
            newRightMappings[newJoinerIndex] = castJoiner.getRightMapping(i);
        }
        return new DefaultTriJoiner<>(newLeftMappings, newJoinerTypes, newRightMappings);
    }

    public BiFunction<A, B, Object> getLeftMapping(int index) {
        return (BiFunction<A, B, Object>) leftMappings[index];
    }

    @Override
    public JoinerType[] getJoinerTypes() {
        return joinerTypes;
    }

    @Override
    public Function<C, Object> getRightMapping(int index) {
        return (Function<C, Object>) rightMappings[index];
    }

    public boolean matches(A a, B b, C c) {
        JoinerType[] joinerTypes = getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = getLeftMapping(i).apply(a, b);
            Object rightMapping = getRightMapping(i).apply(c);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }
}
