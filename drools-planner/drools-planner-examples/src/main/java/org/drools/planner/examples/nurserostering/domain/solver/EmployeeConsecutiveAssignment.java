package org.drools.planner.examples.nurserostering.domain.solver;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;

/**
 * @author Geoffrey De Smet
 */
public class EmployeeConsecutiveAssignment implements Serializable {

    private Employee employee;
    private ShiftDate firstShiftDate;
    private ShiftDate lastShiftDate;

    public EmployeeConsecutiveAssignment(Employee employee, ShiftDate firstShiftDate, ShiftDate lastShiftDate) {
        this.employee = employee;
        this.firstShiftDate = firstShiftDate;
        this.lastShiftDate = lastShiftDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ShiftDate getFirstShiftDate() {
        return firstShiftDate;
    }

    public void setFirstShiftDate(ShiftDate firstShiftDate) {
        this.firstShiftDate = firstShiftDate;
    }

    public ShiftDate getLastShiftDate() {
        return lastShiftDate;
    }

    public void setLastShiftDate(ShiftDate lastShiftDate) {
        this.lastShiftDate = lastShiftDate;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof EmployeeConsecutiveAssignment) {
            EmployeeConsecutiveAssignment other = (EmployeeConsecutiveAssignment) o;
            return new EqualsBuilder()
                    .append(employee, other.employee)
                    .append(firstShiftDate, other.firstShiftDate)
                    .append(lastShiftDate, other.lastShiftDate)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(employee)
                .append(firstShiftDate)
                .append(lastShiftDate)
                .toHashCode();
    }

    public int getFirstShiftDateDayIndex() {
        return firstShiftDate.getDayIndex();
    }

    public int getLastShiftDateDayIndex() {
        return lastShiftDate.getDayIndex();
    }

    public int getDayLength() {
        return lastShiftDate.getDayIndex() - firstShiftDate.getDayIndex() + 1;
    }

}
