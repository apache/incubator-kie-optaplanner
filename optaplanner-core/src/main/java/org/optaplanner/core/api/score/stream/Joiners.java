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

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.optaplanner.core.api.function.PentaPredicate;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.bi.FilteringBiJoiner;
import org.optaplanner.core.impl.score.stream.bi.SingleBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.penta.FilteringPentaJoiner;
import org.optaplanner.core.impl.score.stream.penta.SinglePentaJoiner;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.quad.FilteringQuadJoiner;
import org.optaplanner.core.impl.score.stream.quad.SingleQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;
import org.optaplanner.core.impl.score.stream.tri.FilteringTriJoiner;
import org.optaplanner.core.impl.score.stream.tri.SingleTriJoiner;

/**
 * Creates an {@link BiJoiner}, {@link TriJoiner}, ... instance
 * for use in {@link UniConstraintStream#join(Class, BiJoiner)}, ...
 */
public final class Joiners {

    // TODO Support using non-natural comparators, such as lessThan(leftMapping, rightMapping, comparator).

    // ************************************************************************
    // BiJoiner
    // ************************************************************************

    public static <A> BiJoiner<A, A> equal() {
        return equal(Function.identity());
    }

    public static <A, Property_> BiJoiner<A, A> equal(Function<A, Property_> mapping) {
        return equal(mapping, mapping);
    }

    public static <A, B, Property_> BiJoiner<A, B> equal(Function<A, Property_> leftMapping,
            Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> lessThan(Function<A, Property_> mapping) {
        return lessThan(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> lessThan(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> lessThanOrEqual(
            Function<A, Property_> mapping) {
        return lessThanOrEqual(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> lessThanOrEqual(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> greaterThan(
            Function<A, Property_> mapping) {
        return greaterThan(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> greaterThan(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> greaterThanOrEqual(
            Function<A, Property_> mapping) {
        return greaterThanOrEqual(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> greaterThanOrEqual(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * Applies a filter to the joined tuple, with the semantics of {@link BiConstraintStream#filter(BiPredicate)}.
     *
     * @param filter never null, filter to apply
     * @param <A> type of the first fact in the tuple
     * @param <B> type of the second fact in the tuple
     * @return never null
     */
    public static <A, B> BiJoiner<A, B> filtering(BiPredicate<A, B> filter) {
        return new FilteringBiJoiner<>(filter);
    }

    /*
     * // TODO implement these joiners
     * public static <A, B, Property_> BiJoiner<A, B> containing(
     * Function<A, ? extends Collection<Property_>> leftMapping, Function <B, Property_> rightMapping) {
     * return new SingleBiJoiner<>(leftMapping, JoinerType.CONTAINING, rightMapping);
     * }
     *
     * // TODO containedBy (inverse contains relationship)
     *
     * public static <A, Property_> BiJoiner<A, A> intersecting(
     * Function<A, ? extends Collection<Property_>> mapping) {
     * return intersecting(mapping, mapping);
     * }
     *
     * public static <A, B, Property_> BiJoiner<A, B> intersecting(
     * Function<A, ? extends Collection<Property_>> leftMapping,
     * Function <B, ? extends Collection<Property_>> rightMapping) {
     * return new SingleBiJoiner<>(leftMapping, JoinerType.INTERSECTING, rightMapping);
     * }
     *
     * public static <A, Property_> BiJoiner<A, A> disjoint(Function<A, ? extends Collection<Property_>> mapping) {
     * return disjoint(mapping, mapping);
     * }
     *
     * public static <A, B, Property_> BiJoiner<A, B> disjoint(Function<A, ? extends Collection<Property_>> leftMapping,
     * Function <B, ? extends Collection<Property_>> rightMapping) {
     * return new SingleBiJoiner<>(leftMapping, JoinerType.DISJOINT, rightMapping);
     * }
     */

    /**
     * For pairs of intervals [a,b) and [c,d), returns only those pairs where [a,b) and [c,d) overlap (or, in other
     * words, there exists an x such that x in [a,b) and x in [c,d)).
     * These are exactly the pairs where {@code a < d} and {@code b > c} (or in other words, when the first interval
     * starts before the second one ends and the first interval ends after the second interval begins).
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first argument to the first interval start point
     * @param leftEndMapping maps the first argument to the first interval end point
     * @param rightStartMapping maps the second argument to the second interval start point
     * @param rightEndMapping maps the second argument to the second interval end point
     * 
     * @return never null, an indexed joiner that filters the constraint stream to only include elements (A,B) where
     *         A's and B's intervals (as defined by the function mapping) overlap
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
     * @param <A> the type of both the first and second argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param startMapping maps the argument to the start point of its interval
     * @param endMapping maps the argument to the end point of its interval
     * 
     * @return see {@link #overlaps(Function, Function, Function, Function)}
     */
    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> overlaps(
            Function<A, Property_> startMapping,
            Function<A, Property_> endMapping) {
        return overlaps(startMapping, endMapping, startMapping, endMapping);
    }

    /**
     * For pairs of intervals [a,b) and [c,d), returns only those pairs where [a,b) contains [c,d) (or, in other
     * words, for any x in [c, d), x is also in [a,b)).
     * These are exactly the pairs where {@code a <= c} and {@code b >= d} (or in other words, when the first interval
     * starts before the second one starts and the first interval ends after the second interval ends).
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first argument to the first interval start point
     * @param leftEndMapping maps the first argument to the first interval end point
     * @param rightStartMapping maps the second argument to the second interval start point
     * @param rightEndMapping maps the second argument to the second interval end point
     * 
     * @return never null, an indexed joiner that filters the constraint stream to only include elements (A,B) where
     *         A's interval contains B's interval (as defined by the function mapping)
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
     * @param <A> the type of both the first and second argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param startMapping maps the argument to the start point of its interval
     * @param endMapping maps the argument to the end point of its interval
     * 
     * @return see {@link #during(Function, Function, Function, Function)}
     */
    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> during(
            Function<A, Property_> startMapping,
            Function<A, Property_> endMapping) {
        return during(startMapping, endMapping, startMapping, endMapping);
    }

    // ************************************************************************
    // TriJoiner
    // ************************************************************************

    public static <A, B, C, Property_> TriJoiner<A, B, C> equal(BiFunction<A, B, Property_> leftMapping,
            Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> lessThan(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> lessThanOrEqual(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> greaterThan(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> greaterThanOrEqual(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C> TriJoiner<A, B, C> filtering(TriPredicate<A, B, C> filter) {
        return new FilteringTriJoiner<>(filter);
    }

    /**
     * Like {@link #overlaps(Function, Function, Function, Function)}, but for TriConstraintStream.
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first and second arguments to the first interval start point
     * @param leftEndMapping maps the first and second arguments to the first interval end point
     * @param rightStartMapping maps the third argument to the second interval start point
     * @param rightEndMapping maps the third argument to the second interval end point
     * 
     * @return never null, an indexed joiner that filters the constraint stream to only include elements (A,B,C) where
     *         (A,B)'s and C's intervals (as defined by the function mapping) overlap
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
     * Like {@link #during(Function, Function, Function, Function)}, but for TriConstraintStream.
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first and second arguments to the first interval start point
     * @param leftEndMapping maps the first and second arguments to the first interval end point
     * @param rightStartMapping maps the third argument to the second interval start point
     * @param rightEndMapping maps the third argument to the second interval end point
     * 
     * @return never null, an indexed joiner that filters the constraint stream to only include elements (A,B,C) where
     *         (A,B)'s interval contains C's interval (as defined by the function mapping)
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> during(
            BiFunction<A, B, Property_> leftStartMapping,
            BiFunction<A, B, Property_> leftEndMapping,
            Function<C, Property_> rightStartMapping,
            Function<C, Property_> rightEndMapping) {
        return AbstractTriJoiner.merge(Joiners.lessThanOrEqual(leftStartMapping, rightStartMapping),
                Joiners.greaterThanOrEqual(leftEndMapping, rightEndMapping));
    }

    // ************************************************************************
    // QuadJoiner
    // ************************************************************************

    public static <A, B, C, D, Property_> QuadJoiner<A, B, C, D> equal(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> lessThan(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> lessThanOrEqual(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> greaterThan(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> greaterThanOrEqual(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C, D> QuadJoiner<A, B, C, D> filtering(QuadPredicate<A, B, C, D> filter) {
        return new FilteringQuadJoiner<>(filter);
    }

    /**
     * Like {@link #overlaps(Function, Function, Function, Function)}, but for QuadConstraintStream.
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <D> the type of the fourth argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first, second and third arguments to the first interval start point
     * @param leftEndMapping maps the first, second and third arguments to the first interval end point
     * @param rightStartMapping maps the fourth argument to the second interval start point
     * @param rightEndMapping maps the fourth argument to the second interval end point
     * 
     * @return never null, an indexed joiner that filters the constraint stream to only include elements (A,B,C,D)
     *         where (A,B,C)'s and D's intervals (as defined by the function mapping) overlap
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
     * Like {@link #during(Function, Function, Function, Function)}, but for QuadConstraintStream.
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <D> the type of the fourth argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first, second and third arguments to the first interval start point
     * @param leftEndMapping maps the first, second and third arguments to the first interval end point
     * @param rightStartMapping maps the fourth argument to the second interval start point
     * @param rightEndMapping maps the fourth argument to the second interval end point
     * 
     * @return nerver null, an indexed joiner that filters the constraint stream to only include elements (A,B,C,D)
     *         where (A,B,C)'s interval contains D's interval (as defined by the function mapping)
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> during(
            TriFunction<A, B, C, Property_> leftStartMapping,
            TriFunction<A, B, C, Property_> leftEndMapping,
            Function<D, Property_> rightStartMapping,
            Function<D, Property_> rightEndMapping) {
        return AbstractQuadJoiner.merge(Joiners.lessThanOrEqual(leftStartMapping, rightStartMapping),
                Joiners.greaterThanOrEqual(leftEndMapping, rightEndMapping));
    }

    // ************************************************************************
    // PentaJoiner
    // ************************************************************************

    public static <A, B, C, D, E, Property_> PentaJoiner<A, B, C, D, E> equal(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new SinglePentaJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> PentaJoiner<A, B, C, D, E> lessThan(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new SinglePentaJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> PentaJoiner<A, B, C, D, E> lessThanOrEqual(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new SinglePentaJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> PentaJoiner<A, B, C, D, E> greaterThan(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new SinglePentaJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> PentaJoiner<A, B, C, D, E> greaterThanOrEqual(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new SinglePentaJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C, D, E> PentaJoiner<A, B, C, D, E> filtering(PentaPredicate<A, B, C, D, E> filter) {
        return new FilteringPentaJoiner<>(filter);
    }

    /**
     * Like {@link #overlaps(Function, Function, Function, Function)}, but for PentaConstraintStream.
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <D> the type of the fourth argument
     * @param <E> the type of the fifth argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first, second, third and fourth arguments to the first interval start point
     * @param leftEndMapping maps the first, second, third and fourth arguments to the first interval end point
     * @param rightStartMapping maps the fifth argument to the second interval start point
     * @param rightEndMapping maps the fifth argument to the second interval end point
     * 
     * @return never null, an indexed joiner that filters the constraint stream to only include elements (A,B,C,D,E)
     *         where (A,B,C,D)'s and E's intervals (as defined by the function mapping) overlap
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
     * Like {@link #during(Function, Function, Function, Function)}, but for PentaConstraintStream.
     * 
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <D> the type of the fourth argument
     * @param <E> the type of the fifth argument
     * @param <Property_> the type used to define the interval, comparable
     * 
     * @param leftStartMapping maps the first, second, third and fourth arguments to the first interval start point
     * @param leftEndMapping maps the first, second, third and fourth arguments to the first interval end point
     * @param rightStartMapping maps the fifth argument to the second interval start point
     * @param rightEndMapping maps the fifth argument to the second interval end point
     * 
     * @return never null, an indexed joiner that filters the constraint stream to only include elements (A,B,C,D,E)
     *         where (A,B,C,D)'s interval contains E's interval (as defined by the function mapping)
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> PentaJoiner<A, B, C, D, E> during(
            QuadFunction<A, B, C, D, Property_> leftStartMapping,
            QuadFunction<A, B, C, D, Property_> leftEndMapping,
            Function<E, Property_> rightStartMapping,
            Function<E, Property_> rightEndMapping) {
        return AbstractPentaJoiner.merge(Joiners.lessThanOrEqual(leftStartMapping, rightStartMapping),
                Joiners.greaterThanOrEqual(leftEndMapping, rightEndMapping));
    }

    private Joiners() {
    }

}
