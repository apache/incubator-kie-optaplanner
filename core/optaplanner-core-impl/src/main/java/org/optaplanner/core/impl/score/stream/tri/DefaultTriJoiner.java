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
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public final class DefaultTriJoiner<A, B, C> extends
        org.optaplanner.core.impl.score.stream.common.AbstractJoiner<C> implements TriJoiner<A, B, C> {

    private final JoinerType[] joinerTypes;
    private final BiFunction<A, B, ?>[] leftMappings;
    private final Function<C, ?>[] rightMappings;

    public DefaultTriJoiner(BiFunction<A, B, ?> leftMapping, JoinerType joinerType, Function<C, ?> rightMapping) {
        this.joinerTypes = new JoinerType[] { joinerType };
        this.leftMappings = new BiFunction[] { leftMapping };
        this.rightMappings = new Function[] { rightMapping };
    }

    public DefaultTriJoiner(TriJoiner<A, B, C>... joiners) {
        this.joinerTypes = Arrays.stream(joiners)
                .map(joiner -> (DefaultTriJoiner<A, B, C>) joiner)
                .flatMap(joiner -> Arrays.stream(joiner.getJoinerTypes()))
                .toArray(JoinerType[]::new);
        this.leftMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultTriJoiner<A, B, C>) joiner)
                .flatMap(joiner -> {
                    int joinerCount = joiner.getJoinerCount();
                    BiFunction[] mappings = new BiFunction[joinerCount];
                    for (int i = 0; i < joinerCount; i++) {
                        mappings[i] = joiner.getLeftMapping(i);
                    }
                    return Arrays.stream(mappings);
                })
                .toArray(BiFunction[]::new);
        this.rightMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultTriJoiner<A, B, C>) joiner)
                .flatMap(joiner -> {
                    int joinerCount = joiner.getJoinerCount();
                    Function[] mappings = new Function[joinerCount];
                    for (int i = 0; i < joinerCount; i++) {
                        mappings[i] = joiner.getRightMapping(i);
                    }
                    return Arrays.stream(mappings);
                })
                .toArray(Function[]::new);
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
