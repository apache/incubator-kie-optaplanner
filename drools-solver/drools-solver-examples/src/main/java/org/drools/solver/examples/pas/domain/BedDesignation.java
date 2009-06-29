package org.drools.solver.examples.pas.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class BedDesignation extends AbstractPersistable implements Comparable<BedDesignation> {

    private AdmissionPart admissionPart;
    private Bed bed;

    public AdmissionPart getAdmissionPart() {
        return admissionPart;
    }

    public void setAdmissionPart(AdmissionPart admissionPart) {
        this.admissionPart = admissionPart;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

    public int compareTo(BedDesignation other) {
        return new CompareToBuilder()
                .append(admissionPart, other.admissionPart)
                .append(bed, other.bed)
                .append(id, other.id)
                .toComparison();
    }

    public BedDesignation clone() {
        BedDesignation clone = new BedDesignation();
        clone.id = id;
        clone.admissionPart = admissionPart;
        clone.bed = bed;
        return clone;
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionHashCode()
     */
    public boolean solutionEquals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof BedDesignation) {
            BedDesignation other = (BedDesignation) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(admissionPart, other.admissionPart)
                    .append(bed, other.bed)
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionEquals(Object)
     */
    public int solutionHashCode() {
        return new HashCodeBuilder()
                .append(bed)
                .toHashCode();
    }

    @Override
    public String toString() {
        return admissionPart + " @ " + bed;
    }

    public Patient getPatient() {
        return admissionPart.getPatient();
    }

    public int getPatientPreferredMaximumRoomCapacity() {
        return admissionPart.getPatient().getPreferredMaximumRoomCapacity();
    }

    public Specialism getAdmissionPartSpecialism() {
        return admissionPart.getSpecialism();
    }

    public Room getRoom() {
        return bed.getRoom();
    }

    public int getRoomCapacity() {
        return bed.getRoom().getCapacity();
    }

    public Department getDepartment() {
        return bed.getRoom().getDepartment();
    }

    public GenderLimitation getRoomGenderLimitation() {
        return bed.getRoom().getGenderLimitation();
    }

}