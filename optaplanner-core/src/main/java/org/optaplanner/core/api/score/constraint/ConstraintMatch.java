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

package org.optaplanner.core.api.score.constraint;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;

import org.optaplanner.core.api.score.Score;

/**
 * Retrievable from {@link ConstraintMatchTotal#getConstraintMatchSet()}
 * and {@link Indictment#getConstraintMatchSet()}.
 *
 * @param <Score_> the actual score type
 */
public final class ConstraintMatch<Score_ extends Score<Score_>> implements Comparable<ConstraintMatch<Score_>> {

    private static final Comparator<Object> CONSTRAINT_JUSTIFICATION_COMPARATOR = (left, right) -> {
        if (left == null && right != null) {
            return -1; // Nulls first.
        } else if (left != null && right == null) {
            return 1; // Nulls first.
        } else if (left == right) {
            return 0;
        }
        // First distinguish objects by their type.
        int comparison = left.getClass().getName().compareTo(right.getClass().getName());
        if (comparison != 0) {
            return comparison;
        }
        if (left.getClass() == right.getClass() && Comparable.class.isAssignableFrom(left.getClass())) {
            // If both are of the same type, and that type is comparable, use it.
            // Objects can maintain consistent ordering if they implement Comparable.
            comparison = ((Comparable) left).compareTo(right);
            if (comparison != 0) {
                return comparison;
            }
        }
        // If still equal, compare by identity, bringing consistent ordering to everything else.
        return Integer.compare(System.identityHashCode(left), System.identityHashCode(right));
    };

    private final String constraintPackage;
    private final String constraintName;

    private final List<Object> justificationList;
    private final Score_ score;

    /**
     * @param constraintPackage never null
     * @param constraintName never null
     * @param justificationList never null, sometimes empty
     * @param score never null
     */
    public ConstraintMatch(String constraintPackage, String constraintName, List<Object> justificationList,
            Score_ score) {
        this.constraintPackage = requireNonNull(constraintPackage);
        this.constraintName = requireNonNull(constraintName);
        this.justificationList = requireNonNull(justificationList);
        this.score = requireNonNull(score);
    }

    public String getConstraintPackage() {
        return constraintPackage;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public List<Object> getJustificationList() {
        return justificationList;
    }

    public Score_ getScore() {
        return score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public String getConstraintId() {
        return ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
    }

    public String getIdentificationString() {
        return getConstraintId() + "/" + justificationList;
    }

    /**
     * As defined by {@link Comparable#compareTo(Object)}.
     *
     * <p>
     * The point of this method is to provide consistency when a collection of matches is visualized.
     * Instances of this class should never be compared to one another for any other reason.
     */
    @Override
    public int compareTo(ConstraintMatch<Score_> other) {
        if (this == other) {
            return 0; // Constraint matches only compare to equal if they are the same instance.
        }
        if (!constraintPackage.equals(other.constraintPackage)) {
            return constraintPackage.compareTo(other.constraintPackage);
        } else if (!constraintName.equals(other.constraintName)) {
            return constraintName.compareTo(other.constraintName);
        } else if (!score.equals(other.score)) {
            return -score.compareTo(other.score); // Heavier matches first.
        } else if (justificationList.size() != other.justificationList.size()) {
            // Matches with less justifications first.
            return justificationList.size() < other.justificationList.size() ? -1 : 1;
        } else {
            for (int i = 0; i < justificationList.size(); i++) {
                Object left = justificationList.get(i);
                Object right = other.justificationList.get(i);
                int comparison = CONSTRAINT_JUSTIFICATION_COMPARATOR.compare(left, right);
                if (comparison != 0) {
                    return comparison;
                }
            }
            // Constraint matches only equal when they are the same, this makes compareTo() consistent with equals.
            return Integer.compare(System.identityHashCode(this), System.identityHashCode(other));
        }
    }

    @Override
    public String toString() {
        return getIdentificationString() + "=" + score;
    }

}
