package org.drools.solver.examples.patientadmissionschedule.solver.move;

import java.util.Arrays;
import java.util.Collection;

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
public class BedDesignationSwitchMove implements Move, TabuPropertyEnabled {

    private BedDesignation leftBedDesignation;
    private BedDesignation rightBedDesignation;

    public BedDesignationSwitchMove(BedDesignation leftBedDesignation, BedDesignation rightBedDesignation) {
        this.leftBedDesignation = leftBedDesignation;
        this.rightBedDesignation = rightBedDesignation;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(leftBedDesignation.getBed(), rightBedDesignation.getBed())
                && rightBedDesignation.getBed().allowsAdmissionPart(leftBedDesignation.getAdmissionPart())
                && leftBedDesignation.getBed().allowsAdmissionPart(rightBedDesignation.getAdmissionPart());
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new BedDesignationSwitchMove(rightBedDesignation, leftBedDesignation);
    }

    public void doMove(WorkingMemory workingMemory) {
        Bed oldLeftBed = leftBedDesignation.getBed();
        Bed oldRightBed = rightBedDesignation.getBed();
        moveBed(workingMemory, leftBedDesignation, oldRightBed);
        moveBed(workingMemory, rightBedDesignation, oldLeftBed);
    }

    // Extract to helper class if other moves are created
    private static void moveBed(WorkingMemory workingMemory, BedDesignation bedDesignation, Bed toBed) {
        FactHandle factHandle = workingMemory.getFactHandle(bedDesignation);
        bedDesignation.setBed(toBed);
        workingMemory.update(factHandle, bedDesignation);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<BedDesignation>asList(leftBedDesignation, rightBedDesignation);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof BedDesignationSwitchMove) {
            BedDesignationSwitchMove other = (BedDesignationSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftBedDesignation, other.leftBedDesignation)
                    .append(rightBedDesignation, other.rightBedDesignation)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftBedDesignation)
                .append(rightBedDesignation)
                .toHashCode();
    }

    public String toString() {
        return leftBedDesignation + " <=> " + rightBedDesignation;
    }

}