/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.stream.uni;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintStream;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.impl.score.stream.bi.NoneBiJoiner;
import org.optaplanner.core.impl.score.stream.uni.UniConstraintStreamHelper;

/**
 * A {@link ConstraintStream} that matches one fact.
 *
 * @param <A> the type of the first and only fact in the tuple.
 * @see ConstraintStream
 */
public interface UniConstraintStream<A> extends ConstraintStream {

    // ************************************************************************
    // Filter
    // ************************************************************************

    /**
     * Exhaustively test each fact against the {@link Predicate}
     * and match if {@link Predicate#test(Object)} returns true.
     *
     * @param predicate never null
     * @return never null
     */
    UniConstraintStream<A> filter(Predicate<A> predicate);

    // ************************************************************************
    // Join
    // ************************************************************************

    /**
     * Create a new {@link BiConstraintStream} for every combination of A and B.
     * <p>
     * Important: {@link BiConstraintStream#filter(BiPredicate) Filtering} this is slower and less scalable
     * than a {@link #join(UniConstraintStream, BiJoiner)},
     * because it doesn't apply hashing and/or indexing on the properties,
     * so it creates and checks every combination of A and B.
     *
     * @param otherStream never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B
     */
    default <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream) {
        return join(otherStream, new NoneBiJoiner<>());
    }

    /**
     * Create a new {@link BiConstraintStream} for every combination of A and B for which the {@link BiJoiner}
     * is true (for the properties it extracts from both facts).
     * <p>
     * Important: This is faster and more scalable than a {@link #join(UniConstraintStream) join}
     * followed by a {@link BiConstraintStream#filter(BiPredicate) filter},
     * because it applies hashing and/or indexing on the properties,
     * so it doesn't create nor checks every combination of A and B.
     *
     * @param otherStream never null
     * @param joiner never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which the {@link BiJoiner} is true
     */
    <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream, BiJoiner<A, B> joiner);

    /**
     * As defined by {@link #join(UniConstraintStream, BiJoiner)}.
     * For performance reasons, indexing joiners must be placed before filtering joiners.
     *
     * @param otherStream never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream, BiJoiner<A, B> joiner1,
            BiJoiner<A, B> joiner2) {
        return join(otherStream, new BiJoiner[] { joiner1, joiner2 });
    }

    /**
     * As defined by {@link #join(UniConstraintStream, BiJoiner)}.
     * For performance reasons, indexing joiners must be placed before filtering joiners.
     *
     * @param otherStream never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream, BiJoiner<A, B> joiner1,
            BiJoiner<A, B> joiner2, BiJoiner<A, B> joiner3) {
        return join(otherStream, new BiJoiner[] { joiner1, joiner2, joiner3 });
    }

    /**
     * As defined by {@link #join(UniConstraintStream, BiJoiner)}.
     * For performance reasons, indexing joiners must be placed before filtering joiners.
     *
     * @param otherStream never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param joiner4 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream, BiJoiner<A, B> joiner1,
            BiJoiner<A, B> joiner2, BiJoiner<A, B> joiner3, BiJoiner<A, B> joiner4) {
        return join(otherStream, new BiJoiner[] { joiner1, joiner2, joiner3, joiner4 });
    }

    /**
     * As defined by {@link #join(UniConstraintStream, BiJoiner)}.
     * If multiple {@link BiJoiner}s are provided, for performance reasons, the indexing joiners must be placed before
     * filtering joiners.
     * <p>
     * This method causes <i>Unchecked generics array creation for varargs parameter</i> warnings,
     * but we can't fix it with a {@link SafeVarargs} annotation because it's an interface method.
     * Therefore, there are overloaded methods with up to 4 {@link BiJoiner} parameters.
     *
     * @param otherStream never null
     * @param joiners never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream, BiJoiner<A, B>... joiners) {
        UniConstraintStreamHelper<A, B> helper = new UniConstraintStreamHelper<>(this);
        return helper.join(otherStream, joiners);
    }

    /**
     * Create a new {@link BiConstraintStream} for every combination of A and B.
     * <p>
     * Important: {@link BiConstraintStream#filter(BiPredicate) Filtering} this is slower and less scalable
     * than a {@link #join(Class, BiJoiner)},
     * because it doesn't apply hashing and/or indexing on the properties,
     * so it creates and checks every combination of A and B.
     * <p>
     * This method is syntactic sugar for {@link #join(UniConstraintStream)}.
     *
     * @param otherClass never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B
     */
    default <B> BiConstraintStream<A, B> join(Class<B> otherClass) {
        return join(otherClass, new NoneBiJoiner<>());
    }

    /**
     * Create a new {@link BiConstraintStream} for every combination of A and B
     * for which the {@link BiJoiner} is true (for the properties it extracts from both facts).
     * <p>
     * Important: This is faster and more scalable than a {@link #join(Class) join}
     * followed by a {@link BiConstraintStream#filter(BiPredicate) filter},
     * because it applies hashing and/or indexing on the properties,
     * so it doesn't create nor checks every combination of A and B.
     * <p>
     * This method is syntactic sugar for {@link #join(UniConstraintStream, BiJoiner)}.
     * <p>
     * This method has overloaded methods with multiple {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiner never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which the {@link BiJoiner} is true
     */
    default <B> BiConstraintStream<A, B> join(Class<B> otherClass, BiJoiner<A, B> joiner) {
        return join(getConstraintFactory().from(otherClass), joiner);
    }

    /**
     * As defined by {@link #join(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed before
     * filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2) {
        return join(getConstraintFactory().from(otherClass), joiner1, joiner2);
    }

    /**
     * As defined by {@link #join(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed before
     * filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2,
            BiJoiner<A, B> joiner3) {
        return join(getConstraintFactory().from(otherClass), joiner1, joiner2, joiner3);
    }

    /**
     * As defined by {@link #join(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed before
     * filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param joiner4 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2,
            BiJoiner<A, B> joiner3, BiJoiner<A, B> joiner4) {
        return join(getConstraintFactory().from(otherClass), joiner1, joiner2, joiner3, joiner4);
    }

    /**
     * As defined by {@link #join(Class, BiJoiner)}. If multiple {@link BiJoiner}s are provided, for performance
     * reasons, the indexing joiners must be placed before filtering joiners.
     * <p>
     * This method causes <i>Unchecked generics array creation for varargs parameter</i> warnings,
     * but we can't fix it with a {@link SafeVarargs} annotation because it's an interface method.
     * Therefore, there are overloaded methods with up to 4 {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiners never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every combination of A and B for which all the {@link BiJoiner joiners}
     *         are true
     */
    default <B> BiConstraintStream<A, B> join(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return join(getConstraintFactory().from(otherClass), joiners);
    }

    // ************************************************************************
    // If (not) exists
    // ************************************************************************

    /**
     * Create a new {@link UniConstraintStream} for every A where B exists for which the {@link BiJoiner} is true
     * (for the properties it extracts from both facts).
     * <p>
     * This method has overloaded methods with multiple {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiner never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B exists for which the {@link BiJoiner} is true
     */
    default <B> UniConstraintStream<A> ifExists(Class<B> otherClass, BiJoiner<A, B> joiner) {
        return ifExists(otherClass, new BiJoiner[] { joiner });
    }

    /**
     * As defined by {@link #ifExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed before
     * filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B exists for which all the {@link BiJoiner}s are true
     */
    default <B> UniConstraintStream<A> ifExists(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2) {
        return ifExists(otherClass, new BiJoiner[] { joiner1, joiner2 });
    }

    /**
     * As defined by {@link #ifExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed before
     * filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B exists for which all the {@link BiJoiner}s are true
     */
    default <B> UniConstraintStream<A> ifExists(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2,
            BiJoiner<A, B> joiner3) {
        return ifExists(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3 });
    }

    /**
     * As defined by {@link #ifExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed before
     * filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param joiner4 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B exists for which all the {@link BiJoiner}s are true
     */
    default <B> UniConstraintStream<A> ifExists(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2,
            BiJoiner<A, B> joiner3, BiJoiner<A, B> joiner4) {
        return ifExists(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3, joiner4 });
    }

    /**
     * As defined by {@link #ifExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed before
     * filtering joiners.
     * <p>
     * This method causes <i>Unchecked generics array creation for varargs parameter</i> warnings,
     * but we can't fix it with a {@link SafeVarargs} annotation because it's an interface method.
     * Therefore, there are overloaded methods with up to 4 {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiners never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B exists for which all the {@link BiJoiner}s are true
     */
    <B> UniConstraintStream<A> ifExists(Class<B> otherClass, BiJoiner<A, B>... joiners);

    /**
     * Create a new {@link UniConstraintStream} for every A, if another A exists that does not {@link Object#equals(Object)}
     * the first.
     *
     * @param otherClass never null
     * @return never null, a stream that matches every A where a different A exists
     */
    default UniConstraintStream<A> ifExistsOther(Class<A> otherClass) {
        return ifExists(otherClass, Joiners.filtering(ObjectUtils::notEqual));
    }

    /**
     * Create a new {@link UniConstraintStream} for every A, if another A exists that does not {@link Object#equals(Object)}
     * the first, and for which the {@link BiJoiner} is true (for the properties it extracts from both facts).
     * <p>
     * This method has overloaded methods with multiple {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiner never null
     * @return never null, a stream that matches every A where a different A exists for which the {@link BiJoiner} is
     *         true
     */
    default UniConstraintStream<A> ifExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner) {
        return ifExistsOther(otherClass, new BiJoiner[] { joiner });
    }

    /**
     * As defined by {@link #ifExistsOther(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed
     * before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @return never null, a stream that matches every A where a different A exists for which all the {@link BiJoiner}s
     *         are true
     */
    default UniConstraintStream<A> ifExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner1, BiJoiner<A, A> joiner2) {
        return ifExistsOther(otherClass, new BiJoiner[] { joiner1, joiner2 });
    }

    /**
     * As defined by {@link #ifExistsOther(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed
     * before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @return never null, a stream that matches every A where a different A exists for which all the {@link BiJoiner}s
     *         are true
     */
    default UniConstraintStream<A> ifExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner1, BiJoiner<A, A> joiner2,
            BiJoiner<A, A> joiner3) {
        return ifExistsOther(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3 });
    }

    /**
     * As defined by {@link #ifExistsOther(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed
     * before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param joiner4 never null
     * @return never null, a stream that matches every A where a different A exists for which all the {@link BiJoiner}s
     *         are true
     */
    default UniConstraintStream<A> ifExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner1, BiJoiner<A, A> joiner2,
            BiJoiner<A, A> joiner3, BiJoiner<A, A> joiner4) {
        return ifExistsOther(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3, joiner4 });
    }

    /**
     * As defined by {@link #ifExistsOther(Class, BiJoiner)}. If multiple {@link BiJoiner}s are provided,
     * for performance reasons, the indexing joiners must be placed before filtering joiners.
     * <p>
     * This method causes <i>Unchecked generics array creation for varargs parameter</i> warnings,
     * but we can't fix it with a {@link SafeVarargs} annotation because it's an interface method.
     * Therefore, there are overloaded methods with up to 4 {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiners never null
     * @return never null, a stream that matches every A where a different A exists for which all the {@link BiJoiner}s
     *         are true
     */
    default UniConstraintStream<A> ifExistsOther(Class<A> otherClass, BiJoiner<A, A>... joiners) {
        BiJoiner<A, A> otherness = Joiners.filtering(ObjectUtils::notEqual);
        BiJoiner[] allJoiners = Stream.concat(Arrays.stream(joiners), Stream.of(otherness))
                .toArray(BiJoiner[]::new);
        return ifExists(otherClass, allJoiners);
    }

    /**
     * Create a new {@link UniConstraintStream} for every A where B does not exist for which the {@link BiJoiner} is
     * true (for the properties it extracts from both facts).
     * <p>
     * This method has overloaded methods with multiple {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiner never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B does not exist for which the {@link BiJoiner} is true
     */
    default <B> UniConstraintStream<A> ifNotExists(Class<B> otherClass, BiJoiner<A, B> joiner) {
        return ifNotExists(otherClass, new BiJoiner[] { joiner });
    }

    /**
     * As defined by {@link #ifNotExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed
     * before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B does not exist for which all the {@link BiJoiner}s are
     *         true
     */
    default <B> UniConstraintStream<A> ifNotExists(Class<B> otherClass, BiJoiner<A, B> joiner1,
            BiJoiner<A, B> joiner2) {
        return ifNotExists(otherClass, new BiJoiner[] { joiner1, joiner2 });
    }

    /**
     * As defined by {@link #ifNotExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed
     * before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B does not exist for which all the {@link BiJoiner}s are
     *         true
     */
    default <B> UniConstraintStream<A> ifNotExists(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2,
            BiJoiner<A, B> joiner3) {
        return ifNotExists(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3 });
    }

    /**
     * As defined by {@link #ifNotExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed
     * before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param joiner4 never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B does not exist for which all the {@link BiJoiner}s are
     *         true
     */
    default <B> UniConstraintStream<A> ifNotExists(Class<B> otherClass, BiJoiner<A, B> joiner1, BiJoiner<A, B> joiner2,
            BiJoiner<A, B> joiner3, BiJoiner<A, B> joiner4) {
        return ifNotExists(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3, joiner4 });
    }

    /**
     * As defined by {@link #ifNotExists(Class, BiJoiner)}. For performance reasons, indexing joiners must be placed
     * before filtering joiners.
     * <p>
     * This method causes <i>Unchecked generics array creation for varargs parameter</i> warnings,
     * but we can't fix it with a {@link SafeVarargs} annotation because it's an interface method.
     * Therefore, there are overloaded methods with up to 4 {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiners never null
     * @param <B> the type of the second matched fact
     * @return never null, a stream that matches every A where B does not exist for which all the {@link BiJoiner}s are
     *         true
     */
    <B> UniConstraintStream<A> ifNotExists(Class<B> otherClass, BiJoiner<A, B>... joiners);

    /**
     * Create a new {@link UniConstraintStream} for every A, if no other A exists that does not {@link Object#equals(Object)}
     * the first.
     *
     * @param otherClass never null
     * @return never null, a stream that matches every A where a different A does not exist
     */
    default UniConstraintStream<A> ifNotExistsOther(Class<A> otherClass) {
        return ifNotExists(otherClass, Joiners.filtering(ObjectUtils::notEqual));
    }

    /**
     * Create a new {@link UniConstraintStream} for every A, if no other A exists that does not {@link Object#equals(Object)}
     * the first, and for which the {@link BiJoiner} is true (for the properties it extracts from both facts).
     * <p>
     * This method has overloaded methods with multiple {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiner never null
     * @return never null, a stream that matches every A where a different A does not exist for which the
     *         {@link BiJoiner} is true
     */
    default UniConstraintStream<A> ifNotExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner) {
        return ifNotExistsOther(otherClass, new BiJoiner[] { joiner });
    }

    /**
     * As defined by {@link #ifNotExistsOther(Class, BiJoiner)}. For performance reasons, indexing joiners must be
     * placed before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @return never null, a stream that matches every A where a different A does not exist for which all the
     *         {@link BiJoiner}s are true
     */
    default UniConstraintStream<A> ifNotExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner1,
            BiJoiner<A, A> joiner2) {
        return ifNotExistsOther(otherClass, new BiJoiner[] { joiner1, joiner2 });
    }

    /**
     * As defined by {@link #ifNotExistsOther(Class, BiJoiner)}. For performance reasons, indexing joiners must be
     * placed before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @return never null, a stream that matches every A where a different A does not exist for which all the
     *         {@link BiJoiner}s are true
     */
    default UniConstraintStream<A> ifNotExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner1, BiJoiner<A, A> joiner2,
            BiJoiner<A, A> joiner3) {
        return ifNotExistsOther(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3 });
    }

    /**
     * As defined by {@link #ifNotExistsOther(Class, BiJoiner)}. For performance reasons, indexing joiners must be
     * placed before filtering joiners.
     *
     * @param otherClass never null
     * @param joiner1 never null
     * @param joiner2 never null
     * @param joiner3 never null
     * @param joiner4 never null
     * @return never null, a stream that matches every A where a different A does not exist for which all the
     *         {@link BiJoiner}s are true
     */
    default UniConstraintStream<A> ifNotExistsOther(Class<A> otherClass, BiJoiner<A, A> joiner1, BiJoiner<A, A> joiner2,
            BiJoiner<A, A> joiner3, BiJoiner<A, A> joiner4) {
        return ifNotExistsOther(otherClass, new BiJoiner[] { joiner1, joiner2, joiner3, joiner4 });
    }

    /**
     * As defined by {@link #ifNotExistsOther(Class, BiJoiner)}. If multiple {@link BiJoiner}s are provided, for
     * performance reasons, the indexing joiners must be placed before filtering joiners.
     * <p>
     * This method causes <i>Unchecked generics array creation for varargs parameter</i> warnings,
     * but we can't fix it with a {@link SafeVarargs} annotation because it's an interface method.
     * Therefore, there are overloaded methods with up to 4 {@link BiJoiner} parameters.
     *
     * @param otherClass never null
     * @param joiners never null
     * @return never null, a stream that matches every A where a different A does not exist for which all the
     *         {@link BiJoiner}s are true
     */
    default UniConstraintStream<A> ifNotExistsOther(Class<A> otherClass, BiJoiner<A, A>... joiners) {
        BiJoiner<A, A> otherness = Joiners.filtering(ObjectUtils::notEqual);
        BiJoiner[] allJoiners = Stream.concat(Arrays.stream(joiners), Stream.of(otherness))
                .toArray(BiJoiner[]::new);
        return ifNotExists(otherClass, allJoiners);
    }

    // ************************************************************************
    // Group by
    // ************************************************************************

    /**
     * Convert the {@link UniConstraintStream} to a different {@link UniConstraintStream}, containing only a single
     * tuple, the result of applying {@link UniConstraintCollector}.
     *
     * @param collector never null, the collector to perform the grouping operation with
     *        See {@link ConstraintCollectors} for common operations, such as {@code count()}, {@code sum()} and others.
     * @param <ResultContainer_> the mutable accumulation type (often hidden as an implementation detail)
     * @param <Result_> the type of a fact in the destination {@link UniConstraintStream}'s tuple
     * @return never null
     */
    <ResultContainer_, Result_> UniConstraintStream<Result_> groupBy(
            UniConstraintCollector<A, ResultContainer_, Result_> collector);

    /**
     * Convert the {@link UniConstraintStream} to a different {@link UniConstraintStream}, containing the set of tuples
     * resulting from applying the group key mapping function on all tuples of the original stream.
     * Neither tuple of the new stream {@link Objects#equals(Object, Object)} any other.
     *
     * @param groupKeyMapping never null, mapping function to convert each element in the stream to a different element
     * @param <GroupKey_> the type of a fact in the destination {@link UniConstraintStream}'s tuple
     * @return never null
     */
    <GroupKey_> UniConstraintStream<GroupKey_> groupBy(Function<A, GroupKey_> groupKeyMapping);

    /**
     * Convert the {@link UniConstraintStream} to a {@link BiConstraintStream}, consisting of unique tuples with two
     * facts.
     * <p>
     * The first fact is the return value of the first group key mapping function, applied on the incoming tuple.
     * The second fact is the return value of a given {@link UniConstraintCollector} applied on all incoming tuples with
     * the same first fact.
     *
     * @param groupKeyMapping never null, function to convert the fact in the original tuple to a different fact
     * @param collector never null, the collector to perform the grouping operation with
     *        See {@link ConstraintCollectors} for common operations, such as {@code count()}, {@code sum()} and others.
     * @param <GroupKey_> the type of the first fact in the destination {@link BiConstraintStream}'s tuple
     * @param <ResultContainer_> the mutable accumulation type (often hidden as an implementation detail)
     * @param <Result_> the type of the second fact in the destination {@link BiConstraintStream}'s tuple
     * @return never null
     */
    <GroupKey_, ResultContainer_, Result_> BiConstraintStream<GroupKey_, Result_> groupBy(
            Function<A, GroupKey_> groupKeyMapping,
            UniConstraintCollector<A, ResultContainer_, Result_> collector);

    /**
     * Convert the {@link UniConstraintStream} to a {@link BiConstraintStream}, consisting of unique tuples with two
     * facts.
     * <p>
     * The first fact is the return value of the first group key mapping function, applied on the incoming tuple.
     * The second fact is the return value of the second group key mapping function, applied on all incoming tuples with
     * the same first fact.
     *
     * @param groupKeyAMapping never null, function to convert the original tuple into a first fact
     * @param groupKeyBMapping never null, function to convert the original tuple into a second fact
     * @param <GroupKeyA_> the type of the first fact in the destination {@link BiConstraintStream}'s tuple
     * @param <GroupKeyB_> the type of the second fact in the destination {@link BiConstraintStream}'s tuple
     * @return never null
     */
    <GroupKeyA_, GroupKeyB_> BiConstraintStream<GroupKeyA_, GroupKeyB_> groupBy(
            Function<A, GroupKeyA_> groupKeyAMapping, Function<A, GroupKeyB_> groupKeyBMapping);

    /**
     * Combines the semantics of {@link #groupBy(Function, Function)} and {@link #groupBy(UniConstraintCollector)}.
     * That is, the first and second facts in the tuple follow the {@link #groupBy(Function, Function)} semantics, and
     * the third fact is the result of applying {@link UniConstraintCollector#finisher()} on all the tuples of the
     * original {@link UniConstraintStream} that belong to the group.
     *
     * @param groupKeyAMapping never null, function to convert the original tuple into a first fact
     * @param groupKeyBMapping never null, function to convert the original tuple into a second fact
     * @param collector never null, the collector to perform the grouping operation with
     *        See {@link ConstraintCollectors} for common operations, such as {@code count()}, {@code sum()} and others.
     * @param <GroupKeyA_> the type of the first fact in the destination {@link TriConstraintStream}'s tuple
     * @param <GroupKeyB_> the type of the second fact in the destination {@link TriConstraintStream}'s tuple
     * @param <ResultContainer_> the mutable accumulation type (often hidden as an implementation detail)
     * @param <Result_> the type of the third fact in the destination {@link TriConstraintStream}'s tuple
     * @return never null
     */
    <GroupKeyA_, GroupKeyB_, ResultContainer_, Result_> TriConstraintStream<GroupKeyA_, GroupKeyB_, Result_> groupBy(
            Function<A, GroupKeyA_> groupKeyAMapping, Function<A, GroupKeyB_> groupKeyBMapping,
            UniConstraintCollector<A, ResultContainer_, Result_> collector);

    /**
     * Combines the semantics of {@link #groupBy(Function, Function)} and {@link #groupBy(UniConstraintCollector)}.
     * That is, the first and second facts in the tuple follow the {@link #groupBy(Function, Function)} semantics.
     * The third fact is the result of applying the first {@link UniConstraintCollector#finisher()} on all the tuples
     * of the original {@link UniConstraintStream} that belong to the group.
     * The fourth fact is the result of applying the second {@link UniConstraintCollector#finisher()} on all the tuples
     * of the original {@link UniConstraintStream} that belong to the group
     *
     * @param groupKeyAMapping never null, function to convert the original tuple into a first fact
     * @param groupKeyBMapping never null, function to convert the original tuple into a second fact
     * @param collectorC never null, the collector to perform the first grouping operation with
     *        See {@link ConstraintCollectors} for common operations, such as {@code count()}, {@code sum()} and others.
     * @param collectorD never null, the collector to perform the second grouping operation with
     *        See {@link ConstraintCollectors} for common operations, such as {@code count()}, {@code sum()} and others.
     * @param <GroupKeyA_> the type of the first fact in the destination {@link QuadConstraintStream}'s tuple
     * @param <GroupKeyB_> the type of the second fact in the destination {@link QuadConstraintStream}'s tuple
     * @param <ResultContainerC_> the mutable accumulation type (often hidden as an implementation detail)
     * @param <ResultC_> the type of the third fact in the destination {@link QuadConstraintStream}'s tuple
     * @param <ResultContainerD_> the mutable accumulation type (often hidden as an implementation detail)
     * @param <ResultD_> the type of the fourth fact in the destination {@link QuadConstraintStream}'s tuple
     * @return never null
     */
    <GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(
                    Function<A, GroupKeyA_> groupKeyAMapping, Function<A, GroupKeyB_> groupKeyBMapping,
                    UniConstraintCollector<A, ResultContainerC_, ResultC_> collectorC,
                    UniConstraintCollector<A, ResultContainerD_, ResultD_> collectorD);

    // ************************************************************************
    // Penalize/reward
    // ************************************************************************

    /**
     * Negatively impact the {@link Score}: subtract the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #penalize(String, Score)}.
     * <p>
     * For non-int {@link Score} types use {@link #penalizeLong(String, Score, ToLongFunction)} or
     * {@link #penalizeBigDecimal(String, Score, Function)} instead.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint penalize(String constraintName, Score<?> constraintWeight, ToIntFunction<A> matchWeigher) {
        return penalize(getConstraintFactory().getDefaultConstraintPackage(), constraintName, constraintWeight,
                matchWeigher);
    }

    /**
     * As defined by {@link #penalize(String, Score, ToIntFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint penalize(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntFunction<A> matchWeigher);

    /**
     * Negatively impact the {@link Score}: subtract the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #penalize(String, Score)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint penalizeLong(String constraintName, Score<?> constraintWeight, ToLongFunction<A> matchWeigher) {
        return penalizeLong(getConstraintFactory().getDefaultConstraintPackage(), constraintName, constraintWeight,
                matchWeigher);
    }

    /**
     * As defined by {@link #penalizeLong(String, Score, ToLongFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint penalizeLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongFunction<A> matchWeigher);

    /**
     * Negatively impact the {@link Score}: subtract the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #penalize(String, Score)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint penalizeBigDecimal(String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher) {
        return penalizeBigDecimal(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                constraintWeight, matchWeigher);
    }

    /**
     * As defined by {@link #penalizeBigDecimal(String, Score, Function)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint penalizeBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher);

    /**
     * Negatively impact the {@link Score}: subtract the {@link ConstraintWeight} multiplied by the match weight.
     * Otherwise as defined by {@link #penalizeConfigurable(String)}.
     * <p>
     * For non-int {@link Score} types use {@link #penalizeConfigurableLong(String, ToLongFunction)} or
     * {@link #penalizeConfigurableBigDecimal(String, Function)} instead.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint penalizeConfigurable(String constraintName, ToIntFunction<A> matchWeigher) {
        return penalizeConfigurable(getConstraintFactory().getDefaultConstraintPackage(), constraintName, matchWeigher);
    }

    /**
     * As defined by {@link #penalizeConfigurable(String, ToIntFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint penalizeConfigurable(String constraintPackage, String constraintName, ToIntFunction<A> matchWeigher);

    /**
     * Negatively impact the {@link Score}: subtract the {@link ConstraintWeight} multiplied by the match weight.
     * Otherwise as defined by {@link #penalizeConfigurable(String)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint penalizeConfigurableLong(String constraintName, ToLongFunction<A> matchWeigher) {
        return penalizeConfigurableLong(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                matchWeigher);
    }

    /**
     * As defined by {@link #penalizeConfigurableLong(String, ToLongFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint penalizeConfigurableLong(String constraintPackage, String constraintName,
            ToLongFunction<A> matchWeigher);

    /**
     * Negatively impact the {@link Score}: subtract the {@link ConstraintWeight} multiplied by the match weight.
     * Otherwise as defined by {@link #penalizeConfigurable(String)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint penalizeConfigurableBigDecimal(String constraintName, Function<A, BigDecimal> matchWeigher) {
        return penalizeConfigurableBigDecimal(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                matchWeigher);
    }

    /**
     * As defined by {@link #penalizeConfigurableBigDecimal(String, Function)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint penalizeConfigurableBigDecimal(String constraintPackage, String constraintName,
            Function<A, BigDecimal> matchWeigher);

    /**
     * Positively impact the {@link Score}: add the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #reward(String, Score)}.
     * <p>
     * For non-int {@link Score} types use {@link #rewardLong(String, Score, ToLongFunction)} or
     * {@link #rewardBigDecimal(String, Score, Function)} instead.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint reward(String constraintName, Score<?> constraintWeight, ToIntFunction<A> matchWeigher) {
        return reward(getConstraintFactory().getDefaultConstraintPackage(), constraintName, constraintWeight,
                matchWeigher);
    }

    /**
     * As defined by {@link #reward(String, Score, ToIntFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint reward(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntFunction<A> matchWeigher);

    /**
     * Positively impact the {@link Score}: add the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #reward(String, Score)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint rewardLong(String constraintName, Score<?> constraintWeight, ToLongFunction<A> matchWeigher) {
        return rewardLong(getConstraintFactory().getDefaultConstraintPackage(), constraintName, constraintWeight,
                matchWeigher);
    }

    /**
     * As defined by {@link #rewardLong(String, Score, ToLongFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint rewardLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongFunction<A> matchWeigher);

    /**
     * Positively impact the {@link Score}: add the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #reward(String, Score)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint rewardBigDecimal(String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher) {
        return rewardBigDecimal(getConstraintFactory().getDefaultConstraintPackage(), constraintName, constraintWeight,
                matchWeigher);
    }

    /**
     * As defined by {@link #rewardBigDecimal(String, Score, Function)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint rewardBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher);

    /**
     * Positively impact the {@link Score}: add the {@link ConstraintWeight} multiplied by the match weight.
     * Otherwise as defined by {@link #rewardConfigurable(String)}.
     * <p>
     * For non-int {@link Score} types use {@link #rewardConfigurableLong(String, ToLongFunction)} or
     * {@link #rewardConfigurableBigDecimal(String, Function)} instead.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint rewardConfigurable(String constraintName, ToIntFunction<A> matchWeigher) {
        return rewardConfigurable(getConstraintFactory().getDefaultConstraintPackage(), constraintName, matchWeigher);
    }

    /**
     * As defined by {@link #rewardConfigurable(String, ToIntFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint rewardConfigurable(String constraintPackage, String constraintName, ToIntFunction<A> matchWeigher);

    /**
     * Positively impact the {@link Score}: add the {@link ConstraintWeight} multiplied by the match weight.
     * Otherwise as defined by {@link #rewardConfigurable(String)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint rewardConfigurableLong(String constraintName, ToLongFunction<A> matchWeigher) {
        return rewardConfigurableLong(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                matchWeigher);
    }

    /**
     * As defined by {@link #rewardConfigurableLong(String, ToLongFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint rewardConfigurableLong(String constraintPackage, String constraintName, ToLongFunction<A> matchWeigher);

    /**
     * Positively impact the {@link Score}: add the {@link ConstraintWeight} multiplied by the match weight.
     * Otherwise as defined by {@link #rewardConfigurable(String)}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint rewardConfigurableBigDecimal(String constraintName, Function<A, BigDecimal> matchWeigher) {
        return rewardConfigurableBigDecimal(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                matchWeigher);
    }

    /**
     * As defined by {@link #rewardConfigurableBigDecimal(String, Function)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint rewardConfigurableBigDecimal(String constraintPackage, String constraintName,
            Function<A, BigDecimal> matchWeigher);

    /**
     * Positively or negatively impact the {@link Score} by the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #impact(String, Score)}.
     * <p>
     * Use {@code penalize(...)} or {@code reward(...)} instead, unless this constraint can both have positive and negative
     * weights.
     * <p>
     * For non-int {@link Score} types use {@link #impactLong(String, Score, ToLongFunction)} or
     * {@link #impactBigDecimal(String, Score, Function)} instead.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint impact(String constraintName, Score<?> constraintWeight, ToIntFunction<A> matchWeigher) {
        return impact(getConstraintFactory().getDefaultConstraintPackage(), constraintName, constraintWeight,
                matchWeigher);
    }

    /**
     * As defined by {@link #impact(String, Score, ToIntFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint impact(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntFunction<A> matchWeigher);

    /**
     * Positively or negatively impact the {@link Score} by the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #impact(String, Score)}.
     * <p>
     * Use {@code penalizeLong(...)} or {@code rewardLong(...)} instead, unless this constraint can both have positive
     * and negative weights.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint impactLong(String constraintName, Score<?> constraintWeight, ToLongFunction<A> matchWeigher) {
        return impactLong(getConstraintFactory().getDefaultConstraintPackage(), constraintName, constraintWeight,
                matchWeigher);
    }

    /**
     * As defined by {@link #impactLong(String, Score, ToLongFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint impactLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongFunction<A> matchWeigher);

    /**
     * Positively or negatively impact the {@link Score} by the constraintWeight multiplied by the match weight.
     * Otherwise as defined by {@link #impact(String, Score)}.
     * <p>
     * Use {@code penalizeBigDecimal(...)} or {@code rewardBigDecimal(...)} instead, unless this constraint can both
     * have positive and negative weights.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param constraintWeight never null
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint impactBigDecimal(String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher) {
        return impactBigDecimal(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                constraintWeight, matchWeigher);
    }

    /**
     * As defined by {@link #impactBigDecimal(String, Score, Function)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint impactBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher);

    /**
     * Positively or negatively impact the {@link Score} by the {@link ConstraintWeight} multiplied by the match weight.
     * <p>
     * Use {@code penalizeConfigurable(...)} or {@code rewardConfigurable(...)} instead, unless this constraint can both
     * have positive and negative weights.
     * <p>
     * The constraintWeight comes from an {@link ConstraintWeight} annotated member on the
     * {@link ConstraintConfiguration}, so end users can change the constraint weights dynamically.
     * This constraint may be deactivated if the {@link ConstraintWeight} is zero.
     * If there is no {@link ConstraintConfiguration}, use {@link #impact(String, Score)} instead.
     * <p>
     * For non-int {@link Score} types use {@link #impactConfigurableLong(String, ToLongFunction)} or
     * {@link #impactConfigurableBigDecimal(String, Function)} instead.
     * <p>
     * The {@link Constraint#getConstraintPackage()} defaults to {@link ConstraintConfiguration#constraintPackage()}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint impactConfigurable(String constraintName, ToIntFunction<A> matchWeigher) {
        return impactConfigurable(getConstraintFactory().getDefaultConstraintPackage(), constraintName, matchWeigher);
    }

    /**
     * As defined by {@link #impactConfigurable(String, ToIntFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint impactConfigurable(String constraintPackage, String constraintName, ToIntFunction<A> matchWeigher);

    /**
     * Positively or negatively impact the {@link Score} by the {@link ConstraintWeight} multiplied by the match weight.
     * <p>
     * Use {@code penalizeConfigurableLong(...)} or {@code rewardConfigurableLong(...)} instead, unless this constraint
     * can both have positive and negative weights.
     * <p>
     * The constraintWeight comes from an {@link ConstraintWeight} annotated member on the
     * {@link ConstraintConfiguration}, so end users can change the constraint weights dynamically.
     * This constraint may be deactivated if the {@link ConstraintWeight} is zero.
     * If there is no {@link ConstraintConfiguration}, use {@link #impact(String, Score)} instead.
     * <p>
     * The {@link Constraint#getConstraintPackage()} defaults to {@link ConstraintConfiguration#constraintPackage()}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint impactConfigurableLong(String constraintName, ToLongFunction<A> matchWeigher) {
        return impactConfigurableLong(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                matchWeigher);
    }

    /**
     * As defined by {@link #impactConfigurableLong(String, ToLongFunction)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint impactConfigurableLong(String constraintPackage, String constraintName,
            ToLongFunction<A> matchWeigher);

    /**
     * Positively or negatively impact the {@link Score} by the {@link ConstraintWeight} multiplied by the match weight.
     * <p>
     * Use {@code penalizeConfigurableBigDecimal(...)} or {@code rewardConfigurableBigDecimal(...)} instead, unless this
     * constraint can both have positive and negative weights.
     * <p>
     * The constraintWeight comes from an {@link ConstraintWeight} annotated member on the
     * {@link ConstraintConfiguration}, so end users can change the constraint weights dynamically.
     * This constraint may be deactivated if the {@link ConstraintWeight} is zero.
     * If there is no {@link ConstraintConfiguration}, use {@link #impact(String, Score)} instead.
     * <p>
     * The {@link Constraint#getConstraintPackage()} defaults to {@link ConstraintConfiguration#constraintPackage()}.
     *
     * @param constraintName never null, shows up in {@link ConstraintMatchTotal} during score justification
     * @param matchWeigher never null, the result of this function (matchWeight) is multiplied by the constraintWeight
     * @return never null
     */
    default Constraint impactConfigurableBigDecimal(String constraintName, Function<A, BigDecimal> matchWeigher) {
        return impactConfigurableBigDecimal(getConstraintFactory().getDefaultConstraintPackage(), constraintName,
                matchWeigher);
    }

    /**
     * As defined by {@link #impactConfigurableBigDecimal(String, Function)}.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param matchWeigher never null
     * @return never null
     */
    Constraint impactConfigurableBigDecimal(String constraintPackage, String constraintName,
            Function<A, BigDecimal> matchWeigher);

}
