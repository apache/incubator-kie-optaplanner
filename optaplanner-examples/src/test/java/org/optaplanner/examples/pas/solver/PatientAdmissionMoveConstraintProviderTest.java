package org.optaplanner.examples.pas.solver;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.pas.domain.AdmissionPart;
import org.optaplanner.examples.pas.domain.Bed;
import org.optaplanner.examples.pas.domain.BedDesignation;
import org.optaplanner.examples.pas.domain.Gender;
import org.optaplanner.examples.pas.domain.GenderLimitation;
import org.optaplanner.examples.pas.domain.Night;
import org.optaplanner.examples.pas.domain.Patient;
import org.optaplanner.examples.pas.domain.PatientAdmissionSchedule;
import org.optaplanner.examples.pas.domain.Room;
import org.optaplanner.examples.pas.solver.score.PatientAdmissionMoveConstraintProvider;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class PatientAdmissionMoveConstraintProviderTest {

    private final ConstraintVerifier<PatientAdmissionMoveConstraintProvider, PatientAdmissionSchedule> constraintVerifier =
            ConstraintVerifier
                    .build(new PatientAdmissionMoveConstraintProvider(), PatientAdmissionSchedule.class, BedDesignation.class);

    @Test
    public void femaleInMaleRoomTest() {
        Patient female = new Patient();
        female.setGender(Gender.FEMALE);

        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.MALE_ONLY);

        Night firstNight = new Night();
        firstNight.setIndex(0);

        Night lastNight = new Night();
        lastNight.setIndex(5);

        AdmissionPart femaleAdmission = new AdmissionPart();
        femaleAdmission.setPatient(female);
        femaleAdmission.setFirstNight(firstNight);
        femaleAdmission.setLastNight(lastNight);

        Bed bedInMaleOnlyRoom = new Bed();
        bedInMaleOnlyRoom.setRoom(room);

        BedDesignation bedDesignation = new BedDesignation();
        bedDesignation.setAdmissionPart(femaleAdmission);
        bedDesignation.setBed(bedInMaleOnlyRoom);

        constraintVerifier.verifyThat(PatientAdmissionMoveConstraintProvider::femaleInMaleRoom)
                .given(bedDesignation)
                .penalizesBy(6);
    }
}
