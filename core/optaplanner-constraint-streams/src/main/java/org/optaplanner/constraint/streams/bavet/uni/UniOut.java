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

package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.optaplanner.constraint.streams.bavet.bi.JoinBiNode;

/**
 * A UniOut is one of the connections from ForEachUniNode to downstream nodes.
 * For example, these 4 constraints ({@code forEach(A).join(B)}, {@code forEach(A).filter()},
 * {@code forEach(C).join(A)}, {@code forEach(B).join(C)})
 * create 3 UniOuts for {@code ForEachUniNode<A>} (one to {@code BiJoin<A,B>}, one to {@code UniScoring<A>} and one to {@code BiJoin<C,A>}),
 * 2 UniOuts for {@code ForEachUniNode<B>} and 2 UniOuts for {@code ForEachUniNode<C>}.
 * @param <A> the type of factA
 */
// TODO Java 17: refactor to record
public final class UniOut<A> {

    public final int outIndex;
    public final Predicate<A> predicate;

    /**
     * Calls for example {@link JoinBiNode#insertA(UniTuple)}, {@link JoinBiNode#insertB(UniTuple)} or ...
     */
    public final Consumer<UniTuple<A>> nextNodeInsert;
    /**
     * Calls for example {@link JoinBiNode#retractA(UniTuple)}, {@link JoinBiNode#retractB(UniTuple)} or ...
     */
    public final Consumer<UniTuple<A>> nextNodeRetract;

    public UniOut(int outIndex, Predicate<A> predicate,
            Consumer<UniTuple<A>> nextNodeInsert, Consumer<UniTuple<A>> nextNodeRetract) {
        this.outIndex = outIndex;
        this.predicate = predicate;
        this.nextNodeInsert = nextNodeInsert;
        this.nextNodeRetract = nextNodeRetract;
    }

}
