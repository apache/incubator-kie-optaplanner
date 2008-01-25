package org.drools.solver.examples.itc2007.examination.domain;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Topic extends AbstractPersistable implements Comparable<Topic> {

    private int duration; // in minutes
    private List<Student> studentList;

    // Calculated during initialization, not modified during score calculation.
    private boolean frontLoadLarge;
    private Set<Topic> coincidenceTopicSet = null;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public int getStudentSize() {
        return studentList.size();
    }

    public boolean isFrontLoadLarge() {
        return frontLoadLarge;
    }

    public void setFrontLoadLarge(boolean frontLoadLarge) {
        this.frontLoadLarge = frontLoadLarge;
    }

    public Set<Topic> getCoincidenceTopicSet() {
        return coincidenceTopicSet;
    }

    public void setCoincidenceTopicSet(Set<Topic> coincidenceTopicSet) {
        this.coincidenceTopicSet = coincidenceTopicSet;
    }


    public boolean hasCoincidenceTopic() {
        return coincidenceTopicSet != null;
    }

    public int compareTo(Topic other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    public String toString() {
        return id + " {D" + duration + "|S" + getStudentSize() + "}";
    }

}
