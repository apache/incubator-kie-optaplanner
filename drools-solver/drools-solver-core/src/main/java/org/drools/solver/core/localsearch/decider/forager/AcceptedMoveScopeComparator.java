package org.drools.solver.core.localsearch.decider.forager;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.localsearch.decider.deciderscorecomparator.DeciderScoreComparatorFactory;
import org.drools.solver.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class AcceptedMoveScopeComparator implements Comparator<MoveScope> {

    private Comparator<? extends Score> deciderScoreComparator;

    public void setDeciderScoreComparator(Comparator<? extends Score> deciderScoreComparator) {
        this.deciderScoreComparator = deciderScoreComparator;
    }
    
    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public int compare(MoveScope a, MoveScope b) {
        CompareToBuilder compareToBuilder = new CompareToBuilder();
        compareToBuilder.append(a.getScore(), b.getScore(), deciderScoreComparator);
        compareToBuilder.append(a.getAcceptChance(), b.getAcceptChance());
        // moves are not compared
        return compareToBuilder.toComparison();
    }

}
