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

package org.optaplanner.core.impl.score.stream.uni;

import java.util.function.BiPredicate;

import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.bi.FilteringBiJoiner;
import org.optaplanner.core.impl.score.stream.common.AbstractConstraintStreamHelper;

public final class UniConstraintStreamHelper<A, B>
        extends AbstractConstraintStreamHelper<B, BiConstraintStream<A, B>, BiJoiner<A, B>, BiPredicate<A, B>> {

    private final UniConstraintStream<A> stream;

    public UniConstraintStreamHelper(UniConstraintStream<A> stream) {
        this.stream = stream;
    }

    @Override
    protected BiConstraintStream<A, B> doJoin(Class<B> otherClass) {
        return stream.join(otherClass);
    }

    @Override
    protected BiConstraintStream<A, B> doJoin(Class<B> otherClass, BiJoiner<A, B> joiner) {
        return stream.join(otherClass, joiner);
    }

    @Override
    protected BiConstraintStream<A, B> doJoin(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return stream.join(otherClass, joiners);
    }

    @Override
    protected BiConstraintStream<A, B> filter(BiConstraintStream<A, B> stream, BiPredicate<A, B> predicate) {
        return stream.filter(predicate);
    }

    @Override
    protected BiJoiner<A, B> mergeJoiners(BiJoiner<A, B>... joiners) {
        return AbstractBiJoiner.merge(joiners);
    }

    @Override
    protected boolean isFilteringJoiner(BiJoiner<A, B> joiner) {
        return joiner instanceof FilteringBiJoiner;
    }

    @Override
    public BiPredicate<A, B> extractPredicate(BiJoiner<A, B> joiner) {
        return ((FilteringBiJoiner<A, B>)joiner).getFilter();
    }

    @Override
    protected BiPredicate<A, B> mergePredicates(BiPredicate<A, B> predicate1, BiPredicate<A, B> predicate2) {
        return predicate1.and(predicate2);
    }
}
