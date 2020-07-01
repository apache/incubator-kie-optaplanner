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

package org.optaplanner.core.impl.score.stream.bavet.bi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.score.constraint.DefaultConstraintMatchTotal;
import org.optaplanner.core.impl.score.inliner.UndoScoreImpacter;
import org.optaplanner.core.impl.score.stream.bavet.BavetConstraintSession;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetScoringNode;

public final class BavetScoringBiNode<A, B> extends BavetAbstractBiNode<A, B> implements BavetScoringNode {

    private final String constraintPackage;
    private final String constraintName;
    private final Score<?> constraintWeight;
    private final TriFunction<A, B, Consumer<Score<?>>, UndoScoreImpacter> scoreImpacter;

    private final boolean constraintMatchEnabled;
    private final Set<BavetScoringBiTuple<A, B>> tupleSet;

    public BavetScoringBiNode(BavetConstraintSession session, int nodeOrder,
            String constraintPackage, String constraintName, Score<?> constraintWeight,
            TriFunction<A, B, Consumer<Score<?>>, UndoScoreImpacter> scoreImpacter) {
        super(session, nodeOrder);
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        this.constraintWeight = constraintWeight;
        this.scoreImpacter = scoreImpacter;
        this.constraintMatchEnabled = session.isConstraintMatchEnabled();
        tupleSet = constraintMatchEnabled ? new HashSet<>() : null;
    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    // No node sharing

    // ************************************************************************
    // Runtime
    // ************************************************************************

    @Override
    public BavetScoringBiTuple<A, B> createTuple(BavetAbstractBiTuple<A, B> parentTuple) {
        return new BavetScoringBiTuple<>(this, parentTuple);
    }

    public void refresh(BavetScoringBiTuple<A, B> tuple) {
        A a = tuple.getFactA();
        B b = tuple.getFactB();
        UndoScoreImpacter oldUndoScoreImpacter = tuple.getUndoScoreImpacter();
        if (oldUndoScoreImpacter != null) {
            oldUndoScoreImpacter.undoScoreImpact();
            if (constraintMatchEnabled) {
                tuple.setMatchScore(null);
                boolean removed = tupleSet.remove(tuple);
                if (!removed) {
                    throw new IllegalStateException("Impossible state: The node with constraintId ("
                            + getConstraintId() + ") could not remove the tuple (" + tuple + ") from the tupleSet.");
                }
            }
        }
        if (tuple.isActive()) {
            UndoScoreImpacter undoScoreImpacter = scoreImpacter.apply(a, b, tuple::setMatchScore);
            tuple.setUndoScoreImpacter(undoScoreImpacter);
            if (constraintMatchEnabled) {
                boolean added = tupleSet.add(tuple);
                if (!added) {
                    throw new IllegalStateException("Impossible state: The node with constraintId ("
                            + getConstraintId() + ") could not add the tuple (" + tuple + ") to the tupleSet.");
                }
            }
        } else {
            tuple.setUndoScoreImpacter(null);
        }
        tuple.refreshed();
    }

    @Override
    public ConstraintMatchTotal buildConstraintMatchTotal(Score<?> zeroScore) {
        DefaultConstraintMatchTotal constraintMatchTotal = new DefaultConstraintMatchTotal(constraintPackage,
                constraintName, constraintWeight, zeroScore);
        for (BavetScoringBiTuple<A, B> tuple : tupleSet) {
            constraintMatchTotal.addConstraintMatch(
                    Arrays.asList(tuple.getFactA(), tuple.getFactB()), tuple.getMatchScore());
        }
        return constraintMatchTotal;
    }

    @Override
    public String toString() {
        return "Scoring(" + constraintWeight + ")";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public String getConstraintPackage() {
        return constraintPackage;
    }

    @Override
    public String getConstraintName() {
        return constraintName;
    }

    @Override
    public String getConstraintId() {
        return ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
    }

    @Override
    public Score<?> getConstraintWeight() {
        return constraintWeight;
    }

}
