package org.drools.solver.examples.itc2007.examination.domain.solver;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.examples.itc2007.examination.domain.Topic;

/**
 * Calculated during initialization, not modified during score calculation.
 * @author Geoffrey De Smet
 */
public class TopicConflict implements Serializable {

    private Topic leftTopic;
    private Topic rightTopic;
    private int studentSize;

    public TopicConflict(Topic leftTopic, Topic rightTopic, int studentSize) {
        this.leftTopic = leftTopic;
        this.rightTopic = rightTopic;
        this.studentSize = studentSize;
    }

    public Topic getLeftTopic() {
        return leftTopic;
    }

    public void setLeftTopic(Topic leftTopic) {
        this.leftTopic = leftTopic;
    }

    public Topic getRightTopic() {
        return rightTopic;
    }

    public void setRightTopic(Topic rightTopic) {
        this.rightTopic = rightTopic;
    }

    public int getStudentSize() {
        return studentSize;
    }

    public void setStudentSize(int studentSize) {
        this.studentSize = studentSize;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof TopicConflict) {
            TopicConflict other = (TopicConflict) o;
            return new EqualsBuilder()
                    .append(leftTopic, other.leftTopic)
                    .append(rightTopic, other.rightTopic)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftTopic)
                .append(rightTopic)
                .toHashCode();
    }

    public int compareTo(TopicConflict other) {
        return new CompareToBuilder()
                .append(leftTopic, other.leftTopic)
                .append(rightTopic, other.rightTopic)
                .toComparison();
    }

    @Override
    public String toString() {
        return leftTopic + " & " + rightTopic + " = " + studentSize;
    }

}