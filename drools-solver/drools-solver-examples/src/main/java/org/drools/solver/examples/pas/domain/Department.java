package org.drools.solver.examples.pas.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Department extends AbstractPersistable implements Comparable<Department> {

    private String name;
    private Integer minimumAge = null;
    private Integer maximumAge = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(Integer minimumAge) {
        this.minimumAge = minimumAge;
    }

    public Integer getMaximumAge() {
        return maximumAge;
    }

    public void setMaximumAge(Integer maximumAge) {
        this.maximumAge = maximumAge;
    }

    public int compareTo(Department other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return name;
    }

    public int countDisallowedAdmissionPart(AdmissionPart admissionPart) {
        return countDisallowedPatientAge(admissionPart.getPatient());
    }

    public int countDisallowedPatientAge(Patient patient) {
        int count = 0;
        if (minimumAge != null && patient.getAge() < minimumAge) {
            count += 10;
        }
        if (maximumAge != null && patient.getAge() > maximumAge) {
            count += 10;
        }
        return count;
    }

}