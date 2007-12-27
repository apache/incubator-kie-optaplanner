package org.drools.solver.examples.itc2007.examination.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Geoffrey De Smet
 */
public class TopicConflict implements Serializable {

    private Topic leftTopic;
    private Topic rightTopic;
    private int studentSize;

    public TopicConflict(Topic leftTopic, Topic rightTopic) {
        this.leftTopic = leftTopic;
        this.rightTopic = rightTopic;
    }

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

    // TODO decide to keep or remove this object logicallyAsserted
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

    // TODO decide to keep or remove this object logicallyAsserted
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