package org.optaplanner.constraint.streams.common.quad;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintStream;

public interface InnerQuadConstraintStream<A, B, C, D> extends QuadConstraintStream<A, B, C, D> {

    /**
     * This method will return true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default QuadConstraintStream<A, B, C, D> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy((a, b, c, d) -> a, (a, b, c, d) -> b, (a, b, c, d) -> c, (a, b, c, d) -> d);
        }
    }

    @Override
    default Constraint penalize(String constraintName, Score<?> constraintWeight) {
        return penalize(constraintWeight)
                .as(constraintName);
    }

    @Override
    default Constraint penalize(String constraintPackage, String constraintName, Score<?> constraintWeight) {
        return penalize(constraintWeight)
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint penalizeConfigurable(String constraintName) {
        return penalize()
                .as(constraintName);
    }

    @Override
    default Constraint penalizeConfigurable(String constraintPackage, String constraintName) {
        return penalize()
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint reward(String constraintName, Score<?> constraintWeight) {
        return reward(constraintWeight)
                .as(constraintName);
    }

    @Override
    default Constraint reward(String constraintPackage, String constraintName, Score<?> constraintWeight) {
        return reward(constraintWeight)
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint rewardConfigurable(String constraintName) {
        return reward()
                .as(constraintName);
    }

    @Override
    default Constraint rewardConfigurable(String constraintPackage, String constraintName) {
        return penalize()
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint impact(String constraintName, Score<?> constraintWeight) {
        return impact(constraintWeight)
                .as(constraintName);
    }

    @Override
    default Constraint impact(String constraintPackage, String constraintName, Score<?> constraintWeight) {
        return impact(constraintWeight)
                .as(constraintPackage, constraintName);
    }

}
