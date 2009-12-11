package org.drools.planner.core.score;

/**
 * Default implementation of {@link SimpleScore}.
 * <p/>
 * This class is immutable.
 * @see SimpleScore
 * @author Geoffrey De Smet
 */
public final class DefaultSimpleScore extends AbstractScore<SimpleScore>
        implements SimpleScore {

    public static DefaultSimpleScore parseScore(String scoreString) {
        return valueOf(Integer.parseInt(scoreString));
    }

    public static DefaultSimpleScore valueOf(int score) {
        return new DefaultSimpleScore(score);
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

    public SimpleScore add(SimpleScore augend) {
        return new DefaultSimpleScore(this.score + augend.getScore());
    }

    public SimpleScore substract(SimpleScore subtrahend) {
        return new DefaultSimpleScore(this.score - subtrahend.getScore());
    }

    public SimpleScore multiply(double multiplicand) {
        return new DefaultSimpleScore((int) Math.round(this.score * multiplicand));
    }

    public SimpleScore divide(double divisor) {
        return new DefaultSimpleScore((int) Math.round(this.score / divisor));
    }

    public boolean equals(Object o) {
        // A direct implementation (instead of EqualsBuilder) to avoid dependencies
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
        // A direct implementation (instead of HashCodeBuilder) to avoid dependencies
        return (17 * 37) + score;
    }

    public int compareTo(SimpleScore other) {
        // A direct implementation (instead of CompareToBuilder) to avoid dependencies
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