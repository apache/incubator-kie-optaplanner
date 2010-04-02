package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("EmployeeAssignment")
public class EmployeeAssignment extends AbstractPersistable implements Comparable<EmployeeAssignment> {

    private Employee employee;
    private ShiftDate shiftDate;

    // Changed by moves, between score calculations.
    // Can be null, if the employee is not working on the shiftDate
    private Shift shift;

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

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        if (shift != null && !shift.getShiftDate().equals(shiftDate)) {
            throw new IllegalArgumentException("The EmployeeAssignment (" + this + ") cannot have a shift (" + shift
                    + ") with a different shiftDate(" + shiftDate + ").");
        }
        this.shift = shift;
    }

    public String getLabel() {
        return employee + "->" + (shift == null ?  "FREE" : shift.getShiftType());
    }

    public int compareTo(EmployeeAssignment other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(shiftDate, other.shiftDate)
                .append(shift, other.shift)
                .toComparison();
    }

    public EmployeeAssignment clone() {
        EmployeeAssignment clone = new EmployeeAssignment();
        clone.id = id;
        clone.employee = employee;
        clone.shiftDate = shiftDate;
        clone.shift = shift;
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
        } else if (o instanceof EmployeeAssignment) {
            EmployeeAssignment other = (EmployeeAssignment) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(employee, other.employee)
                    .append(shiftDate, other.shiftDate)
                    .append(shift, other.shift)
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
                .append(id)
                .append(employee)
                .append(shiftDate)
                .append(shift)
                .toHashCode();
    }

    @Override
    public String toString() {
        return employee + "(" + shiftDate + ")" + "->" + shift;
    }

    public int getShiftDateDayIndex() {
        return getShiftDate().getDayIndex();
    }

}
