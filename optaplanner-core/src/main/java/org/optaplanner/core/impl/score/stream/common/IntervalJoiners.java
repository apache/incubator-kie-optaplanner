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

package org.optaplanner.core.impl.score.stream.common;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

/**
 * Creates an {@link BiJoiner}, {@link TriJoiner}, ... instance
 * for data types that can be described as intervals (for instance,
 * time slots and numeric ranges). Based on Allen's Interval Algebra.
 * See https://en.wikipedia.org/wiki/Allen%27s_interval_algebra
 */
public class IntervalJoiners {

    // BiJoiners

    /**
     * For pairs of intervals [a,b) and [c,d), returns only those
     * pairs where [a,b) and [c,d) overlap (or, in other
     * words, there exists an x such that x in [a,b) and
     * x in [c,d)). These are exactly the pairs where
     * a < d and b > c (or in other words, when the first
     * interval starts before the second one ends and the
     * first interval ends after the second interval begins).
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first argument to the first interval start point
     * @param leftEndMapping Maps the first argument to the first interval end point
     * @param rightStartMapping Maps the second argument to the second interval start point
     * @param rightEndMapping Maps the second argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B) where A's and B's intervals (as defined by the function mapping)
     *         overlap.
     */
    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> overlaps(
            Function<A, Property_> leftStartMapping,
            Function<A, Property_> leftEndMapping,
            Function<B, Property_> rightStartMapping,
            Function<B, Property_> rightEndMapping) {
        return AbstractBiJoiner.merge(Joiners.lessThan(leftStartMapping, rightEndMapping),
                Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    /**
     * Syntactic sugar for {@link #overlaps(Function, Function, Function, Function)} where
     * both elements of the tuple (A,B) are the same type and have the same interval
     * defining function. Identical to
     * {@code overlaps(startMapping, endMapping, startMapping, endMapping)}.
     * 
     * @param <A> The type of both the first and second argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param startMapping Maps the argument to the start point of its interval
     * @param endMapping Maps the argument to the end point of its interval
     * 
     * @return See {@link #overlaps(Function, Function, Function, Function)}
     */
    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> overlaps(
            Function<A, Property_> startMapping,
            Function<A, Property_> endMapping) {
        return overlaps(startMapping, endMapping, startMapping, endMapping);
    }

    /**
     * For pairs of intervals [a,b) and [c,d), returns only those
     * pairs where [a,b) contains [c,d) (or, in other
     * words, for any x in [c, d), x is also in [a,b)). These are exactly the pairs
     * where a <= c and b >= d (or in other words, when the first
     * interval starts before the second one starts and the
     * first interval ends after the second interval ends).
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first argument to the first interval start point
     * @param leftEndMapping Maps the first argument to the first interval end point
     * @param rightStartMapping Maps the second argument to the second interval start point
     * @param rightEndMapping Maps the second argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B) where A's interval contains B's interval (as defined by the function mapping)
     */
    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> during(
            Function<A, Property_> leftStartMapping,
            Function<A, Property_> leftEndMapping,
            Function<B, Property_> rightStartMapping,
            Function<B, Property_> rightEndMapping) {
        return AbstractBiJoiner.merge(Joiners.lessThanOrEqual(leftStartMapping, rightStartMapping),
                Joiners.greaterThanOrEqual(leftEndMapping, rightEndMapping));
    }

    /**
     * Syntactic sugar for {@link #during(Function, Function, Function, Function)} where
     * both elements of the tuple (A,B) are the same type and have the same interval
     * defining function. Identical to
     * {@code during(startMapping, endMapping, startMapping, endMapping)}.
     * 
     * @param <A> The type of both the first and second argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param startMapping Maps the argument to the start point of its interval
     * @param endMapping Maps the argument to the end point of its interval
     * 
     * @return See {@link #during(Function, Function, Function, Function)}
     */
    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> during(
            Function<A, Property_> startMapping,
            Function<A, Property_> endMapping) {
        return during(startMapping, endMapping, startMapping, endMapping);
    }

    // TriJoiners
    /**
     * Like {@link #overlaps(Function, Function, Function, Function)}, but for
     * TriConstraintStream.
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <C> The type of the third argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first and second arguments to the first interval start point
     * @param leftEndMapping Maps the first and second arguments to the first interval end point
     * @param rightStartMapping Maps the third argument to the second interval start point
     * @param rightEndMapping Maps the third argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B,C) where (A,B)'s and C's intervals (as defined by the function mapping)
     *         overlap.
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> overlaps(
            BiFunction<A, B, Property_> leftStartMapping,
            BiFunction<A, B, Property_> leftEndMapping,
            Function<C, Property_> rightStartMapping,
            Function<C, Property_> rightEndMapping) {
        return AbstractTriJoiner.merge(Joiners.lessThan(leftStartMapping, rightEndMapping),
                Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    /**
     * Like {@link #during(Function, Function, Function, Function)}, but for
     * TriConstraintStream.
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <C> The type of the third argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first and second arguments to the first interval start point
     * @param leftEndMapping Maps the first and second arguments to the first interval end point
     * @param rightStartMapping Maps the third argument to the second interval start point
     * @param rightEndMapping Maps the third argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B,C) where (A,B)'s interval contains C's interval (as defined by the function mapping)
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> during(
            BiFunction<A, B, Property_> leftStartMapping,
            BiFunction<A, B, Property_> leftEndMapping,
            Function<C, Property_> rightStartMapping,
            Function<C, Property_> rightEndMapping) {
        return AbstractTriJoiner.merge(Joiners.lessThanOrEqual(leftStartMapping, rightStartMapping),
                Joiners.greaterThanOrEqual(leftEndMapping, rightEndMapping));
    }

    // QuadJoiners
    /**
     * Like {@link #overlaps(Function, Function, Function, Function)}, but for
     * QuadConstraintStream.
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <C> The type of the third argument
     * @param <D> The type of the fourth argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first, second and third arguments to the first interval start point
     * @param leftEndMapping Maps the first, second and third arguments to the first interval end point
     * @param rightStartMapping Maps the fourth argument to the second interval start point
     * @param rightEndMapping Maps the fourth argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B,C,D) where (A,B,C)'s and D's intervals (as defined by the function mapping)
     *         overlap.
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> overlaps(
            TriFunction<A, B, C, Property_> leftStartMapping,
            TriFunction<A, B, C, Property_> leftEndMapping,
            Function<D, Property_> rightStartMapping,
            Function<D, Property_> rightEndMapping) {
        return AbstractQuadJoiner.merge(Joiners.lessThan(leftStartMapping, rightEndMapping),
                Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    /**
     * Like {@link #during(Function, Function, Function, Function)}, but for
     * QuadConstraintStream.
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <C> The type of the third argument
     * @param <D> The type of the fourth argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first, second and third arguments to the first interval start point
     * @param leftEndMapping Maps the first, second and third arguments to the first interval end point
     * @param rightStartMapping Maps the fourth argument to the second interval start point
     * @param rightEndMapping Maps the fourth argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B,C,D) where (A,B,C)'s interval contains D's interval (as defined by the function mapping)
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> during(
            TriFunction<A, B, C, Property_> leftStartMapping,
            TriFunction<A, B, C, Property_> leftEndMapping,
            Function<D, Property_> rightStartMapping,
            Function<D, Property_> rightEndMapping) {
        return AbstractQuadJoiner.merge(Joiners.lessThanOrEqual(leftStartMapping, rightStartMapping),
                Joiners.greaterThanOrEqual(leftEndMapping, rightEndMapping));
    }

    // PentaJoiners
    /**
     * Like {@link #overlaps(Function, Function, Function, Function)}, but for
     * PentaConstraintStream.
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <C> The type of the third argument
     * @param <D> The type of the fourth argument
     * @param <E> The type of the fifth argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first, second, third and fourth arguments to the first interval start point
     * @param leftEndMapping Maps the first, second, third and fourth arguments to the first interval end point
     * @param rightStartMapping Maps the fifth argument to the second interval start point
     * @param rightEndMapping Maps the fifth argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B,C,D,E) where (A,B,C,D)'s and E's intervals (as defined by the function mapping)
     *         overlap.
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> PentaJoiner<A, B, C, D, E> overlaps(
            QuadFunction<A, B, C, D, Property_> leftStartMapping,
            QuadFunction<A, B, C, D, Property_> leftEndMapping,
            Function<E, Property_> rightStartMapping,
            Function<E, Property_> rightEndMapping) {
        return AbstractPentaJoiner.merge(Joiners.lessThan(leftStartMapping, rightEndMapping),
                Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    /**
     * Like {@link #during(Function, Function, Function, Function)}, but for
     * PentaConstraintStream.
     * 
     * @param <A> The type of the first argument
     * @param <B> The type of the second argument
     * @param <C> The type of the third argument
     * @param <D> The type of the fourth argument
     * @param <E> The type of the fifth argument
     * @param <Property_> The type used to define the interval, comparable
     * 
     * @param leftStartMapping Maps the first, second, third and fourth arguments to the first interval start point
     * @param leftEndMapping Maps the first, second, third and fourth arguments to the first interval end point
     * @param rightStartMapping Maps the fifth argument to the second interval start point
     * @param rightEndMapping Maps the fifth argument to the second interval end point
     * 
     * @return An indexed joiner that filters the constraint stream to only include
     *         elements (A,B,C,D,E) where (A,B,C,D)'s interval contains E's interval (as defined by the function mapping)
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> PentaJoiner<A, B, C, D, E> during(
            QuadFunction<A, B, C, D, Property_> leftStartMapping,
            QuadFunction<A, B, C, D, Property_> leftEndMapping,
            Function<E, Property_> rightStartMapping,
            Function<E, Property_> rightEndMapping) {
        return AbstractPentaJoiner.merge(Joiners.lessThanOrEqual(leftStartMapping, rightStartMapping),
                Joiners.greaterThanOrEqual(leftEndMapping, rightEndMapping));
    }

    private IntervalJoiners() {
    }
}
