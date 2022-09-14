package org.optaplanner.core.impl.score.constraint;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;

public final class DefaultIndictment<Score_ extends Score<Score_>> implements Indictment<Score_> {

    public static <A> Collection<?> getDefaultIndictmentMapping(A factA) {
        return List.of(factA);
    }

    public static <A, B> Collection<?> getDefaultIndictmentMapping(A factA, B factB) {
        return List.of(factA, factB);
    }

    public static <A, B, C> Collection<?> getDefaultIndictmentMapping(A factA, B factB, C factC) {
        return List.of(factA, factB, factC);
    }

    public static <A, B, C, D> Collection<?> getDefaultIndictmentMapping(A factA, B factB, C factC, D factD) {
        return List.of(factA, factB, factC, factD);
    }

    private final Object justification;

    private final Set<ConstraintMatch<Score_>> constraintMatchSet;
    private Score_ score;

    public DefaultIndictment(Object justification, Score_ zeroScore) {
        this.justification = justification;
        constraintMatchSet = new LinkedHashSet<>();
        score = zeroScore;
    }

    @Override
    public Object getJustification() {
        return justification;
    }

    @Override
    public Set<ConstraintMatch<Score_>> getConstraintMatchSet() {
        return constraintMatchSet;
    }

    @Override
    public Score_ getScore() {
        return score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void addConstraintMatch(ConstraintMatch<Score_> constraintMatch) {
        score = score.add(constraintMatch.getScore());
        boolean added = constraintMatchSet.add(constraintMatch);
        if (!added) {
            throw new IllegalStateException("The indictment (" + this
                    + ") could not add constraintMatch (" + constraintMatch
                    + ") to its constraintMatchSet (" + constraintMatchSet + ").");
        }
    }

    public void removeConstraintMatch(ConstraintMatch<Score_> constraintMatch) {
        score = score.subtract(constraintMatch.getScore());
        boolean removed = constraintMatchSet.remove(constraintMatch);
        if (!removed) {
            throw new IllegalStateException("The indictment (" + this
                    + ") could not remove constraintMatch (" + constraintMatch
                    + ") from its constraintMatchSet (" + constraintMatchSet + ").");
        }
    }

    // ************************************************************************
    // Infrastructure methods
    // ************************************************************************

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof DefaultIndictment) {
            DefaultIndictment<Score_> other = (DefaultIndictment<Score_>) o;
            return justification.equals(other.justification);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return justification.hashCode();
    }

    @Override
    public String toString() {
        return justification + "=" + score;
    }

}
