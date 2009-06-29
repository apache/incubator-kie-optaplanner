package org.drools.solver.examples.pas.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class PreferredPatientEquipment extends AbstractPersistable implements Comparable<PreferredPatientEquipment> {

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

    public int compareTo(PreferredPatientEquipment other) {
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