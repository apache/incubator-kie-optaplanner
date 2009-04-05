package org.drools.solver.core.score;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Default implementation of {@link SimpleScore}.
 * <p/>
 * This class is immutable.
 * @author Geoffrey De Smet
 */
public final class DefaultSimpleScore extends AbstractScore<SimpleScore>
        implements SimpleScore {

    public static Score valueOf(int score) {
        return new DefaultSimpleScore(score);
    }

    public static Score valueOf(String scoreString) {
        return valueOf(Integer.parseInt(scoreString));
    }

    private final int score;

    public DefaultSimpleScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean equals(Object o) {
        // A direct implementation (instead of EqualsBuilder) for to avoid dependencies
        if (this == o) {
            return true;
        } else if (o instanceof SimpleScore) {
            SimpleScore other = (SimpleScore) o;
            return score == other.getScore();
        } else {
            return false;
        }
    }

    public int hashCode() {
        // A direct implementation (instead of HashCodeBuilder) for to avoid dependencies
        return (17 * 37) + score;
    }

    public int compareTo(SimpleScore other) {
        // A direct implementation (instead of CompareToBuilder) for to avoid dependencies
       if (score < other.getScore()) {
           return -1;
       } else if (score > other.getScore()) {
           return 1;
       } else {
           return 0;
       }
    }

    public String toString() {
        return Integer.toString(score);
    }

}