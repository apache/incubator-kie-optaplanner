package org.drools.planner.examples.nurserostering.domain.request;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("DayOnRequest")
public class DayOnRequest extends AbstractPersistable implements Comparable<DayOnRequest> {

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

    public int compareTo(DayOnRequest other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(shiftDate, other.shiftDate)
                .toComparison();
    }

    @Override
    public String toString() {
        return shiftDate + "_ON_" + employee;
    }

}
