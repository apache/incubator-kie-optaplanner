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

package org.optaplanner.core.impl.score.stream.bi;

import java.util.Arrays;
import java.util.function.Function;

import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.impl.score.stream.common.AbstractJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public final class DefaultBiJoiner<A, B> extends AbstractJoiner<B> implements BiJoiner<A, B> {

    public static final BiJoiner NONE = new DefaultBiJoiner(new Function[0], new JoinerType[0], new Function[0]);

    private final JoinerType[] joinerTypes;
    private final Function<A, ?>[] leftMappings;
    private final Function<B, ?>[] rightMappings;

    public <Property_> DefaultBiJoiner(Function<A, Property_> leftMapping, JoinerType joinerType,
            Function<B, Property_> rightMapping) {
        this(new Function[] { leftMapping }, new JoinerType[] { joinerType }, new Function[] { rightMapping });
    }

    private <Property_> DefaultBiJoiner(Function<A, Property_>[] leftMappings, JoinerType[] joinerTypes,
            Function<B, Property_>[] rightMappings) {
        this.joinerTypes = joinerTypes;
        this.leftMappings = leftMappings;
        this.rightMappings = rightMappings;
    }

    @Override
    public DefaultBiJoiner<A, B> and(BiJoiner<A, B> otherJoiner) {
        DefaultBiJoiner<A, B> castJoiner = (DefaultBiJoiner<A, B>) otherJoiner;
        int joinerCount = this.joinerTypes.length;
        int newJoinerCount = joinerCount + castJoiner.getJoinerCount();
        JoinerType[] newJoinerTypes = Arrays.copyOf(this.joinerTypes, newJoinerCount);
        Function[] newLeftMappings = Arrays.copyOf(this.leftMappings, newJoinerCount);
        Function[] newRightMappings = Arrays.copyOf(this.rightMappings, newJoinerCount);
        for (int i = 0; i < castJoiner.getJoinerCount(); i++) {
            int newJoinerIndex = i + joinerCount;
            newJoinerTypes[newJoinerIndex] = castJoiner.getJoinerTypes()[i];
            newLeftMappings[newJoinerIndex] = castJoiner.getLeftMapping(i);
            newRightMappings[newJoinerIndex] = castJoiner.getRightMapping(i);
        }
        return new DefaultBiJoiner<>(newLeftMappings, newJoinerTypes, newRightMappings);
    }

    public Function<A, Object> getLeftMapping(int index) {
        return (Function<A, Object>) leftMappings[index];
    }

    @Override
    public JoinerType[] getJoinerTypes() {
        return joinerTypes;
    }

    @Override
    public Function<B, Object> getRightMapping(int index) {
        return (Function<B, Object>) rightMappings[index];
    }

    public boolean matches(A a, B b) {
        JoinerType[] joinerTypes = getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = getLeftMapping(i).apply(a);
            Object rightMapping = getRightMapping(i).apply(b);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }

}
