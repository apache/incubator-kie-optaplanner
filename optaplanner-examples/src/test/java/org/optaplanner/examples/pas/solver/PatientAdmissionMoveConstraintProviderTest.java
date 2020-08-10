package org.optaplanner.examples.pas.solver;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.examples.pas.domain.Bed;
import org.optaplanner.examples.pas.domain.BedDesignation;
import org.optaplanner.examples.pas.domain.Department;
import org.optaplanner.examples.pas.domain.DepartmentSpecialism;
import org.optaplanner.examples.pas.domain.Equipment;
import org.optaplanner.examples.pas.domain.Gender;
import org.optaplanner.examples.pas.domain.GenderLimitation;
import org.optaplanner.examples.pas.domain.Patient;
import org.optaplanner.examples.pas.domain.PatientAdmissionSchedule;
import org.optaplanner.examples.pas.domain.PreferredPatientEquipment;
import org.optaplanner.examples.pas.domain.RequiredPatientEquipment;
import org.optaplanner.examples.pas.domain.Room;
import org.optaplanner.examples.pas.domain.RoomEquipment;
import org.optaplanner.examples.pas.domain.RoomSpecialism;
import org.optaplanner.examples.pas.domain.Specialism;
import org.optaplanner.examples.pas.solver.score.PatientAdmissionMoveConstraintProvider;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class PatientAdmissionMoveConstraintProviderTest {

    private final ConstraintVerifier<PatientAdmissionMoveConstraintProvider, PatientAdmissionSchedule> constraintVerifier =
            ConstraintVerifier
                    .build(new PatientAdmissionMoveConstraintProvider(), PatientAdmissionSchedule.class, BedDesignation.class);

    private static Stream genderLimitationsProvider() {
        return Stream.of(
                Arguments.of(Gender.FEMALE, GenderLimitation.MALE_ONLY,
                        (BiFunction<PatientAdmissionMoveConstraintProvider, ConstraintFactory, Constraint>) PatientAdmissionMoveConstraintProvider::femaleInMaleRoomConstraint),
                Arguments.of(Gender.MALE, GenderLimitation.FEMALE_ONLY,
                        (BiFunction<PatientAdmissionMoveConstraintProvider, ConstraintFactory, Constraint>) PatientAdmissionMoveConstraintProvider::maleInFemaleRoomConstraint));
    }

    private static Stream departmentAgeLimitationProvider() {
        Department adultDepartment = new Department();
        adultDepartment.setMinimumAge(18);
        adultDepartment.setId(1L);

        Department underageDepartment = new Department();
        underageDepartment.setMaximumAge(18);
        underageDepartment.setId(1L);

        return Stream.of(
                Arguments.of(adultDepartment, 5,
                        (BiFunction<PatientAdmissionMoveConstraintProvider, ConstraintFactory, Constraint>) PatientAdmissionMoveConstraintProvider::departmentMinimumAgeConstraint),
                Arguments.of(underageDepartment, 42,
                        (BiFunction<PatientAdmissionMoveConstraintProvider, ConstraintFactory, Constraint>) PatientAdmissionMoveConstraintProvider::departmentMaximumAgeConstraint));
    }

    @ParameterizedTest
    @MethodSource("genderLimitationsProvider")
    public void genderRoomLimitationConstraintTest(Gender gender, GenderLimitation genderLimitation,
            BiFunction constraintFunction) {

        Room room = new Room();
        room.setGenderLimitation(genderLimitation);

        Patient patient = new Patient();
        patient.setGender(gender);

        BedDesignation femaleInMaleRoom = new BedDesignation().build()
                .withPatient(patient)
                .withRoom(room)
                .withNights(0, 5);

        constraintVerifier.verifyThat(constraintFunction)
                .given(femaleInMaleRoom)
                .penalizesBy(6);
    }

    @Test
    public void sameBedInSameNightConstraintTest() {

        Bed bed = new Bed();

        BedDesignation designation = new BedDesignation().build()
                .withId(1L)
                .withBed(bed)
                .withNights(0, 5);

        BedDesignation sameBedAndNightsDesignation = new BedDesignation().build()
                .withId(2L)
                .withBed(bed)
                .withNights(0, 2);

        constraintVerifier.verifyThat(PatientAdmissionMoveConstraintProvider::sameBedInSameNightConstraint)
                .given(designation, sameBedAndNightsDesignation)
                .penalizesBy(3);
    }

    @ParameterizedTest
    @MethodSource("departmentAgeLimitationProvider")
    public void departmentAgeLimitationConstraintTest(Department department, int patientAge, BiFunction constraintFunction) {

        Room room = new Room();
        room.setDepartment(department);

        Patient patient = new Patient();
        patient.setAge(patientAge);

        BedDesignation designation = new BedDesignation().build()
                .withNights(0, 5)
                .withPatient(patient)
                .withRoom(room);

        constraintVerifier.verifyThat(constraintFunction)
                .given(designation, department)
                .penalizesBy(6);
    }

    @Test
    public void requiredPatientEquipmentConstraintTest() {

        Patient patient = new Patient();
        Room room = new Room();

        Equipment equipment1 = new Equipment();
        Equipment equipment2 = new Equipment();

        BedDesignation designation = new BedDesignation().build()
                .withNights(0, 5)
                .withPatient(patient)
                .withRoom(room);

        //ReqPatientEq1
        RequiredPatientEquipment requiredPatientEquipment1 = new RequiredPatientEquipment();
        requiredPatientEquipment1.setPatient(patient);
        requiredPatientEquipment1.setEquipment(equipment1);
        //ReqPatientEq2
        RequiredPatientEquipment requiredPatientEquipment2 = new RequiredPatientEquipment();
        requiredPatientEquipment2.setPatient(patient);
        requiredPatientEquipment2.setEquipment(equipment2);
        //RoomEquipment
        RoomEquipment roomEquipment = new RoomEquipment();
        roomEquipment.setEquipment(equipment2);
        roomEquipment.setRoom(room);

        constraintVerifier.verifyThat(PatientAdmissionMoveConstraintProvider::requiredPatientEquipmentConstraint)
                .given(requiredPatientEquipment1, requiredPatientEquipment2, roomEquipment, designation)
                .penalizesBy(6);
    }

    @Test
    public void differentGenderInSameGenderRoomInSameNightConstraintTest() {
        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.SAME_GENDER);

        //Assign female
        Patient female = new Patient();
        female.setGender(Gender.FEMALE);

        Bed bed1 = new Bed();
        bed1.setRoom(room);

        BedDesignation bedDesignation1 = new BedDesignation().build()
                .withNights(0, 5)
                .withId(1L)
                .withBed(bed1)
                .withPatient(female);

        //Assign male
        Patient male = new Patient();
        male.setGender(Gender.MALE);

        Bed bed2 = new Bed();
        bed2.setRoom(room);

        BedDesignation bedDesignation2 = new BedDesignation().build()
                .withNights(0, 5)
                .withId(2L)
                .withBed(bed2)
                .withPatient(male);

        constraintVerifier
                .verifyThat(PatientAdmissionMoveConstraintProvider::differentGenderInSameGenderRoomInSameNightConstraint)
                .given(bedDesignation1, bedDesignation2)
                .penalizesBy(6);
    }

    @Test
    public void assignEveryPatientToABedConstraintTest() {

        BedDesignation unassignedBed = new BedDesignation().build()
                .withNights(0, 2)
                .withBed(null);

        constraintVerifier
                .verifyThat(PatientAdmissionMoveConstraintProvider::assignEveryPatientToABedConstraint)
                .given(unassignedBed)
                .penalizesBy(3);
    }

    @Test()
    public void preferredMaximumRoomCapacityConstraintTest() {

        Patient patient = new Patient();
        patient.setPreferredMaximumRoomCapacity(3);

        Room room = new Room();
        room.setCapacity(6);

        BedDesignation capacityReferenced = new BedDesignation().build()
                .withNights(0, 2)
                .withRoom(room)
                .withPatient(patient);

        constraintVerifier
                .verifyThat(PatientAdmissionMoveConstraintProvider::preferredMaximumRoomCapacityConstraint)
                .given(capacityReferenced)
                .penalizesBy(3);
    }

    @Test
    public void preferredPatientEquipmentConstraintTest() {

        Patient patient = new Patient();

        Room room = new Room();

        Equipment equipment1 = new Equipment();
        Equipment equipment2 = new Equipment();

        Bed bed = new Bed();
        bed.setRoom(room);

        BedDesignation bedDesignation = new BedDesignation().build()
                .withNights(0, 5)
                .withPatient(patient)
                .withBed(bed);

        PreferredPatientEquipment preferredPatientEquipment1 = new PreferredPatientEquipment();
        preferredPatientEquipment1.setEquipment(equipment1);
        preferredPatientEquipment1.setPatient(patient);

        PreferredPatientEquipment preferredPatientEquipment2 = new PreferredPatientEquipment();
        preferredPatientEquipment2.setEquipment(equipment2);
        preferredPatientEquipment2.setPatient(patient);

        RoomEquipment roomEquippedOnlyByOneEq = new RoomEquipment();
        roomEquippedOnlyByOneEq.setEquipment(equipment2);
        roomEquippedOnlyByOneEq.setRoom(room);

        constraintVerifier.verifyThat(PatientAdmissionMoveConstraintProvider::preferredPatientEquipmentConstraint)
                .given(preferredPatientEquipment1, preferredPatientEquipment2, roomEquippedOnlyByOneEq, bedDesignation)
                .penalizesBy(6);
    }

    @Test
    public void departmentSpecialismConstraintTest() {

        Department department = new Department();

        Room roomInDep = new Room();
        roomInDep.setDepartment(department);

        Bed bedInDep = new Bed();
        bedInDep.setRoom(roomInDep);

        //Designation with 1st spec
        Specialism spec1 = new Specialism();
        BedDesignation designationWithDepartmentSpecialism1 = new BedDesignation().build()
                .withNights(0, 5)
                .withBed(bedInDep)
                .withSpecialism(spec1);

        //Designation with 2nd spec
        Specialism spec2 = new Specialism();
        BedDesignation designationWithDepartmentSpecialism2 = new BedDesignation().build()
                .withNights(0, 5)
                .withBed(bedInDep)
                .withSpecialism(spec2);

        DepartmentSpecialism departmentSpecialismWithOneSpec = new DepartmentSpecialism();
        departmentSpecialismWithOneSpec.setDepartment(department);
        departmentSpecialismWithOneSpec.setSpecialism(spec1);

        constraintVerifier.verifyThat(PatientAdmissionMoveConstraintProvider::departmentSpecialismConstraint)
                .given(designationWithDepartmentSpecialism1, designationWithDepartmentSpecialism2,
                        departmentSpecialismWithOneSpec)
                .penalizesBy(6);
    }

    @Test
    public void roomSpecialismConstraintTest() {

        Room roomInDep = new Room();
        Bed bedInDep = new Bed();
        bedInDep.setRoom(roomInDep);

        //Designation with 1st spec
        Specialism spec1 = new Specialism();
        BedDesignation designationWithRoomSpecialism1 = new BedDesignation().build()
                .withNights(0, 5)
                .withBed(bedInDep)
                .withSpecialism(spec1);

        //Designation with 2nd spec
        Specialism spec2 = new Specialism();
        BedDesignation designationWithRoomSpecialism2 = new BedDesignation().build()
                .withNights(0, 5)
                .withBed(bedInDep)
                .withSpecialism(spec2);

        RoomSpecialism roomSpecialism = new RoomSpecialism();
        roomSpecialism.setRoom(roomInDep);
        roomSpecialism.setSpecialism(spec1);

        constraintVerifier.verifyThat(PatientAdmissionMoveConstraintProvider::roomSpecialismNotExistsConstraint)
                .given(designationWithRoomSpecialism1, designationWithRoomSpecialism2, roomSpecialism)
                .penalizesBy(6);
    }

    @Test
    public void roomSpecialismNotFirstPriorityConstraintConstraintTest() {

        Room roomInDep = new Room();
        Bed bedInDep = new Bed();

        bedInDep.setRoom(roomInDep);
        //Designation with 1st spec
        Specialism spec1 = new Specialism();
        BedDesignation designationWithRoomSpecialism1 = new BedDesignation().build()
                .withNights(0, 5)
                .withBed(bedInDep)
                .withSpecialism(spec1);

        //Designation with 2nd spec
        Specialism spec2 = new Specialism();
        BedDesignation designationWithRoomSpecialism2 = new BedDesignation().build()
                .withNights(0, 5)
                .withBed(bedInDep)
                .withSpecialism(spec2);

        RoomSpecialism roomSpecialism = new RoomSpecialism();
        roomSpecialism.setRoom(roomInDep);
        roomSpecialism.setSpecialism(spec1);
        roomSpecialism.setPriority(2);

        constraintVerifier.verifyThat(PatientAdmissionMoveConstraintProvider::roomSpecialismNotFirstPriorityConstraint)
                .given(designationWithRoomSpecialism1, designationWithRoomSpecialism2, roomSpecialism)
                .penalizesBy(6);
    }
}
