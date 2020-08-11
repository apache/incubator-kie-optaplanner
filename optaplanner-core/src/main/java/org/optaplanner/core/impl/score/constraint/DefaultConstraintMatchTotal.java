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

package org.optaplanner.core.impl.score.constraint;

import static java.util.Comparator.comparing;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;

public final class DefaultConstraintMatchTotal implements ConstraintMatchTotal,
        Comparable<DefaultConstraintMatchTotal> {

    private static final Comparator<DefaultConstraintMatchTotal> COMPARATOR =
            comparing(DefaultConstraintMatchTotal::getConstraintPackage)
                    .thenComparing(DefaultConstraintMatchTotal::getConstraintName);

    private final String constraintPackage;
    private final String constraintName;
    private final Score constraintWeight;

    private final Map<ConstraintMatch, Integer> constraintMatchRuleFireCountMap = new LinkedHashMap<>();
    private Score score;

    public DefaultConstraintMatchTotal(String constraintPackage, String constraintName, Score zeroScore) {
        this(constraintPackage, constraintName, null, zeroScore);
    }

    public DefaultConstraintMatchTotal(String constraintPackage, String constraintName, Score constraintWeight,
            Score zeroScore) {
        this.constraintPackage = requireNonNull(constraintPackage);
        this.constraintName = requireNonNull(constraintName);
        this.constraintWeight = constraintWeight;
        this.score = requireNonNull(zeroScore);
    }

    @Override
    public String getConstraintPackage() {
        return constraintPackage;
    }

    @Override
    public String getConstraintName() {
        return constraintName;
    }

    @Override
    public Score getConstraintWeight() {
        return constraintWeight;
    }

    @Override
    public Set<ConstraintMatch> getConstraintMatchSet() {
        return constraintMatchRuleFireCountMap.keySet();
    }

    @Override
    public Score getScore() {
        return score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public ConstraintMatch addConstraintMatch(List<Object> justificationList, Score score) {
        ConstraintMatch constraintMatch = new ConstraintMatch(constraintPackage, constraintName, justificationList,
                score);
        int currentFireCount = constraintMatchRuleFireCountMap.compute(constraintMatch,
                (key, fireCount) -> (fireCount == null) ? 1 : fireCount + 1);
        if (currentFireCount == 1) {
            /*
             * Sometimes the same constraint match may be sent more than once, such as when the DRL uses logical OR on
             * two facts of the same type which turn out to resolve to the same fact.
             * This rule will fire twice, sending two distinct constraint match instances with identical contents.
             * See PLANNER-1433 for details.
             */
            this.score = this.score.add(score);
        }
        return constraintMatch;
    }

    public void removeConstraintMatch(ConstraintMatch constraintMatch) {
        Integer currentFireCount = constraintMatchRuleFireCountMap.compute(constraintMatch, (key, fireCount) -> {
            if (fireCount == null || fireCount < 1) {
                throw new IllegalStateException("The constraintMatchTotal (" + this
                        + ") could not remove constraintMatch (" + constraintMatch
                        + ") from its map (" + constraintMatchRuleFireCountMap + ").");
            } else if (fireCount == 1) { // Unmap as all the constraint matches with the same justifications are gone.
                return null;
            } else {
                return fireCount - 1;
            }
        });
        if (currentFireCount == null) { // Constraint match was just removed.
            score = score.subtract(constraintMatch.getScore());
        }
    }

    // ************************************************************************
    // Infrastructure methods
    // ************************************************************************

    @Override
    public String getConstraintId() {
        return ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
    }

    @Override
    public int compareTo(DefaultConstraintMatchTotal other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof DefaultConstraintMatchTotal) {
            DefaultConstraintMatchTotal other = (DefaultConstraintMatchTotal) o;
            return constraintPackage.equals(other.constraintPackage)
                    && constraintName.equals(other.constraintName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash(constraintPackage, constraintName);
    }

    @Override
    public String toString() {
        return getConstraintId() + "=" + score;
    }

}
