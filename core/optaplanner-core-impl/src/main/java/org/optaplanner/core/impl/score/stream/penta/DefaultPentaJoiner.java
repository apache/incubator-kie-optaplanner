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

package org.optaplanner.core.impl.score.stream.penta;

import java.util.Arrays;
import java.util.function.Function;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.impl.score.stream.JoinerType;
import org.optaplanner.core.impl.score.stream.common.AbstractJoiner;

public final class DefaultPentaJoiner<A, B, C, D, E> extends AbstractJoiner<E> implements PentaJoiner<A, B, C, D, E> {

    public static final DefaultPentaJoiner NONE =
            new DefaultPentaJoiner(new QuadFunction[0], new JoinerType[0], new Function[0]);

    private final JoinerType[] joinerTypes;
    private final QuadFunction<A, B, C, D, ?>[] leftMappings;
    private final Function<E, ?>[] rightMappings;

    public <Property_> DefaultPentaJoiner(QuadFunction<A, B, C, D, Property_> leftMapping, JoinerType joinerType,
            Function<E, Property_> rightMapping) {
        this(new QuadFunction[] { leftMapping }, new JoinerType[] { joinerType }, new Function[] { rightMapping });
    }

    private <Property_> DefaultPentaJoiner(QuadFunction<A, B, C, D, Property_>[] leftMappings, JoinerType[] joinerTypes,
            Function<E, Property_>[] rightMappings) {
        this.joinerTypes = joinerTypes;
        this.leftMappings = leftMappings;
        this.rightMappings = rightMappings;
    }

    @Override
    public DefaultPentaJoiner<A, B, C, D, E> and(PentaJoiner<A, B, C, D, E> otherJoiner) {
        DefaultPentaJoiner<A, B, C, D, E> castJoiner = (DefaultPentaJoiner<A, B, C, D, E>) otherJoiner;
        int joinerCount = this.joinerTypes.length;
        int newJoinerCount = joinerCount + castJoiner.getJoinerCount();
        JoinerType[] newJoinerTypes = Arrays.copyOf(this.joinerTypes, newJoinerCount);
        QuadFunction[] newLeftMappings = Arrays.copyOf(this.leftMappings, newJoinerCount);
        Function[] newRightMappings = Arrays.copyOf(this.rightMappings, newJoinerCount);
        for (int i = 0; i < castJoiner.getJoinerCount(); i++) {
            int newJoinerIndex = i + joinerCount;
            newJoinerTypes[newJoinerIndex] = castJoiner.getJoinerTypes()[i];
            newLeftMappings[newJoinerIndex] = castJoiner.getLeftMapping(i);
            newRightMappings[newJoinerIndex] = castJoiner.getRightMapping(i);
        }
        return new DefaultPentaJoiner<>(newLeftMappings, newJoinerTypes, newRightMappings);
    }

    public QuadFunction<A, B, C, D, Object> getLeftMapping(int index) {
        return (QuadFunction<A, B, C, D, Object>) leftMappings[index];
    }

    @Override
    public JoinerType[] getJoinerTypes() {
        return joinerTypes;
    }

    @Override
    public Function<E, Object> getRightMapping(int index) {
        return (Function<E, Object>) rightMappings[index];
    }

    public boolean matches(A a, B b, C c, D d, E e) {
        JoinerType[] joinerTypes = getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = getLeftMapping(i).apply(a, b, c, d);
            Object rightMapping = getRightMapping(i).apply(e);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }
}
