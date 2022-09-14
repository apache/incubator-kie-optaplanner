package org.optaplanner.core.api.score.stream.uni;

import java.util.function.Function;

import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;
import org.optaplanner.core.api.score.stream.ConstraintJustification;

/**
 * Used to build a {@link Constraint} out of a {@link UniConstraintStream}, applying optional configuration.
 * To build the constraint, use one of the terminal operations, such as {@link #asConstraint(String)}.
 * <p>
 * Unless {@link #justifyWith(Function)} is called, the default justification mapping will be used.
 */
public interface UniConstraintBuilder<A> extends ConstraintBuilder<UniConstraintBuilder<A>> {

    /**
     * Sets a custom function to apply on a constraint match to justify it.
     * That function must not return a {@link java.util.Collection},
     * else {@link IllegalStateException} will be thrown during score calculation.
     *
     * @see ConstraintMatch
     * @param justificationMapping never null
     * @return this
     */
    <ConstraintJustification_ extends ConstraintJustification> UniConstraintBuilder<A> justifyWith(
            Function<A, ConstraintJustification_> justificationMapping);

}
