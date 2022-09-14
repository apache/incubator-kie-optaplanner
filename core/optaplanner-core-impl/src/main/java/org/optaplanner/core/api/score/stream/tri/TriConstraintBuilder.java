package org.optaplanner.core.api.score.stream.tri;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintBuilder;
import org.optaplanner.core.api.score.stream.ConstraintJustification;

/**
 * Used to build a {@link Constraint} out of a {@link TriConstraintStream}, applying optional configuration.
 * To build the constraint, use one of the terminal operations, such as {@link #asConstraint(String)}.
 * <p>
 * Unless {@link #justifyWith(TriFunction)} is called, the default justification mapping will be used.
 */
public interface TriConstraintBuilder<A, B, C> extends ConstraintBuilder<TriConstraintBuilder<A, B, C>> {

    /**
     * Sets a custom function to apply on a constraint match to justify it.
     *
     * @see ConstraintMatch
     * @param justificationMapping never null
     * @return this
     */
    <ConstraintJustification_ extends ConstraintJustification> TriConstraintBuilder<A, B, C> justifyWith(
            TriFunction<A, B, C, ConstraintJustification_> justificationMapping);

}
