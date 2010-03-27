package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("ShiftOffRequest")
public class ShiftOffRequest extends AbstractPersistable implements Comparable<ShiftOffRequest> {

    private Employee employee;
    private Shift shift;
    private int weight;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int compareTo(ShiftOffRequest other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(shift, other.shift)
                .toComparison();
    }

    @Override
    public String toString() {
        return shift + "_OFF_" + employee;
    }

}
