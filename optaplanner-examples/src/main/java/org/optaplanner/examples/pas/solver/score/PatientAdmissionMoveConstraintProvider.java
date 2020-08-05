package org.optaplanner.examples.pas.solver.score;

import static org.optaplanner.core.api.score.stream.Joiners.filtering;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;

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
                femaleInMaleRoomConstraint(constraintFactory),
                maleInFemaleRoomConstraint(constraintFactory),
                sameBedInSameNightConstraint(constraintFactory)
        };
    }

    public Constraint femaleInMaleRoomConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getPatientGender() == Gender.FEMALE
                        && bd.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize("femaleInMaleRoom", HardMediumSoftScore.ofHard(50), BedDesignation::getAdmissionPartNightCount);
    }

    public Constraint maleInFemaleRoomConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getPatientGender() == Gender.MALE
                        && bd.getRoomGenderLimitation() == GenderLimitation.FEMALE_ONLY)
                .penalize("maleInFemaleRoom", HardMediumSoftScore.ofHard(50), BedDesignation::getAdmissionPartNightCount);
    }

    public Constraint sameBedInSameNightConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .join(BedDesignation.class,
                        lessThan(BedDesignation::getId),
                        filtering((leftBd,
                                rightBd) -> leftBd.getAdmissionPart().calculateSameNightCount(rightBd.getAdmissionPart()) > 0),
                        filtering((leftBd,
                                rightBd) -> leftBd.getBed() != null && rightBd.getBed() != null))
                .filter((leftBd, leftBd2) -> leftBd.getBed().getId().equals(leftBd2.getBed().getId()))
                .penalize("sameBedInSameNight", HardMediumSoftScore.ofHard(1000),
                        (leftBd, rightBd) -> leftBd.getAdmissionPart().calculateSameNightCount(rightBd.getAdmissionPart()));
    }
}
