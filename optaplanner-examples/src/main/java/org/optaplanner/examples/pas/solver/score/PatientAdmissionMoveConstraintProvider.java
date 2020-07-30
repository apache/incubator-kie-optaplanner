package org.optaplanner.examples.pas.solver.score;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.examples.pas.domain.BedDesignation;
import org.optaplanner.examples.pas.domain.Gender;
import org.optaplanner.examples.pas.domain.GenderLimitation;

public class PatientAdmissionMoveConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                femaleInMaleRoom(constraintFactory)
        };
    }

    public Constraint femaleInMaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getPatientGender() == Gender.FEMALE
                        && bd.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize("femaleInMaleRoom", HardMediumSoftScore.ofHard(50), BedDesignation::getAdmissionPartNightCount);
    }
}
