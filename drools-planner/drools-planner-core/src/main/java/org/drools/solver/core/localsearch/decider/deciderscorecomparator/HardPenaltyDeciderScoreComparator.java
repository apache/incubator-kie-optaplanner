package org.drools.solver.core.localsearch.decider.deciderscorecomparator;

import java.util.Comparator;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.HardAndSoftScore;

/**
 * Compares 2 HardAndSoftScore based on the calculation the hard multiplied by a weight to the soft.
* @author Geoffrey De Smet
*/
public class HardPenaltyDeciderScoreComparator implements Comparator<Score> {

    private int hardWeight;

    public HardPenaltyDeciderScoreComparator(int hardWeight) {
        this.hardWeight = hardWeight;
    }

    public int getHardWeight() {
        return hardWeight;
    }

    public int compare(Score s1, Score s2) {
        HardAndSoftScore score1 = (HardAndSoftScore) s1;
        HardAndSoftScore score2 = (HardAndSoftScore) s2;
        int score1Side = score1.getHardScore() * hardWeight + score1.getSoftScore();
        int score2Side = score2.getHardScore() * hardWeight + score2.getSoftScore();
        return score1Side < score2Side ? -1 : (score1Side == score2Side ? 0 : 1);
    }

}