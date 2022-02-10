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

package org.optaplanner.constraint.streams.quad;

import java.util.Arrays;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.AbstractJoiner;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.impl.score.stream.JoinerType;

public final class DefaultQuadJoiner<A, B, C, D> extends AbstractJoiner<D> implements QuadJoiner<A, B, C, D> {

    public static final QuadJoiner NONE = new DefaultQuadJoiner(new TriFunction[0], new JoinerType[0], new Function[0]);

    private final JoinerType[] joinerTypes;
    private final TriFunction<A, B, C, ?>[] leftMappings;
    private final Function<D, ?>[] rightMappings;

    public <Property_> DefaultQuadJoiner(TriFunction<A, B, C, Property_> leftMapping, JoinerType joinerType,
            Function<D, Property_> rightMapping) {
        this(new TriFunction[] { leftMapping }, new JoinerType[] { joinerType }, new Function[] { rightMapping });
    }

    private <Property_> DefaultQuadJoiner(TriFunction<A, B, C, Property_>[] leftMappings, JoinerType[] joinerTypes,
            Function<D, Property_>[] rightMappings) {
        this.joinerTypes = joinerTypes;
        this.leftMappings = leftMappings;
        this.rightMappings = rightMappings;
    }

    @Override
    public DefaultQuadJoiner<A, B, C, D> and(QuadJoiner<A, B, C, D> otherJoiner) {
        DefaultQuadJoiner<A, B, C, D> castJoiner = (DefaultQuadJoiner<A, B, C, D>) otherJoiner;
        int joinerCount = this.joinerTypes.length;
        int newJoinerCount = joinerCount + castJoiner.getJoinerCount();
        JoinerType[] newJoinerTypes = Arrays.copyOf(this.joinerTypes, newJoinerCount);
        TriFunction[] newLeftMappings = Arrays.copyOf(this.leftMappings, newJoinerCount);
        Function[] newRightMappings = Arrays.copyOf(this.rightMappings, newJoinerCount);
        for (int i = 0; i < castJoiner.getJoinerCount(); i++) {
            int newJoinerIndex = i + joinerCount;
            newJoinerTypes[newJoinerIndex] = castJoiner.getJoinerTypes()[i];
            newLeftMappings[newJoinerIndex] = castJoiner.getLeftMapping(i);
            newRightMappings[newJoinerIndex] = castJoiner.getRightMapping(i);
        }
        return new DefaultQuadJoiner<>(newLeftMappings, newJoinerTypes, newRightMappings);
    }

    public TriFunction<A, B, C, Object> getLeftMapping(int index) {
        return (TriFunction<A, B, C, Object>) leftMappings[index];
    }

    @Override
    public JoinerType[] getJoinerTypes() {
        return joinerTypes;
    }

    @Override
    public Function<D, Object> getRightMapping(int index) {
        return (Function<D, Object>) rightMappings[index];
    }

    public boolean matches(A a, B b, C c, D d) {
        JoinerType[] joinerTypes = getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = getLeftMapping(i).apply(a, b, c);
            Object rightMapping = getRightMapping(i).apply(d);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }
}
