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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.constraint.DefaultConstraintMatchTotal;
import org.optaplanner.core.impl.score.constraint.DefaultIndictment;

public abstract class ScoreInliner<Score_ extends Score<Score_>> {

    protected final boolean constraintMatchEnabled;
    private final Score_ zeroScore;
    private final Map<String, DefaultConstraintMatchTotal<Score_>> constraintMatchTotalMap = new TreeMap<>();

    protected ScoreInliner(boolean constraintMatchEnabled, Score_ zeroScore) {
        this.constraintMatchEnabled = constraintMatchEnabled;
        this.zeroScore = zeroScore;
    }

    public abstract Score_ extractScore(int initScore);

    public abstract WeightedScoreImpacter buildWeightedScoreImpacter(Score_ constraintWeight);

    protected Runnable addConstraintMatch(String constraintPackage, String constraintName,
            List<Object> justificationList, Score_ score) {
        String constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
        DefaultConstraintMatchTotal<Score_> constraintMatchTotal = constraintMatchTotalMap.computeIfAbsent(constraintId,
                id -> new DefaultConstraintMatchTotal<>(constraintPackage, constraintName, score));
        ConstraintMatch<Score_> constraintMatch = constraintMatchTotal.addConstraintMatch(justificationList, score);
        return () -> {
          constraintMatchTotal.removeConstraintMatch(constraintMatch);
          if (constraintMatchTotal.getConstraintMatchSet().isEmpty()) { // Clean up.
              constraintMatchTotalMap.remove(constraintId);
          }
        };
    }

    public Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        return Collections.unmodifiableMap(constraintMatchTotalMap);
    }

    public Map<Object, Indictment<Score_>> getIndictmentMap() { // TODO This is temporary, inefficient code, replace it!
        Map<Object, Indictment<Score_>> indictmentMap = new LinkedHashMap<>(); // TODO use entitySize
        for (ConstraintMatchTotal<Score_> constraintMatchTotal : constraintMatchTotalMap.values()) {
            for (ConstraintMatch<Score_> constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
                constraintMatch.getJustificationList().stream()
                        .distinct() // One match might have the same justification twice
                        .forEach(justification -> {
                            DefaultIndictment<Score_> indictment =
                                    (DefaultIndictment<Score_>) indictmentMap.computeIfAbsent(justification,
                                            k -> new DefaultIndictment<>(justification, zeroScore));
                            indictment.addConstraintMatch(constraintMatch);
                        });
            }
        }
        return indictmentMap;
    }

    protected void ensureNonZeroConstraintWeight(Score_ constraintWeight) {
        if (constraintWeight.equals(zeroScore)) {
            throw new IllegalArgumentException("The constraintWeight (" + constraintWeight + ") cannot be zero,"
                    + " this constraint should have been culled during node creation.");
        }
    }

}
