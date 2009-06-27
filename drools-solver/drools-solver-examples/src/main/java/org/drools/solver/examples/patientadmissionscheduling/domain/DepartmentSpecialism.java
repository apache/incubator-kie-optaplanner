package org.drools.solver.examples.patientadmissionscheduling.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;
import org.drools.solver.examples.manners2009.domain.Guest;
import org.drools.solver.examples.manners2009.domain.Hobby;
import org.drools.solver.examples.manners2009.domain.HobbyPractician;

/**
 * @author Geoffrey De Smet
 */
public class DepartmentSpecialism extends AbstractPersistable implements Comparable<DepartmentSpecialism> {

    private Department department;
    private Specialism specialism;

    private int priority; // AKA choice

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Specialism getSpecialism() {
        return specialism;
    }

    public void setSpecialism(Specialism specialism) {
        this.specialism = specialism;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int compareTo(DepartmentSpecialism other) {
        return new CompareToBuilder()
                .append(department, other.department)
                .append(specialism, other.specialism)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return department + "-" + specialism;
    }

}