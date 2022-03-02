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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.common.inliner.UndoScoreImpacter;
import org.optaplanner.core.api.score.Score;

public final class ScoringUniNode<A> extends AbstractNode {

    private final Score<?> constraintWeight;
    private final Function<A, UndoScoreImpacter> scoreImpacter;

    private final Map<UniTuple<A>, UndoScoreImpacter> impacterMap = new HashMap<>();

    public ScoringUniNode(int nodeIndex,
            Score<?> constraintWeight, Function<A, UndoScoreImpacter> scoreImpacter) {
        super(nodeIndex);
        this.constraintWeight = constraintWeight;
        this.scoreImpacter = scoreImpacter;
    }

    public void insert(UniTuple<A> tupleA) {
        UndoScoreImpacter undoScoreImpacter = scoreImpacter.apply(tupleA.factA);
        UndoScoreImpacter old = impacterMap.put(tupleA, undoScoreImpacter);
        if (old != null) {
            throw new IllegalStateException("Impossible state: the tuple for the fact (" + tupleA.factA
                    + ") was already added in the impacterMap.");
        }
    }

    public void retract(UniTuple<A> tupleA) {
        UndoScoreImpacter undoScoreImpacter = impacterMap.remove(tupleA);
        if (undoScoreImpacter == null) {
            throw new IllegalStateException("Impossible state: the tuple for the fact (" + tupleA.factA
                    + ") doesn't exist in the impacterMap.");
        }
        undoScoreImpacter.run();
    }

    @Override
    public String toString() {
        return "Scoring(" + constraintWeight + ")";
    }

}
