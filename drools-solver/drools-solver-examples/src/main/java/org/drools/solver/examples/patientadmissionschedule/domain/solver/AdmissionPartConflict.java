package org.drools.solver.examples.patientadmissionschedule.domain.solver;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.examples.patientadmissionschedule.domain.AdmissionPart;

/**
 * Calculated during initialization, not modified during score calculation.
 * @author Geoffrey De Smet
 */
public class AdmissionPartConflict implements Serializable {

    private AdmissionPart leftAdmissionPart;
    private AdmissionPart rightAdmissionPart;
    private int nightSize;

    public AdmissionPartConflict(AdmissionPart leftAdmissionPart, AdmissionPart rightAdmissionPart, int nightSize) {
        this.leftAdmissionPart = leftAdmissionPart;
        this.rightAdmissionPart = rightAdmissionPart;
        this.nightSize = nightSize;
    }

    public AdmissionPart getLeftAdmissionPart() {
        return leftAdmissionPart;
    }

    public void setLeftAdmissionPart(AdmissionPart leftAdmissionPart) {
        this.leftAdmissionPart = leftAdmissionPart;
    }

    public AdmissionPart getRightAdmissionPart() {
        return rightAdmissionPart;
    }

    public void setRightAdmissionPart(AdmissionPart rightAdmissionPart) {
        this.rightAdmissionPart = rightAdmissionPart;
    }

    public int getNightSize() {
        return nightSize;
    }

    public void setNightSize(int nightSize) {
        this.nightSize = nightSize;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof AdmissionPartConflict) {
            AdmissionPartConflict other = (AdmissionPartConflict) o;
            return new EqualsBuilder()
                    .append(leftAdmissionPart, other.leftAdmissionPart)
                    .append(rightAdmissionPart, other.rightAdmissionPart)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftAdmissionPart)
                .append(rightAdmissionPart)
                .toHashCode();
    }

    public int compareTo(AdmissionPartConflict other) {
        return new CompareToBuilder()
                .append(leftAdmissionPart, other.leftAdmissionPart)
                .append(rightAdmissionPart, other.rightAdmissionPart)
                .toComparison();
    }

    @Override
    public String toString() {
        return leftAdmissionPart + " & " + rightAdmissionPart + " = " + nightSize;
    }

    public boolean isDifferentGender() {
        return leftAdmissionPart.getPatient().getGender() != rightAdmissionPart.getPatient().getGender();
    }

}