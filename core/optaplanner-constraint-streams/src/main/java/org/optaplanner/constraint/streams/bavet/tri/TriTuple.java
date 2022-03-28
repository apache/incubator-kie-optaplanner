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

package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;
import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.constraint.streams.common.inliner.UndoScoreImpacter;

public final class TriTuple<A, B, C> implements Tuple {

    public final A factA;
    public final B factB;
    public final C factC;

    public final Object[][] joinStore;
    public final Object[] groupStore;
    public final UndoScoreImpacter[] scorerStore;

    public BavetTupleState state;

    public TriTuple(A factA, B factB, C factC, int joinStoreSize, int groupStoreSize, int scoreStoreSize) {
        this.factA = factA;
        this.factB = factB;
        this.factC = factC;
        joinStore = (joinStoreSize <= 0) ? null : new Object[joinStoreSize][];
        groupStore = (groupStoreSize <= 0) ? null : new Object[groupStoreSize];
        scorerStore = (scoreStoreSize <= 0) ? null : new UndoScoreImpacter[scoreStoreSize];
    }

    @Override
    public String toString() {
        return "TriTuple(" + factA + ", " + factB + ", " + factC + ")";
    }

}
