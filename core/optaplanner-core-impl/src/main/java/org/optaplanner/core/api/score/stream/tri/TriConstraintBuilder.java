package org.optaplanner.core.api.score.stream.tri;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;

/**
 * Used to build a {@link Constraint} out of a {@link TriConstraintStream}, applying optional configuration.
 * To build the constraint, use one of the terminal operations, such as {@link #asConstraint(String)}.
 * <p>
 * Unless {@link #justifiedWith(TriFunction)} is called,
 * the default justification function will be used.
 * This function takes the input arguments and converts them into a {@link java.util.List}.
 */
public interface TriConstraintBuilder<A, B, C> extends ConstraintBuilder<TriConstraintBuilder<A, B, C>> {

    /**
     * Sets a custom function to apply on a constraint match to justify it.
     * That function must not return a {@link java.util.Collection},
     * else {@link IllegalStateException} will be thrown during score calculation.
     *
     * @see ConstraintMatch
     * @param justificationFunction never null
     * @return this
     */
    TriConstraintBuilder<A, B, C> justifiedWith(TriFunction<A, B, C, Object> justificationFunction);

}
