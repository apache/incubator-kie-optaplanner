package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;
import org.drools.planner.examples.nurserostering.domain.contract.Contract;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Employee")
public class Employee extends AbstractPersistable implements Comparable<Employee> {

    private String code;
    private String name;
    private Contract contract;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public int compareTo(Employee other) {
        return new CompareToBuilder()
                .append(name, other.name)
                .toComparison();
    }

    @Override
    public String toString() {
        return code + ": " + name;
    }

}
