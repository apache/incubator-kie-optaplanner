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

package org.optaplanner.core.api.score.stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

public class JoinersTest {

    @Test
    public void equalBi() {
        Function<BigInteger, Long> leftMapping = BigInteger::longValue;
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractBiJoiner<BigInteger, BigDecimal> joiner = (AbstractBiJoiner<BigInteger, BigDecimal>) Joiners.equal(leftMapping,
                rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ONE, BigDecimal.ZERO)).isFalse();
        });
    }

    @Test
    public void equalTri() {
        BiFunction<BigInteger, BigInteger, Long> leftMapping = (a, b) -> a.add(b).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractTriJoiner<BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractTriJoiner<BigInteger, BigInteger, BigDecimal>) Joiners.equal(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ONE, BigInteger.ZERO, BigDecimal.ZERO)).isFalse();
        });
    }

    @Test
    public void equalQuad() {
        TriFunction<BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c) -> a.add(b).add(c).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners.equal(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ZERO)).isFalse();
        });
    }

    @Test
    public void equalPenta() {
        QuadFunction<BigInteger, BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c, d) -> a.add(b).add(c).add(d)
                .longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .equal(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ZERO)).isFalse();
        });
    }

    @Test
    public void lessThanBi() {
        Function<BigInteger, Long> leftMapping = BigInteger::longValue;
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractBiJoiner<BigInteger, BigDecimal> joiner = (AbstractBiJoiner<BigInteger, BigDecimal>) Joiners
                .lessThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void lessThanTri() {
        BiFunction<BigInteger, BigInteger, Long> leftMapping = (a, b) -> a.add(b).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractTriJoiner<BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractTriJoiner<BigInteger, BigInteger, BigDecimal>) Joiners.lessThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void lessThanQuad() {
        TriFunction<BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c) -> a.add(b).add(c).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .lessThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void lessThanPenta() {
        QuadFunction<BigInteger, BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c, d) -> a.add(b).add(c).add(d)
                .longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .lessThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void lessThanOrEqualBi() {
        Function<BigInteger, Long> leftMapping = BigInteger::longValue;
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractBiJoiner<BigInteger, BigDecimal> joiner = (AbstractBiJoiner<BigInteger, BigDecimal>) Joiners
                .lessThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void lessThanOrEqualTri() {
        BiFunction<BigInteger, BigInteger, Long> leftMapping = (a, b) -> a.add(b).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractTriJoiner<BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractTriJoiner<BigInteger, BigInteger, BigDecimal>) Joiners.lessThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void lessThanOrEqualQuad() {
        TriFunction<BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c) -> a.add(b).add(c).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .lessThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void lessThanOrEqualPenta() {
        QuadFunction<BigInteger, BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c, d) -> a.add(b).add(c).add(d)
                .longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .lessThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isTrue();
        });
    }

    @Test
    public void greaterThanBi() {
        Function<BigInteger, Long> leftMapping = BigInteger::longValue;
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractBiJoiner<BigInteger, BigDecimal> joiner = (AbstractBiJoiner<BigInteger, BigDecimal>) Joiners
                .greaterThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigDecimal.ONE)).isFalse();
        });
    }

    @Test
    public void greaterThanTri() {
        BiFunction<BigInteger, BigInteger, Long> leftMapping = (a, b) -> a.add(b).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractTriJoiner<BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractTriJoiner<BigInteger, BigInteger, BigDecimal>) Joiners.greaterThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
        });
    }

    @Test
    public void greaterThanQuad() {
        TriFunction<BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c) -> a.add(b).add(c).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .greaterThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
        });
    }

    @Test
    public void greaterThanPenta() {
        QuadFunction<BigInteger, BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c, d) -> a.add(b).add(c).add(d)
                .longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .greaterThan(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.TEN)).isFalse();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isFalse();
        });
    }

    @Test
    public void greaterThanOrEqualBi() {
        Function<BigInteger, Long> leftMapping = BigInteger::longValue;
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractBiJoiner<BigInteger, BigDecimal> joiner = (AbstractBiJoiner<BigInteger, BigDecimal>) Joiners
                .greaterThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigDecimal.ONE)).isFalse();
        });
    }

    @Test
    public void greaterThanOrEqualTri() {
        BiFunction<BigInteger, BigInteger, Long> leftMapping = (a, b) -> a.add(b).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractTriJoiner<BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractTriJoiner<BigInteger, BigInteger, BigDecimal>) Joiners.greaterThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
        });
    }

    @Test
    public void greaterThanOrEqualQuad() {
        TriFunction<BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c) -> a.add(b).add(c).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .greaterThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigDecimal.ONE)).isFalse();
        });
    }

    @Test
    public void greaterThanOrEqualPenta() {
        QuadFunction<BigInteger, BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c, d) -> a.add(b).add(c).add(d)
                .longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal>) Joiners
                        .greaterThanOrEqual(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isTrue();
            softly.assertThat(joiner.matches(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigDecimal.ONE)).isFalse();
        });
    }

    private static final class Interval<A, B> {
        final A start;
        final A end;
        final Function<A, B> map;

        public Interval(A start, A end, Function<A, B> map) {
            this.start = start;
            this.end = end;
            this.map = map;
        }

        public B getStart() {
            return map.apply(start);
        }

        public B getEnd() {
            return map.apply(end);
        }

        @Override
        public String toString() {
            return "(" + start.toString() + ", " + end.toString() + ")";
        }

        public static <A, B, C> IntervalFactory<A, B, C> createFactory(Function<C, A> constructorMap, Function<A, B> map) {
            return new IntervalFactory<A, B, C>(constructorMap, map);
        }

        private final static class IntervalFactory<A, B, C> {
            final Function<C, A> constructorMap;
            final Function<A, B> map;

            public IntervalFactory(Function<C, A> constructorMap, Function<A, B> map) {
                this.constructorMap = constructorMap;
                this.map = map;
            }

            public Interval<A, B> of(C start, C end) {
                return new Interval<A, B>(constructorMap.apply(start), constructorMap.apply(end), map);
            }
        }
    }

    private final Interval.IntervalFactory<BigInteger, Long, Long> bigIntInterval =
            Interval.createFactory(BigInteger::valueOf, BigInteger::longValue);

    private final Interval.IntervalFactory<BigDecimal, Long, Long> bigDecimalInterval =
            Interval.createFactory(BigDecimal::valueOf, BigDecimal::longValue);

    @Test
    public void overlapsBi() {
        Function<Interval<BigInteger, Long>, Long> leftStartMapping = Interval::getStart;
        Function<Interval<BigInteger, Long>, Long> leftEndMapping = Interval::getEnd;
        Function<Interval<BigDecimal, Long>, Long> rightStartMapping = Interval::getStart;
        Function<Interval<BigDecimal, Long>, Long> rightEndMapping = Interval::getEnd;

        AbstractBiJoiner<Interval<BigInteger, Long>, Interval<BigDecimal, Long>> joiner =
                (AbstractBiJoiner<Interval<BigInteger, Long>, Interval<BigDecimal, Long>>) Joiners.overlaps(leftStartMapping,
                        leftEndMapping, rightStartMapping, rightEndMapping);

        assertSoftly(softly -> {
            // Case A = B
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 5L),
                    bigDecimalInterval.of(1L, 5L))).isTrue();

            // Case B starts before A, A ends after B
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 7L),
                    bigDecimalInterval.of(1L, 5L))).isTrue();

            // Case A starts before B, B ends after A
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 5L),
                    bigDecimalInterval.of(3L, 7L))).isTrue();

            // Case B contains A
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 5L),
                    bigDecimalInterval.of(1L, 10L))).isTrue();

            // Case A contains B
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 10L),
                    bigDecimalInterval.of(3L, 5L))).isTrue();

            // Case A before B
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 2L),
                    bigDecimalInterval.of(3L, 4L))).isFalse();
            // Case B before A
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 4L),
                    bigDecimalInterval.of(1L, 2L))).isFalse();

            // Case A meets B
            // This is false since typically, when overlaps is used,
            // end is exclusive, and start is inclusive,
            // so 0-5, 5-10 do not overlap
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 3L),
                    bigDecimalInterval.of(3L, 5L))).isFalse();

            // Case B meets A
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 5L),
                    bigDecimalInterval.of(1L, 3L))).isFalse();
        });
    }

    @Test
    public void overlapsOrMeetsBi() {
        Function<Interval<BigInteger, Long>, Long> leftStartMapping = Interval::getStart;
        Function<Interval<BigInteger, Long>, Long> leftEndMapping = Interval::getEnd;
        Function<Interval<BigDecimal, Long>, Long> rightStartMapping = Interval::getStart;
        Function<Interval<BigDecimal, Long>, Long> rightEndMapping = Interval::getEnd;

        AbstractBiJoiner<Interval<BigInteger, Long>, Interval<BigDecimal, Long>> joiner =
                (AbstractBiJoiner<Interval<BigInteger, Long>, Interval<BigDecimal, Long>>) Joiners.overlapsOrMeets(
                        leftStartMapping,
                        leftEndMapping, rightStartMapping, rightEndMapping);

        assertSoftly(softly -> {
            // Case A = B
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 5L),
                    bigDecimalInterval.of(1L, 5L))).isTrue();

            // Case B starts before A, A ends after B
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 7L),
                    bigDecimalInterval.of(1L, 5L))).isTrue();

            // Case A starts before B, B ends after A
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 5L),
                    bigDecimalInterval.of(3L, 7L))).isTrue();

            // Case B contains A
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 5L),
                    bigDecimalInterval.of(1L, 10L))).isTrue();

            // Case A contains B
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 10L),
                    bigDecimalInterval.of(3L, 5L))).isTrue();

            // Case A meets B
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 3L),
                    bigDecimalInterval.of(3L, 5L))).isTrue();

            // Case B meets A
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 5L),
                    bigDecimalInterval.of(1L, 3L))).isTrue();

            // Case A before B
            softly.assertThat(joiner.matches(bigIntInterval.of(1L, 2L),
                    bigDecimalInterval.of(3L, 4L))).isFalse();
            // Case B before A
            softly.assertThat(joiner.matches(bigIntInterval.of(3L, 4L),
                    bigDecimalInterval.of(1L, 2L))).isFalse();
        });
    }
}
