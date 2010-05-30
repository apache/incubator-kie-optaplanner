package org.drools.planner.examples.nurserostering.domain.contract;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;
import org.drools.planner.examples.nurserostering.domain.Pattern;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("PatternContractLine")
public class PatternContractLine extends AbstractPersistable implements Comparable<PatternContractLine> {

    private Contract contract;
    private Pattern pattern;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public int compareTo(PatternContractLine other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return contract + "-" + pattern;
    }

}
