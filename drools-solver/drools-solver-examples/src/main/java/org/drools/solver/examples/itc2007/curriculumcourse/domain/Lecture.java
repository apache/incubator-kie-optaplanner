package org.drools.solver.examples.itc2007.curriculumcourse.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Lecture extends AbstractPersistable implements Comparable<Lecture> {

    private Course course;
    private int lectureIndexInCourse;

    // Changed by moves, between score calculations.
    private Period period;
    private Room room;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getLectureIndexInCourse() {
        return lectureIndexInCourse;
    }

    public void setLectureIndexInCourse(int lectureIndexInCourse) {
        this.lectureIndexInCourse = lectureIndexInCourse;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }


    public int getStudentSize() {
        return course.getStudentSize();
    }

    public int compareTo(Lecture other) {
        return new CompareToBuilder()
                .append(period, other.period)
                .append(room, other.room)
                .append(course, other.course)
                .toComparison();
    }

    public Lecture clone() {
        Lecture clone = new Lecture();
        clone.id = id;
        clone.course = course;
        clone.lectureIndexInCourse = lectureIndexInCourse;
        clone.period = period;
        clone.room = room;
        return clone;
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionHashCode()
     */
    public boolean solutionEquals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Lecture) {
            Lecture other = (Lecture) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(course, other.course)
                    .append(period, other.period)
                    .append(room, other.room)
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionEquals(Object)
     */
    public int solutionHashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(course)
                .append(period)
                .append(room)
                .toHashCode();
    }

    @Override
    public String toString() {
        return course + "-" + lectureIndexInCourse + " @ " + period + " + " + room;
    }

}