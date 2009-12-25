package org.drools.planner.examples.pas.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("RequiredPatientEquipment")
public class RequiredPatientEquipment extends AbstractPersistable implements Comparable<RequiredPatientEquipment> {

    private Patient patient;
    private Equipment equipment;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public int compareTo(RequiredPatientEquipment other) {
        return new CompareToBuilder()
                .append(patient, other.patient)
                .append(equipment, other.equipment)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return patient + "-" + equipment;
    }

}