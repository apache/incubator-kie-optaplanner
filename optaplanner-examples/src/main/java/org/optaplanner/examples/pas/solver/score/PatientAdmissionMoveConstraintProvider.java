package org.optaplanner.examples.pas.solver.score;

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

import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.filtering;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;

public class PatientAdmissionMoveConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                femaleInMaleRoomConstraint(constraintFactory),
                maleInFemaleRoomConstraint(constraintFactory),
                sameBedInSameNightConstraint(constraintFactory),
                departmentMinimumAgeConstraint(constraintFactory),
                departmentMaximumAgeConstraint(constraintFactory),
                requiredPatientEquipmentConstraint(constraintFactory),
                differentGenderInSameGenderRoomInSameNightConstraint(constraintFactory),
                assignEveryPatientToABedConstraint(constraintFactory),
                preferredMaximumRoomCapacityConstraint(constraintFactory),
                departmentSpecialismConstraint(constraintFactory),
                roomSpecialismNotExistsConstraint(constraintFactory),
                roomSpecialismNotFirstPriorityConstraint(constraintFactory),
                preferredPatientEquipmentConstraint(constraintFactory)
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

    //from uniqye pair
    public Constraint sameBedInSameNightConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .join(BedDesignation.class,
                        //get unique designation by id
                        lessThan(BedDesignation::getId),
                        //get for the same period
                        filtering((leftBd,
                                   rightBd) -> leftBd.getAdmissionPart().calculateSameNightCount(rightBd.getAdmissionPart()) > 0),
                        //get only assigned beds
                        filtering((leftBd,
                                   rightBd) -> leftBd.getBed() != null && rightBd.getBed() != null))
                .filter((leftBd, leftBd2) -> leftBd.getBed().getId().equals(leftBd2.getBed().getId()))
                .penalize("sameBedInSameNight", HardMediumSoftScore.ofHard(1000),
                        (leftBd, rightBd) -> leftBd.getAdmissionPart().calculateSameNightCount(rightBd.getAdmissionPart()));
    }

    public Constraint differentGenderInSameGenderRoomInSameNightConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER
                        && bd.getPatient() != null && bd.getBed() != null)
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
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getDepartment() != null && bd.getPatient() != null)
                .ifExists(Department.class,
                        equal(bd -> bd.getDepartment().getId(), Department::getId),
                        filtering((left, right) -> right.getMinimumAge() != null
                                && left.getPatientAge() < right.getMinimumAge()))
                .penalize("departmentMinimumAge", HardMediumSoftScore.ofHard(100),
                        BedDesignation::getAdmissionPartNightCount);
    }

    public Constraint departmentMaximumAgeConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getDepartment() != null && bd.getPatient() != null)
                .ifExists(Department.class,
                        equal(bd -> bd.getDepartment().getId(), Department::getId),
                        filtering((left, right) -> right.getMaximumAge() != null
                                && left.getPatientAge() > right.getMaximumAge()))
                .penalize("departmentMaximumAge", HardMediumSoftScore.ofHard(100),
                        BedDesignation::getAdmissionPartNightCount);
    }

    public Constraint requiredPatientEquipmentConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getPatient() != null && bd.getBed() != null)
                .join(RequiredPatientEquipment.class,
                        equal(BedDesignation::getPatient, RequiredPatientEquipment::getPatient))
                .ifNotExists(RoomEquipment.class,
                        equal((bd, re) -> bd.getRoom(), RoomEquipment::getRoom),
                        equal((bd, re) -> re.getEquipment(), RoomEquipment::getEquipment))
                .penalize("requiredPatientEquipment", HardMediumSoftScore.ofHard(50),
                        (bd, re) -> bd.getAdmissionPartNightCount());
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
                .filter(bd -> bd.getBed() != null //bed is nullable so fromUnfiltered will not work
                        && bd.getPatient() != null //?TODO check
                        && bd.getPatient().getPreferredMaximumRoomCapacity() != null
                        && bd.getPatient().getPreferredMaximumRoomCapacity() < bd.getRoom().getCapacity())
                .penalize("assignEveryPatientToABed", HardMediumSoftScore.ofSoft(8),
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
        return constraintFactory.from(BedDesignation.class)
                .filter(bd -> bd.getPatient() != null && bd.getBed() != null)//if bed then room
                .join(PreferredPatientEquipment.class,
                        equal(BedDesignation::getPatient, PreferredPatientEquipment::getPatient))


                .ifNotExists(RoomEquipment.class,
                        equal((bd, re) -> bd.getRoom(), RoomEquipment::getRoom),
                        equal((bd, re) -> re.getEquipment(), RoomEquipment::getEquipment))
                .penalize("preferredPatientEquipment", HardMediumSoftScore.ofSoft(20),
                        (bd, re) -> bd.getAdmissionPartNightCount());
    }
}
