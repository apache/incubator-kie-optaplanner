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

    private final JoinerType[] joinerTypes;
    private final Function<A, ?>[] leftMappings;
    private final Function<B, ?>[] rightMappings;

    public DefaultBiJoiner(Function<A, ?> leftMapping, JoinerType joinerType, Function<B, ?> rightMapping) {
        this.joinerTypes = new JoinerType[] { joinerType };
        this.leftMappings = new Function[] { leftMapping };
        this.rightMappings = new Function[] { rightMapping };
    }

    public DefaultBiJoiner(BiJoiner<A, B>... joiners) {
        this.joinerTypes = Arrays.stream(joiners)
                .map(joiner -> (DefaultBiJoiner<A, B>) joiner)
                .flatMap(joiner -> Arrays.stream(joiner.getJoinerTypes()))
                .toArray(JoinerType[]::new);
        this.leftMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultBiJoiner<A, B>) joiner)
                .flatMap(joiner -> {
                    int joinerCount = joiner.getJoinerCount();
                    Function[] mappings = new Function[joinerCount];
                    for (int i = 0; i < joinerCount; i++) {
                        mappings[i] = joiner.getLeftMapping(i);
                    }
                    return Arrays.stream(mappings);
                })
                .toArray(Function[]::new);
        this.rightMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultBiJoiner<A, B>) joiner)
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
