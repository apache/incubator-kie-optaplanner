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
import org.optaplanner.core.impl.score.stream.common.AbstractJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public final class DefaultPentaJoiner<A, B, C, D, E> extends AbstractJoiner<E> implements PentaJoiner<A, B, C, D, E> {

    private final JoinerType[] joinerTypes;
    private final QuadFunction<A, B, C, D, ?>[] leftMappings;
    private final Function<E, ?>[] rightMappings;

    public DefaultPentaJoiner(QuadFunction<A, B, C, D, ?> leftMapping, JoinerType joinerType, Function<E, ?> rightMapping) {
        this.joinerTypes = new JoinerType[] { joinerType };
        this.leftMappings = new QuadFunction[] { leftMapping };
        this.rightMappings = new Function[] { rightMapping };
    }

    public DefaultPentaJoiner(PentaJoiner<A, B, C, D, E>... joiners) {
        this.joinerTypes = Arrays.stream(joiners)
                .map(joiner -> (DefaultPentaJoiner<A, B, C, D, E>) joiner)
                .flatMap(joiner -> Arrays.stream(joiner.getJoinerTypes()))
                .toArray(JoinerType[]::new);
        this.leftMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultPentaJoiner<A, B, C, D, E>) joiner)
                .flatMap(joiner -> {
                    int joinerCount = joiner.getJoinerCount();
                    QuadFunction[] mappings = new QuadFunction[joinerCount];
                    for (int i = 0; i < joinerCount; i++) {
                        mappings[i] = joiner.getLeftMapping(i);
                    }
                    return Arrays.stream(mappings);
                })
                .toArray(QuadFunction[]::new);
        this.rightMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultPentaJoiner<A, B, C, D, E>) joiner)
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
