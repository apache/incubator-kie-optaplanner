package org.drools.planner.examples.nurserostering.domain.contract;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("ContractLine")
public abstract class ContractLine extends AbstractPersistable implements Comparable<ContractLine> {

    private Contract contract;
    private ContractLineType contractLineType;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ContractLineType getContractLineType() {
        return contractLineType;
    }

    public void setContractLineType(ContractLineType contractLineType) {
        this.contractLineType = contractLineType;
    }

    public int compareTo(ContractLine other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return contract + "-" + contractLineType;
    }
}
