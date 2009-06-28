package org.drools.solver.examples.patientadmissionschedule.solver.move;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.runtime.rule.FactHandle;
import org.drools.solver.core.localsearch.decider.accepter.tabu.TabuPropertyEnabled;
import org.drools.solver.core.move.Move;
import org.drools.solver.examples.patientadmissionschedule.domain.Bed;
import org.drools.solver.examples.patientadmissionschedule.domain.BedDesignation;

/**
 * @author Geoffrey De Smet
 */
public class BedChangeMove implements Move, TabuPropertyEnabled {

    private BedDesignation bedDesignation;
    private Bed toBed;

    public BedChangeMove(BedDesignation bedDesignation, Bed toBed) {
        this.bedDesignation = bedDesignation;
        this.toBed = toBed;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(bedDesignation.getBed(), toBed);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new BedChangeMove(bedDesignation, bedDesignation.getBed());
    }

    public void doMove(WorkingMemory workingMemory) {
        FactHandle factHandle = workingMemory.getFactHandle(bedDesignation);
        bedDesignation.setBed(toBed);
        workingMemory.update(factHandle, bedDesignation);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(bedDesignation);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof BedChangeMove) {
            BedChangeMove other = (BedChangeMove) o;
            return new EqualsBuilder()
                    .append(bedDesignation, other.bedDesignation)
                    .append(toBed, other.toBed)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(bedDesignation)
                .append(toBed)
                .toHashCode();
    }

    public String toString() {
        return bedDesignation + " => " + toBed;
    }

}