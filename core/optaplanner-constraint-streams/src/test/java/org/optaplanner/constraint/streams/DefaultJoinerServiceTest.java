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

package org.optaplanner.constraint.streams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.optaplanner.core.impl.score.stream.JoinerType.EQUAL;
import static org.optaplanner.core.impl.score.stream.JoinerType.LESS_THAN;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.function.PentaPredicate;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.impl.score.stream.JoinerService;

class DefaultJoinerServiceTest {

    private final JoinerService joinerService = new DefaultJoinerService();

    @Test
    <A, B> void cachesBi() {
        Function<A, Integer> leftMapping = a -> 0;
        Function<B, Integer> rightMapping = b -> 1;
        BiJoiner<A, A> joiner1 = joinerService.newBiJoiner(leftMapping, EQUAL, leftMapping);
        BiJoiner<A, A> joiner2 = joinerService.newBiJoiner(leftMapping, EQUAL, leftMapping);
        BiJoiner<A, A> joiner3 = joinerService.newBiJoiner(leftMapping, LESS_THAN, leftMapping);

        // Different joiner type, same mapping.
        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });

        // Same joiner type, different mapping
        BiJoiner<A, B> joiner4 = joinerService.newBiJoiner(leftMapping, EQUAL, rightMapping);
        assertThat(joiner1).isNotSameAs(joiner4);
    }

    @Test
    <A, B> void cachesBiFilter() {
        BiPredicate<A, B> predicate = (a, b) -> true;
        BiJoiner<A, B> joiner1 = joinerService.newBiJoiner(predicate);
        BiJoiner<A, B> joiner2 = joinerService.newBiJoiner(predicate);
        BiJoiner<A, B> joiner3 = joinerService.newBiJoiner((a, b) -> true); // Does not equal others.

        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });
    }

    @Test
    <A, B, C> void cachesTri() {
        BiFunction<A, B, Integer> leftMapping = (a, b) -> 0;
        Function<C, Integer> rightMapping = c -> 1;
        TriJoiner<A, B, C> joiner1 = joinerService.newTriJoiner(leftMapping, EQUAL, rightMapping);
        TriJoiner<A, B, C> joiner2 = joinerService.newTriJoiner(leftMapping, EQUAL, rightMapping);
        TriJoiner<A, B, C> joiner3 = joinerService.newTriJoiner(leftMapping, LESS_THAN, rightMapping);

        // Different joiner type, same mapping.
        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });

        // Same joiner type, different mapping
        TriJoiner<A, B, C> joiner4 = joinerService.newTriJoiner(leftMapping, EQUAL, c -> 1);
        assertThat(joiner1).isNotSameAs(joiner4);
    }

    @Test
    <A, B, C> void cachesTriFilter() {
        TriPredicate<A, B, C> predicate = (a, b, c) -> true;
        TriJoiner<A, B, C> joiner1 = joinerService.newTriJoiner(predicate);
        TriJoiner<A, B, C> joiner2 = joinerService.newTriJoiner(predicate);
        TriJoiner<A, B, C> joiner3 = joinerService.newTriJoiner((a, b, c) -> true); // Does not equal others.

        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });
    }

    @Test
    <A, B, C, D> void cachesQuad() {
        TriFunction<A, B, C, Integer> leftMapping = (a, b, c) -> 0;
        Function<D, Integer> rightMapping = d -> 1;
        QuadJoiner<A, B, C, D> joiner1 = joinerService.newQuadJoiner(leftMapping, EQUAL, rightMapping);
        QuadJoiner<A, B, C, D> joiner2 = joinerService.newQuadJoiner(leftMapping, EQUAL, rightMapping);
        QuadJoiner<A, B, C, D> joiner3 = joinerService.newQuadJoiner(leftMapping, LESS_THAN, rightMapping);

        // Different joiner type, same mapping.
        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });

        // Same joiner type, different mapping
        QuadJoiner<A, B, C, D> joiner4 = joinerService.newQuadJoiner(leftMapping, EQUAL, d -> 1);
        assertThat(joiner1).isNotSameAs(joiner4);
    }

    @Test
    <A, B, C, D> void cachesQuadFilter() {
        QuadPredicate<A, B, C, D> predicate = (a, b, c, d) -> true;
        QuadJoiner<A, B, C, D> joiner1 = joinerService.newQuadJoiner(predicate);
        QuadJoiner<A, B, C, D> joiner2 = joinerService.newQuadJoiner(predicate);
        QuadJoiner<A, B, C, D> joiner3 = joinerService.newQuadJoiner((a, b, c, d) -> true); // Does not equal others.

        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });
    }

    @Test
    <A, B, C, D, E> void cachesPenta() {
        QuadFunction<A, B, C, D, Integer> leftMapping = (a, b, c, d) -> 0;
        Function<E, Integer> rightMapping = e -> 1;
        PentaJoiner<A, B, C, D, E> joiner1 = joinerService.newPentaJoiner(leftMapping, EQUAL, rightMapping);
        PentaJoiner<A, B, C, D, E> joiner2 = joinerService.newPentaJoiner(leftMapping, EQUAL, rightMapping);
        PentaJoiner<A, B, C, D, E> joiner3 = joinerService.newPentaJoiner(leftMapping, LESS_THAN, rightMapping);

        // Different joiner type, same mapping.
        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });

        // Same joiner type, different mapping
        PentaJoiner<A, B, C, D, E> joiner4 = joinerService.newPentaJoiner(leftMapping, EQUAL, e -> 1);
        assertThat(joiner1).isNotSameAs(joiner4);
    }

    @Test
    <A, B, C, D, E> void cachesPentaFilter() {
        PentaPredicate<A, B, C, D, E> predicate = (a, b, c, d, e) -> true;
        PentaJoiner<A, B, C, D, E> joiner1 = joinerService.newPentaJoiner(predicate);
        PentaJoiner<A, B, C, D, E> joiner2 = joinerService.newPentaJoiner(predicate);
        PentaJoiner<A, B, C, D, E> joiner3 = joinerService.newPentaJoiner((a, b, c, d, e) -> true); // Does not equal others.

        assertSoftly(softly -> {
            softly.assertThat(joiner1).isSameAs(joiner2);
            softly.assertThat(joiner1).isNotSameAs(joiner3);
        });
    }

}
