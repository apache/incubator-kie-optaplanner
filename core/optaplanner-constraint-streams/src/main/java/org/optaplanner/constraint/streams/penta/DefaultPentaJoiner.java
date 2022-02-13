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

package org.optaplanner.constraint.streams.penta;

import java.util.Arrays;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.AbstractJoiner;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.impl.score.stream.JoinerType;

public final class DefaultPentaJoiner<A, B, C, D, E> extends AbstractJoiner<E> implements PentaJoiner<A, B, C, D, E> {

    public static final DefaultPentaJoiner NONE =
            new DefaultPentaJoiner(new QuadFunction[0], new JoinerType[0], new Function[0]);

    private final QuadFunction<A, B, C, D, ?>[] leftMappings;

    public <Property_> DefaultPentaJoiner(QuadFunction<A, B, C, D, Property_> leftMapping, JoinerType joinerType,
            Function<E, Property_> rightMapping) {
        super(rightMapping, joinerType);
        this.leftMappings = new QuadFunction[] { leftMapping };
    }

    private <Property_> DefaultPentaJoiner(QuadFunction<A, B, C, D, Property_>[] leftMappings, JoinerType[] joinerTypes,
            Function<E, Property_>[] rightMappings) {
        super(rightMappings, joinerTypes);
        this.leftMappings = leftMappings;
    }

    @Override
    public DefaultPentaJoiner<A, B, C, D, E> and(PentaJoiner<A, B, C, D, E> otherJoiner) {
        DefaultPentaJoiner<A, B, C, D, E> castJoiner = (DefaultPentaJoiner<A, B, C, D, E>) otherJoiner;
        int joinerCount = getJoinerCount();
        int castJoinerCount = castJoiner.getJoinerCount();
        int newJoinerCount = joinerCount + castJoinerCount;
        JoinerType[] newJoinerTypes = Arrays.copyOf(this.joinerTypes, newJoinerCount);
        QuadFunction[] newLeftMappings = Arrays.copyOf(this.leftMappings, newJoinerCount);
        Function[] newRightMappings = Arrays.copyOf(this.rightMappings, newJoinerCount);
        for (int i = 0; i < castJoinerCount; i++) {
            int newJoinerIndex = i + joinerCount;
            newJoinerTypes[newJoinerIndex] = castJoiner.getJoinerType(i);
            newLeftMappings[newJoinerIndex] = castJoiner.getLeftMapping(i);
            newRightMappings[newJoinerIndex] = castJoiner.getRightMapping(i);
        }
        return new DefaultPentaJoiner<>(newLeftMappings, newJoinerTypes, newRightMappings);
    }

    public QuadFunction<A, B, C, D, Object> getLeftMapping(int index) {
        return (QuadFunction<A, B, C, D, Object>) leftMappings[index];
    }

    public boolean matches(A a, B b, C c, D d, E e) {
        int joinerCount = getJoinerCount();
        for (int i = 0; i < joinerCount; i++) {
            JoinerType joinerType = getJoinerType(i);
            Object leftMapping = getLeftMapping(i).apply(a, b, c, d);
            Object rightMapping = getRightMapping(i).apply(e);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }
}
