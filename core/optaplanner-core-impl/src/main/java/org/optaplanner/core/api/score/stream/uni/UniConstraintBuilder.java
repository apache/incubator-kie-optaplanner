package org.optaplanner.core.api.score.stream.uni;

import java.util.function.Function;

import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;

/**
 * Used to build a {@link Constraint} out of a {@link UniConstraintStream}, applying optional configuration.
 * To build the constraint, use one of the terminal operations, such as {@link #asConstraint(String)}.
 * <p>
 * Unless {@link #justifiedWith(Function)} is called,
 * the default justification function will be used.
 * This function takes the input arguments and converts them into a {@link java.util.List}.
 */
public interface UniConstraintBuilder<A> extends ConstraintBuilder<UniConstraintBuilder<A>> {

    /**
     * Sets a custom function to apply on a constraint match to justify it.
     *
     * @see ConstraintMatch
     * @param justificationFunction never null
     * @return this
     */
    UniConstraintBuilder<A> justifiedWith(Function<A, Object> justificationFunction);

}
