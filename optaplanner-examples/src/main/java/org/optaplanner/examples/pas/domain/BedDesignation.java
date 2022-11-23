package org.optaplanner.examples.pas.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.examples.common.domain.AbstractPersistableJackson;
import org.optaplanner.examples.pas.domain.solver.BedDesignationDifficultyWeightFactory;
import org.optaplanner.examples.pas.domain.solver.BedStrengthComparator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity(difficultyWeightFactoryClass = BedDesignationDifficultyWeightFactory.class)
public class BedDesignation extends AbstractPersistableJackson {

    private AdmissionPart admissionPart;
    private Bed bed;

    public BedDesignation() {
    }

    public BedDesignation(long id, AdmissionPart admissionPart, Bed bed) {
        super(id);
        this.admissionPart = admissionPart;
        this.bed = bed;
    }

    public AdmissionPart getAdmissionPart() {
        return admissionPart;
    }

    public void setAdmissionPart(AdmissionPart admissionPart) {
        this.admissionPart = admissionPart;
    }

    @PlanningVariable(nullable = true, valueRangeProviderRefs = {
            "bedRange" }, strengthComparatorClass = BedStrengthComparator.class)
    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Patient getPatient() {
        return admissionPart.getPatient();
    }

    public Gender getPatientGender() {
        return admissionPart.getPatient().getGender();
    }

    public int getPatientAge() {
        return admissionPart.getPatient().getAge();
    }

    public Integer getPatientPreferredMaximumRoomCapacity() {
        return admissionPart.getPatient().getPreferredMaximumRoomCapacity();
    }

    public Specialism getAdmissionPartSpecialism() {
        return admissionPart.getSpecialism();
    }

    public int getFirstNightIndex() {
        return admissionPart.getFirstNight().getIndex();
    }

    public int getLastNightIndex() {
        return admissionPart.getLastNight().getIndex();
    }

    public int getAdmissionPartNightCount() {
        return admissionPart.getNightCount();
    }

    @JsonIgnore
    public Room getRoom() {
        if (bed == null) {
            return null;
        }
        return bed.getRoom();
    }

    @JsonIgnore
    public int getRoomCapacity() {
        if (bed == null) {
            return Integer.MIN_VALUE;
        }
        return bed.getRoom().getCapacity();
    }

    @JsonIgnore
    public Department getDepartment() {
        if (bed == null) {
            return null;
        }
        return bed.getRoom().getDepartment();
    }

    @JsonIgnore
    public GenderLimitation getRoomGenderLimitation() {
        if (bed == null) {
            return null;
        }
        return bed.getRoom().getGenderLimitation();
    }

    @Override
    public String toString() {
        return admissionPart.toString();
    }

}
