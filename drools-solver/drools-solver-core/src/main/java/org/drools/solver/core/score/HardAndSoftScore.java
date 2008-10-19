package org.drools.solver.core.score;

/**
 * This class is immutable.
 * @author Geoffrey De Smet
 */
public class HardAndSoftScore extends AbstractScore<HardAndSoftScore> {

    private int hardScore;
    private int softScore;

    public HardAndSoftScore(int hardScore) {
        this.hardScore = hardScore;
        // Any other softScore is better
        softScore = Integer.MIN_VALUE;
    }

    public HardAndSoftScore(int hardScore, int softScore) {
        this.hardScore = hardScore;
        this.softScore = softScore;
    }

    public int compareTo(HardAndSoftScore other) {
        if (hardScore != other.hardScore) {
            if (hardScore < other.hardScore) {
                return -1;
            } else {
                return 1;
            }
        } else {
           if (softScore < other.softScore) {
               return -1;
           } else if (softScore > other.softScore) {
               return 1;
           } else {
               return 0;
           }
        }
    }

    public String toString() {
        return hardScore + "hard/" + softScore + "soft";
    }

}
