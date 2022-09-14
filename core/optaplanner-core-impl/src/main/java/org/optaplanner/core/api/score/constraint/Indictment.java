package org.optaplanner.core.api.score.constraint;

import java.util.Set;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;

/**
 * Explains the {@link Score} of a {@link PlanningSolution}, from the opposite side than {@link ConstraintMatchTotal}.
 * Retrievable from {@link ScoreExplanation#getIndictmentMap()}.
 * 
 * @param <Score_> the actual score type
 */
public interface Indictment<Score_ extends Score<Score_>> {

    /**
     * As defined by {@link #getIndictedObject()}.
     *
     * @deprecated Prefer {@link #getIndictedObject()}.
     * @return never null
     */
    @Deprecated(forRemoval = true)
    default Object getJustification() {
        return getIndictedObject();
    }

    /**
     * The object that was involved in causing the constraints to match.
     * It is part of {@link ConstraintMatch#getIndictedObjectList()} of every {@link ConstraintMatch}
     * returned by {@link #getConstraintMatchSet()}.
     *
     * @return never null
     * @param <IndictedObject_> Shorthand so that the user does not need to cast in user code.
     */
    <IndictedObject_> IndictedObject_ getIndictedObject();

    /**
     * @return never null
     */
    Set<ConstraintMatch<Score_>> getConstraintMatchSet();

    /**
     * @return {@code >= 0}
     */
    default int getConstraintMatchCount() {
        return getConstraintMatchSet().size();
    }

    /**
     * Sum of the {@link #getConstraintMatchSet()}'s {@link ConstraintMatch#getScore()}.
     *
     * @return never null
     */
    Score_ getScore();

}
