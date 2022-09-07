package org.optaplanner.core.api.score.stream.bi;

import java.util.function.BiFunction;

import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;

/**
 * Used to build a {@link Constraint} out of a {@link BiConstraintStream}, applying optional configuration.
 * To build the constraint, use one of the terminal operations, such as {@link #asConstraint(String)}.
 * <p>
 * Unless {@link #justifiedWith(BiFunction)} is called,
 * the default justification function will be used.
 * This function takes the input arguments and converts them into a {@link java.util.List}.
 */
public interface BiConstraintBuilder<A, B> extends ConstraintBuilder<BiConstraintBuilder<A, B>> {

    /**
     * Sets a custom function to apply on a constraint match to justify it.
     *
     * @see ConstraintMatch
     * @param justificationFunction never null
     * @return this
     */
    BiConstraintBuilder<A, B> justifiedWith(BiFunction<A, B, Object> justificationFunction);

}
