package org.drools.planner.examples.common.business;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.core.score.constraint.ConstraintType;

/**
 * @author Geoffrey De Smet
 */
public class ScoreDetail implements Comparable<ScoreDetail> {

    private String ruleId;
    private ConstraintType constraintType;
    private double scoreTotal = 0.0;
    private int occurrenceSize = 0;

    public ScoreDetail(String ruleId, ConstraintType constraintType) {
        this.ruleId = ruleId;
        this.constraintType = constraintType;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(ConstraintType constraintType) {
        this.constraintType = constraintType;
    }

    public double getScoreTotal() {
        return scoreTotal;
    }

    public void setScoreTotal(double scoreTotal) {
        this.scoreTotal = scoreTotal;
    }

    public int getOccurrenceSize() {
        return occurrenceSize;
    }

    public void setOccurrenceSize(int occurrenceSize) {
        this.occurrenceSize = occurrenceSize;
    }


    public void addOccurrenceScore(double occurrenceScore) {
        scoreTotal += occurrenceScore;
        occurrenceSize++;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ScoreDetail) {
            ScoreDetail other = (ScoreDetail) o;
            return new EqualsBuilder()
                    .append(ruleId, other.ruleId)
                    .append(constraintType, other.constraintType)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(ruleId)
                .append(constraintType)
                .toHashCode();
    }

    public int compareTo(ScoreDetail other) {
        return new CompareToBuilder()
                .append(constraintType, other.constraintType)
                .append(ruleId, other.ruleId)
                .toComparison();
    }

    public String toString() {
        return ruleId + "/" + constraintType + "=" + scoreTotal + "(" + occurrenceSize + ")";
    }

}
