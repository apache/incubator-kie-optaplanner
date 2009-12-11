package org.drools.planner.core.score.comparator;

import java.util.Comparator;

import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.HardAndSoftScore;

/**
 * Compares 2 HardAndSoftScore after flattening: the hard multiplied by a hardWeight gets added to the soft.
* @author Geoffrey De Smet
*/
public class FlatteningHardAndSoftScoreComparator implements Comparator<Score> {

    private int hardWeight;

    public FlatteningHardAndSoftScoreComparator(int hardWeight) {
        this.hardWeight = hardWeight;
    }

    public int compare(Score s1, Score s2) {
        HardAndSoftScore score1 = (HardAndSoftScore) s1;
        HardAndSoftScore score2 = (HardAndSoftScore) s2;
        int score1Side = score1.getHardScore() * hardWeight + score1.getSoftScore();
        int score2Side = score2.getHardScore() * hardWeight + score2.getSoftScore();
        return score1Side < score2Side ? -1 : (score1Side == score2Side ? 0 : 1);
    }

}