package org.optaplanner.constraint.streams.common.bi;

import static org.optaplanner.constraint.streams.common.RetrievalSemantics.STANDARD;

import org.optaplanner.constraint.streams.common.RetrievalSemantics;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;

public interface InnerBiConstraintStream<A, B> extends BiConstraintStream<A, B> {

    RetrievalSemantics getRetrievalSemantics();

    /**
     * This method will return true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default <C> TriConstraintStream<A, B, C> join(Class<C> otherClass, TriJoiner<A, B, C>... joiners) {
        if (getRetrievalSemantics() == STANDARD) {
            return join(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            return join(getConstraintFactory().from(otherClass), joiners);
        }
    }

    @Override
    default BiConstraintStream<A, B> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy((a, b) -> a, (a, b) -> b);
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
