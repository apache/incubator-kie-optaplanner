package org.optaplanner.examples.pas.solver;

import org.optaplanner.examples.pas.domain.AdmissionPart;
import org.optaplanner.examples.pas.domain.Bed;
import org.optaplanner.examples.pas.domain.BedDesignation;
import org.optaplanner.examples.pas.domain.Night;
import org.optaplanner.examples.pas.domain.Patient;
import org.optaplanner.examples.pas.domain.Room;
import org.optaplanner.examples.pas.domain.Specialism;

public class BedDesignationDO extends BedDesignation {

    private BedDesignationDO(Builder builder) {
        this.setAdmissionPart(builder.admission);
        this.setBed(builder.bed);
    }

    public static class Builder {

        private Long id;
        private AdmissionPart admission;
        private Bed bed;


        public Builder() {
            this.admission = new AdmissionPart();
            this.bed = new Bed();
        }

        public Builder withNights(int first, int last) {
            Night firstNight = new Night();
            firstNight.setIndex(first);
            this.admission.setFirstNight(firstNight);
            Night lastNight = new Night();
            lastNight.setIndex(last);
            this.admission.setLastNight(lastNight);
            return this;
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPatient(Patient patient) {
            this.admission.setPatient(patient);
            return this;
        }

        public Builder withBed(Bed bed) {
            this.bed = bed;
            return this;
        }

        public Builder withRoom(Room room) {
            this.bed.setRoom(room);
            return this;
        }

        public Builder withSpecialism(Specialism spec1) {
            this.admission.setSpecialism(spec1);
            return this;
        }

        public BedDesignationDO build() {
            BedDesignationDO bedDesignationDO = new BedDesignationDO(this);
            bedDesignationDO.setId(this.id);
            return bedDesignationDO;
        }
    }
}
