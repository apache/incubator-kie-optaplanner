package org.optaplanner.core.api.score.stream.quad;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;

/**
 * Used to build a {@link Constraint} out of a {@link QuadConstraintStream}, applying optional configuration.
 * To build the constraint, use one of the terminal operations, such as {@link #asConstraint(String)}.
 * <p>
 * Unless {@link #justifiedWith(QuadFunction)} is called,
 * the default justification function will be used.
 * This function takes the input arguments and converts them into a {@link java.util.List}.
 */
public interface QuadConstraintBuilder<A, B, C, D> extends ConstraintBuilder<QuadConstraintBuilder<A, B, C, D>> {

    /**
     * Sets a custom function to apply on a constraint match to justify it.
     *
     * @see ConstraintMatch
     * @param justificationFunction never null
     * @return this
     */
    QuadConstraintBuilder<A, B, C, D> justifiedWith(QuadFunction<A, B, C, D, Object> justificationFunction);

}
