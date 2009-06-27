package org.drools.solver.examples.patientadmissionschedule.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Room extends AbstractPersistable implements Comparable<Room> {

    private String name;

    private Department department;
    private int capacity;
    private GenderLimitation genderLimitation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public GenderLimitation getGenderLimitation() {
        return genderLimitation;
    }

    public void setGenderLimitation(GenderLimitation genderLimitation) {
        this.genderLimitation = genderLimitation;
    }

    public int compareTo(Room other) {
        return new CompareToBuilder()
                .append(department, other.department)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return department + "_" + name;
    }

}