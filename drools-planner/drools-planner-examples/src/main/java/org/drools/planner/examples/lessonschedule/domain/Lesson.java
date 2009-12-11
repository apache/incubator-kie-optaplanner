package org.drools.planner.examples.lessonschedule.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Lesson extends AbstractPersistable implements Comparable<Lesson> {

    private Teacher teacher;
    private Group group;

    private Timeslot timeslot;

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public int compareTo(Lesson other) {
        return new CompareToBuilder()
                .append(timeslot, other.timeslot)
                .append(teacher, other.teacher)
                .append(group, other.group)
                .append(id, other.id)
                .toComparison();
    }

    public Lesson clone() {
        Lesson clone = new Lesson();
        clone.id = id;
        clone.teacher = teacher;
        clone.group = group;
        clone.timeslot = timeslot;
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() + " " + teacher + " + " + group + " @ " + timeslot;
    }

}
