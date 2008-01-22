package org.drools.solver.examples.itc2007.examination.solver.move;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.decider.accepter.tabu.TabuPropertyEnabled;
import org.drools.solver.core.move.Move;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Period;

/**
 * @author Geoffrey De Smet
 */
public class PeriodChangeBulkMove implements Move, TabuPropertyEnabled {

    private Collection<Exam> exams;
    private Period toPeriod;

    public PeriodChangeBulkMove(Collection<Exam> exams, Period toPeriod) {
        this.exams = exams;
        if (exams.isEmpty()) {
            throw new IllegalArgumentException("The exams is empty.");
        }
        this.toPeriod = toPeriod;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(extractFromPeriod(), toPeriod);
    }

    private Period extractFromPeriod() {
        return exams.iterator().next().getPeriod();
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new PeriodChangeBulkMove(exams, extractFromPeriod());
    }

    public void doMove(WorkingMemory workingMemory) {
        for (Exam exam : exams) {
            FactHandle examHandle = workingMemory.getFactHandle(exam);
            exam.setPeriod(toPeriod);
            workingMemory.update(examHandle, exam);
        }
    }

    public Collection<? extends Object> getTabuProperties() {
        return exams;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PeriodChangeBulkMove) {
            PeriodChangeBulkMove other = (PeriodChangeBulkMove) o;
            return new EqualsBuilder()
                    .append(exams, other.exams)
                    .append(toPeriod, other.toPeriod)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(exams)
                .append(toPeriod)
                .toHashCode();
    }

    public String toString() {
        return exams + " => " + toPeriod;
    }

}