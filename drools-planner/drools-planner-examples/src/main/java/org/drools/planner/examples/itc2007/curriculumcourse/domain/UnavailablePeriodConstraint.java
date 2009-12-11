package org.drools.planner.examples.itc2007.curriculumcourse.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class UnavailablePeriodConstraint extends AbstractPersistable
        implements Comparable<UnavailablePeriodConstraint> {

    private Course course;
    private Period period;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public int compareTo(UnavailablePeriodConstraint other) {
        return new CompareToBuilder()
                .append(course, other.course)
                .append(period, other.period)
                .toComparison();
    }

    @Override
    public String toString() {
        return course + "@" + period;
    }

}