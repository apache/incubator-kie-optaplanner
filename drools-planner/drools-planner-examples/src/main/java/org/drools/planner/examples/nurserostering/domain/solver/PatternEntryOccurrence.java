package org.drools.planner.examples.nurserostering.domain.solver;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.Pattern;
import org.drools.planner.examples.nurserostering.domain.PatternEntry;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;

/**
 * @author Geoffrey De Smet
 */
public class PatternEntryOccurrence implements Comparable<PatternEntryOccurrence>, Serializable {

    private PatternEntry patternEntry;
    private Employee employee;
    private ShiftDate shiftDate;

    public PatternEntryOccurrence(PatternEntry patternEntry, Employee employee, ShiftDate shiftDate) {
        this.patternEntry = patternEntry;
        this.employee = employee;
        this.shiftDate = shiftDate;
    }

    public PatternEntry getPatternEntry() {
        return patternEntry;
    }

    public void setPatternEntry(PatternEntry patternEntry) {
        this.patternEntry = patternEntry;
    }

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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PatternEntryOccurrence) {
            PatternEntryOccurrence other = (PatternEntryOccurrence) o;
            return new EqualsBuilder()
                    .append(patternEntry, other.patternEntry)
                    .append(employee, other.employee)
                    .append(shiftDate, other.shiftDate)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(patternEntry)
                .append(employee)
                .append(shiftDate)
                .toHashCode();
    }

    public int compareTo(PatternEntryOccurrence other) {
        return new CompareToBuilder()
                .append(patternEntry, other.patternEntry)
                .append(employee, other.employee)
                .append(shiftDate, other.shiftDate)
                .toComparison();
    }

    @Override
    public String toString() {
        return patternEntry + " for " + employee + " on " + shiftDate;
    }

    public Pattern getPattern() {
        return patternEntry.getPattern();
    }

    public int getEntryIndex() {
        return patternEntry.getEntryIndex();
    }

    public int getDayIndex() {
        return shiftDate.getDayIndex();
    }

}
