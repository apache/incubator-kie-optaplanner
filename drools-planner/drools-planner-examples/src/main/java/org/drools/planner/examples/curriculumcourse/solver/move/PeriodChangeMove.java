package org.drools.planner.examples.curriculumcourse.solver.move;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.curriculumcourse.domain.Lecture;
import org.drools.planner.examples.curriculumcourse.domain.Period;

/**
 * @author Geoffrey De Smet
 */
public class PeriodChangeMove implements Move, TabuPropertyEnabled {

    private Lecture lecture;
    private Period toPeriod;

    public PeriodChangeMove(Lecture lecture, Period toPeriod) {
        this.lecture = lecture;
        this.toPeriod = toPeriod;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(lecture.getPeriod(), toPeriod);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new PeriodChangeMove(lecture, lecture.getPeriod());
    }

    public void doMove(WorkingMemory workingMemory) {
        CurriculumCourseMoveHelper.movePeriod(workingMemory, lecture, toPeriod);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(lecture);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PeriodChangeMove) {
            PeriodChangeMove other = (PeriodChangeMove) o;
            return new EqualsBuilder()
                    .append(lecture, other.lecture)
                    .append(toPeriod, other.toPeriod)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(lecture)
                .append(toPeriod)
                .toHashCode();
    }

    public String toString() {
        return lecture + " => " + toPeriod;
    }

}