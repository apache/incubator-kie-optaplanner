package org.drools.planner.core.score;

/**
 * Default implementation of {@link HardAndSoftScore}.
 * <p/>
 * This class is immutable.
 * @see HardAndSoftScore
 * @author Geoffrey De Smet
 */
public final class DefaultHardAndSoftScore extends AbstractScore<HardAndSoftScore>
        implements HardAndSoftScore {

    private static final String HARD_LABEL = "hard";
    private static final String SOFT_LABEL = "soft";

    public static DefaultHardAndSoftScore parseScore(String scoreString) {
        String[] scoreTokens = scoreString.split(HARD_LABEL + "\\/");
        if (scoreTokens.length != 2 || !scoreTokens[1].endsWith(SOFT_LABEL)) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") doesn't follow the 999hard/999soft pattern.");
        }
        int hardScore = Integer.parseInt(scoreTokens[0]);
        int softScore = Integer.parseInt(scoreTokens[1].substring(0, scoreTokens[1].length() - SOFT_LABEL.length()));
        return valueOf(hardScore, softScore);
    }

    public static DefaultHardAndSoftScore valueOf(int hardScore) {
        return new DefaultHardAndSoftScore(hardScore);
    }

    public static DefaultHardAndSoftScore valueOf(int hardScore, int softScore) {
        return new DefaultHardAndSoftScore(hardScore, softScore);
    }

    private final int hardScore;
    private final int softScore;

    public DefaultHardAndSoftScore(int hardScore) {
        // Any other softScore is better
        this(hardScore, Integer.MIN_VALUE);
    }

    public DefaultHardAndSoftScore(int hardScore, int softScore) {
        this.hardScore = hardScore;
        this.softScore = softScore;
    }

    public int getHardScore() {
        return hardScore;
    }

    public int getSoftScore() {
        return softScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public HardAndSoftScore add(HardAndSoftScore augend) {
        return new DefaultHardAndSoftScore(this.hardScore + augend.getHardScore(),
                this.softScore + augend.getSoftScore());
    }

    public HardAndSoftScore substract(HardAndSoftScore subtrahend) {
        return new DefaultHardAndSoftScore(this.hardScore - subtrahend.getHardScore(),
                this.softScore - subtrahend.getSoftScore());
    }

    public HardAndSoftScore multiply(double multiplicand) {
        return new DefaultHardAndSoftScore((int) Math.round(this.hardScore * multiplicand),
                (int) Math.round(this.softScore * multiplicand));
    }

    public HardAndSoftScore divide(double divisor) {
        return new DefaultHardAndSoftScore((int) Math.round(this.hardScore / divisor),
                (int) Math.round(this.softScore / divisor));
    }

    public boolean equals(Object o) {
        // A direct implementation (instead of EqualsBuilder) to avoid dependencies
        if (this == o) {
            return true;
        } else if (o instanceof HardAndSoftScore) {
            HardAndSoftScore other = (HardAndSoftScore) o;
            return hardScore == other.getHardScore()
                    && softScore == other.getSoftScore();
        } else {
            return false;
        }
    }

    public int hashCode() {
        // A direct implementation (instead of HashCodeBuilder) to avoid dependencies
        return (((17 * 37) + hardScore)) * 37 + softScore;
    }

    public int compareTo(HardAndSoftScore other) {
        // A direct implementation (instead of CompareToBuilder) to avoid dependencies
        if (hardScore != other.getHardScore()) {
            if (hardScore < other.getHardScore()) {
                return -1;
            } else {
                return 1;
            }
        } else {
           if (softScore < other.getSoftScore()) {
               return -1;
           } else if (softScore > other.getSoftScore()) {
               return 1;
           } else {
               return 0;
           }
        }
    }

    public String toString() {
        return hardScore + HARD_LABEL + "/" + softScore + SOFT_LABEL;
    }

}
