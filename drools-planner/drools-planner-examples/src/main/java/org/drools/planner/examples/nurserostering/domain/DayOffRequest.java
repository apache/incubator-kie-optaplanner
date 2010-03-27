package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("DayOffRequest")
public class DayOffRequest extends AbstractPersistable implements Comparable<DayOffRequest> {

    private Employee employee;
    private ShiftDate shiftDate;
    private int weight;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ShiftDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(ShiftDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int compareTo(DayOffRequest other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(shiftDate, other.shiftDate)
                .toComparison();
    }

    @Override
    public String toString() {
        return shiftDate + "_OFF_" + employee;
    }

}
