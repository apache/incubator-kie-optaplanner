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

package org.optaplanner.core.impl.score.inliner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.constraint.DefaultConstraintMatchTotal;
import org.optaplanner.core.impl.score.constraint.DefaultIndictment;

public abstract class ScoreInliner<Score_ extends Score<Score_>> {

    protected final boolean constraintMatchEnabled;
    private final Score_ zeroScore;
    private final Map<String, DefaultConstraintMatchTotal<Score_>> constraintMatchTotalMap;
    private final Map<Object, DefaultIndictment<Score_>> indictmentMap;

    protected ScoreInliner(boolean constraintMatchEnabled, Score_ zeroScore) {
        this.constraintMatchEnabled = constraintMatchEnabled;
        this.zeroScore = zeroScore;
        this.constraintMatchTotalMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
        this.indictmentMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
    }

    public abstract Score_ extractScore(int initScore);

    public abstract WeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            Score_ constraintWeight);

    protected Runnable addConstraintMatch(String constraintPackage, String constraintName, Score_ constraintWeight,
            Score_ score, List<Object> justificationList) {
        String constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
        DefaultConstraintMatchTotal<Score_> constraintMatchTotal =
                constraintMatchTotalMap.computeIfAbsent(constraintId,
                        __ -> new DefaultConstraintMatchTotal<>(constraintPackage, constraintName, constraintWeight,
                                zeroScore));
        ConstraintMatch<Score_> constraintMatch =
                constraintMatchTotal.addConstraintMatch(justificationList, score);
        List<DefaultIndictment<Score_>> indictmentList = justificationList.stream()
                .distinct() // One match might have the same justification twice
                .map(justification -> {
                    DefaultIndictment<Score_> indictment = indictmentMap.computeIfAbsent(justification,
                            __ -> new DefaultIndictment<>(justification, zeroScore));
                    indictment.addConstraintMatch(constraintMatch);
                    return indictment;
                }).collect(Collectors.toList());
        return () -> {
            constraintMatchTotal.removeConstraintMatch(constraintMatch);
            if (constraintMatchTotal.getConstraintMatchSet().isEmpty()) {
                constraintMatchTotalMap.remove(constraintId);
            }
            for (DefaultIndictment<Score_> indictment : indictmentList) {
                indictment.removeConstraintMatch(constraintMatch);
                if (indictment.getConstraintMatchSet().isEmpty()) {
                    indictmentMap.remove(indictment.getJustification());
                }
            }
        };
    }

    public Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        // Unchecked assignment necessary as CMT and DefaultCMT incompatible in the Map generics.
        return (Map) constraintMatchTotalMap;
    }

    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        // Unchecked assignment necessary as Indictment and DefaultIndictment incompatible in the Map generics.
        return (Map) indictmentMap;
    }

    protected void assertNonZeroConstraintWeight(Score_ constraintWeight) {
        if (constraintWeight.equals(zeroScore)) {
            throw new IllegalArgumentException("Impossible state: The constraintWeight (" +
                    constraintWeight + ") cannot be zero, constraint should have been culled during node creation.");
        }
    }

    /**
     * Runs constraint matching if enabled.
     *
     * Returns {@code undoWithoutConstraintMatch} if constraint matching is disabled.
     * Otherwise adds a constraint match and extends the undo with an operation to remove the newly added match.
     *
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight never null
     * @param undoWithoutConstraintMatch never null
     * @param scoreSupplier never null
     * @param justificationsSupplier never null
     * @return never null
     */
    protected UndoScoreImpacter buildUndo(String constraintPackage, String constraintName, Score_ constraintWeight,
            UndoScoreImpacter undoWithoutConstraintMatch, Supplier<Score_> scoreSupplier,
            JustificationsSupplier justificationsSupplier) {
        if (!constraintMatchEnabled) {
            return undoWithoutConstraintMatch;
        }
        Runnable undoWithConstraintMatch = addConstraintMatch(constraintPackage, constraintName, constraintWeight,
                scoreSupplier.get(), justificationsSupplier.get());
        return () -> {
            undoWithoutConstraintMatch.run();
            undoWithConstraintMatch.run();
        };
    }

}
