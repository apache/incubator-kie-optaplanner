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

import java.util.Arrays;

import org.optaplanner.core.api.score.stream.ConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintStream;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;

public final class JoinerUtils {

    public static <A, B> BiConstraintStream<A, B> join(UniConstraintStream<A> stream, Class<B> otherClass,
            BiJoiner<A, B>... joiners) {
        return join(new BiConstraintStreamDescriptor<>(stream), otherClass, joiners);
    }

    public static <A, B, C> TriConstraintStream<A, B, C> join(BiConstraintStream<A, B> stream, Class<C> otherClass,
            TriJoiner<A, B, C>... joiners) {
        return join(new TriConstraintStreamDescriptor<>(stream), otherClass, joiners);
    }

    public static <A, B, C, D> QuadConstraintStream<A, B, C, D> join(TriConstraintStream<A, B, C> stream,
            Class<D> otherClass, QuadJoiner<A, B, C, D>... joiners) {
        return join(new QuadConstraintStreamDescriptor<>(stream), otherClass, joiners);
    }

    private static <Right, JoinedStream extends ConstraintStream, Joiner, Predicate> JoinedStream join(
            ConstraintStreamHelper<Right, JoinedStream, Joiner, Predicate> constraintStreamDescriptor,
            Class<Right> otherClass, Joiner... joiners) {
        int joinerCount = joiners.length;
        int indexOfFirstFilter = -1;
        // Make sure all indexing joiners, if any, come before filtering joiners. This is necessary for performance.
        for (int i = 0; i < joinerCount; i++) {
            Joiner joiner = joiners[i];
            if (indexOfFirstFilter >= 0) {
                if (!(constraintStreamDescriptor.isFilteringJoiner(joiner))) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow " +
                            "a filtering joiner (" + joiners[indexOfFirstFilter] + ").\n" +
                            "Maybe reorder the joiners such that filtering() joiners are later in the parameter list.");
                }
            } else {
                if (constraintStreamDescriptor.isFilteringJoiner(joiner)) {
                    // From now on, we only allow filtering joiners.
                    indexOfFirstFilter = i;
                }
            }
        }
        if (indexOfFirstFilter < 0) { // Only found indexing joiners.
            Joiner mergedJoiners = constraintStreamDescriptor.mergeJoiners(joiners);
            return constraintStreamDescriptor.join(otherClass, mergedJoiners);
        }
        // Assemble the join stream that may be followed by filter stream.
        JoinedStream joined = indexOfFirstFilter == 0 ?
                constraintStreamDescriptor.join(otherClass) :
                constraintStreamDescriptor.join(otherClass, Arrays.copyOf(joiners, indexOfFirstFilter));
        int filterCount = joinerCount - indexOfFirstFilter;
        if (filterCount == 0) { // No filters, return the original join stream.
            return joined;
        }
        // We merge all filters into one, so that we don't pay the penalty for lack of indexing more than once.
        Joiner filteringJoiner = joiners[indexOfFirstFilter];
        Predicate resultingFilter = constraintStreamDescriptor.extractPredicate(filteringJoiner);
        for (int i = indexOfFirstFilter + 1; i < joinerCount; i++) {
            Joiner otherFilteringJoiner = joiners[i];
            Predicate otherFilter = constraintStreamDescriptor.extractPredicate(otherFilteringJoiner);
            resultingFilter = constraintStreamDescriptor.mergePredicates(resultingFilter, otherFilter);
        }
        return constraintStreamDescriptor.filter(joined, resultingFilter);
    }

    private JoinerUtils() {
    }

}
