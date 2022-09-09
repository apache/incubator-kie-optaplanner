package org.optaplanner.test.api.score.stream.testdata;

import java.util.Objects;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.planningid.TestdataStringPlanningIdEntity;

public class TestdataConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                penalizeEveryEntity(constraintFactory),
                rewardEveryEntity(constraintFactory),
                impactEveryEntity(constraintFactory),
                differentStringEntityHaveDifferentValues(constraintFactory),
        };
    }

    public Constraint penalizeEveryEntity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TestdataEntity.class)
                .penalize(SimpleScore.ONE)
                .asConstraint("Penalize every entity");
    }

    public Constraint rewardEveryEntity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TestdataEntity.class)
                .reward(SimpleScore.ONE)
                .asConstraint("Reward every entity");
    }

    public Constraint impactEveryEntity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TestdataEntity.class)
                .impact(SimpleScore.ONE,
                        entity -> Objects.equals(entity.getCode(), "A") ? 1 : -1)
                .asConstraint("Impact every entity");
    }

    public Constraint differentStringEntityHaveDifferentValues(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(TestdataStringPlanningIdEntity.class,
                Joiners.equal(TestdataStringPlanningIdEntity::getValue))
                .penalize(SimpleScore.ONE)
                .asConstraint("Different String Entity Have Different Values");
    }

}
