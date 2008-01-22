package org.drools.solver.examples.itc2007.examination.solver.move;

import java.util.Collection;
import java.util.Collections;

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
public class PeriodChangeMove implements Move, TabuPropertyEnabled {

    private Exam exam;
    private Period toPeriod;

    public PeriodChangeMove(Exam exam, Period toPeriod) {
        this.exam = exam;
        this.toPeriod = toPeriod;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(exam.getPeriod(), toPeriod);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new PeriodChangeMove(exam, exam.getPeriod());
    }

    public void doMove(WorkingMemory workingMemory) {
        FactHandle examHandle = workingMemory.getFactHandle(exam);
        exam.setPeriod(toPeriod);
        workingMemory.update(examHandle, exam);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(exam);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PeriodChangeMove) {
            PeriodChangeMove other = (PeriodChangeMove) o;
            return new EqualsBuilder()
                    .append(exam, other.exam)
                    .append(toPeriod, other.toPeriod)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(exam)
                .append(toPeriod)
                .toHashCode();
    }

    public String toString() {
        return exam + " => " + toPeriod;
    }

}