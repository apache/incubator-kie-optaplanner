package org.optaplanner.examples.pas.solver.score;

import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.filtering;
import static org.optaplanner.core.api.score.stream.Joiners.greaterThan;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;

import java.util.function.Function;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.examples.pas.domain.BedDesignation;
import org.optaplanner.examples.pas.domain.Department;
import org.optaplanner.examples.pas.domain.DepartmentSpecialism;
import org.optaplanner.examples.pas.domain.Gender;
import org.optaplanner.examples.pas.domain.GenderLimitation;
import org.optaplanner.examples.pas.domain.PreferredPatientEquipment;
import org.optaplanner.examples.pas.domain.RequiredPatientEquipment;
import org.optaplanner.examples.pas.domain.RoomEquipment;
import org.optaplanner.examples.pas.domain.RoomSpecialism;

/*
 * This is constraints for Hospital Bed Planning
 * They are based on patientAdmissionScheduleConstraints.drl
 * Planning Entity: BedDesignation
 * Planning Variable: Bed(nullable) - would not be prefiltered on uninitialized solutions
 * Bed is nullable so in case you need to access it members check that planning value bed is not null
 */

public class PatientAdmissionMoveConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                sameBedInSameNightConstraint(constraintFactory),
                femaleInMaleRoomConstraint(constraintFactory),
                maleInFemaleRoomConstraint(constraintFactory),
                differentGenderInSameGenderRoomInSameNightConstraint(constraintFactory),
                departmentMinimumAgeConstraint(constraintFactory),
                departmentMaximumAgeConstraint(constraintFactory),
                requiredPatientEquipmentConstraint(constraintFactory),
                assignEveryPatientToABedConstraint(constraintFactory),
                preferredMaximumRoomCapacityConstraint(constraintFactory),
                departmentSpecialismConstraint(constraintFactory),
                roomSpecialismNotExistsConstraint(constraintFactory),
                roomSpecialismNotFirstPriorityConstraint(constraintFactory),
                preferredPatientEquipmentConstraint(constraintFactory)
        };
    }

    public Constraint sameBedInSameNightConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(BedDesignation.class)
                .filter((left, right) -> left.getBed() != null
                        && right.getBed() != null
                        && left.getBed() == right.getBed()
                        && left.getAdmissionPart().calculateSameNightCount(right.getAdmissionPart()) > 0)
                .penalize("sameBedInSameNight", HardMediumSoftScore.ofHard(1000),
                        (leftBd, rightBd) -> leftBd.getAdmissionPart().calculateSameNightCount(rightBd.getAdmissionPart()));
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

    public Constraint differentGenderInSameGenderRoomInSameNightConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER
                        && bd.getBed() != null)
                .join(BedDesignation.class,
                        equal(BedDesignation::getRoom),
                        lessThan(BedDesignation::getId),
                        filtering((left, right) -> right.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER
                                && left.getAdmissionPart().calculateSameNightCount(right.getAdmissionPart()) > 0
                                && left.getPatient().getGender() != right.getPatient().getGender()))
                .penalize("differentGenderInSameGenderRoomInSameNight", HardMediumSoftScore.ofHard(1000),
                        (left, right) -> left.getAdmissionPart().calculateSameNightCount(right.getAdmissionPart()));
    }

    public Constraint departmentMinimumAgeConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Department.class)
                .filter(d -> d.getMinimumAge() != null)
                .join(BedDesignation.class,
                        equal(Function.identity(), BedDesignation::getDepartment),
                        greaterThan(Department::getMinimumAge, BedDesignation::getPatientAge))
                .penalize("departmentMinimumAge", HardMediumSoftScore.ofHard(100),
                        (d, bd) -> bd.getAdmissionPartNightCount());
    }

    public Constraint departmentMaximumAgeConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Department.class)
                .filter(d -> d.getMaximumAge() != null)
                .join(BedDesignation.class,
                        equal(Function.identity(), BedDesignation::getDepartment),
                        lessThan(Department::getMaximumAge, BedDesignation::getPatientAge))
                .penalize("departmentMaximumAge", HardMediumSoftScore.ofHard(100),
                        (d, bd) -> bd.getAdmissionPartNightCount());
    }

    public Constraint requiredPatientEquipmentConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(RequiredPatientEquipment.class)
                .join(BedDesignation.class,
                        equal(RequiredPatientEquipment::getPatient, BedDesignation::getPatient),
                        filtering((rpe, bd) -> bd.getBed() != null))
                .ifNotExists(RoomEquipment.class,
                        equal((rpe, bd) -> bd.getRoom(), RoomEquipment::getRoom),
                        equal((rpe, bd) -> rpe.getEquipment(), RoomEquipment::getEquipment))
                .penalize("requiredPatientEquipment", HardMediumSoftScore.ofHard(50),
                        (rpe, bd) -> bd.getAdmissionPartNightCount());
    }

    //Medium
    public Constraint assignEveryPatientToABedConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUnfiltered(BedDesignation.class)
                .filter(bd -> bd.getBed() == null)
                .penalize("assignEveryPatientToABed", HardMediumSoftScore.ONE_MEDIUM,
                        BedDesignation::getAdmissionPartNightCount);
    }

    //Soft
    public Constraint preferredMaximumRoomCapacityConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getBed() != null
                        && bd.getPatient().getPreferredMaximumRoomCapacity() != null
                        && bd.getPatient().getPreferredMaximumRoomCapacity() < bd.getRoom().getCapacity())
                .penalize("preferredMaximumRoomCapacity", HardMediumSoftScore.ofSoft(8),
                        BedDesignation::getAdmissionPartNightCount);
    }

    public Constraint departmentSpecialismConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getBed() != null)
                .ifNotExists(DepartmentSpecialism.class,
                        equal(BedDesignation::getDepartment, DepartmentSpecialism::getDepartment),
                        equal(BedDesignation::getAdmissionPartSpecialism, DepartmentSpecialism::getSpecialism))
                .penalize("departmentSpecialism", HardMediumSoftScore.ofSoft(10),
                        BedDesignation::getAdmissionPartNightCount);
    }

    public Constraint roomSpecialismNotExistsConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getBed() != null && bd.getAdmissionPartSpecialism() != null)
                .ifNotExists(RoomSpecialism.class,
                        equal(BedDesignation::getRoom, RoomSpecialism::getRoom),
                        equal(BedDesignation::getAdmissionPartSpecialism, RoomSpecialism::getSpecialism))
                .penalize("roomSpecialismNotExists", HardMediumSoftScore.ofSoft(20),
                        BedDesignation::getAdmissionPartNightCount);
    }

    public Constraint roomSpecialismNotFirstPriorityConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getBed() != null && bd.getAdmissionPartSpecialism() != null)
                .join(RoomSpecialism.class,
                        equal(BedDesignation::getRoom, RoomSpecialism::getRoom),
                        equal(BedDesignation::getAdmissionPartSpecialism, RoomSpecialism::getSpecialism),
                        filtering((bd, rs) -> rs.getPriority() > 1))
                .penalize("roomSpecialismNotFirstPriority", HardMediumSoftScore.ofSoft(10),
                        (bd, rs) -> (rs.getPriority() - 1) * bd.getAdmissionPartNightCount());
    }

    public Constraint preferredPatientEquipmentConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(PreferredPatientEquipment.class)
                .join(BedDesignation.class,
                        equal(PreferredPatientEquipment::getPatient, BedDesignation::getPatient),
                        filtering((ppe, bd) -> bd.getBed() != null))
                .ifNotExists(RoomEquipment.class,
                        equal((re, bd) -> bd.getRoom(), RoomEquipment::getRoom),
                        equal((re, bd) -> re.getEquipment(), RoomEquipment::getEquipment))
                .penalize("preferredPatientEquipment", HardMediumSoftScore.ofSoft(20),
                        (re, bd) -> bd.getAdmissionPartNightCount());
    }
}
