package org.drools.solver.core.localsearch.decider.forager;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.core.localsearch.decider.MoveScope;

/**
 * @author Geoffrey De Smet
 */
public class AcceptedMoveScopeComparator implements Comparator<MoveScope> {

    public int compare(MoveScope a, MoveScope b) {
        CompareToBuilder compareToBuilder = new CompareToBuilder();
        compareToBuilder.append(a.getScore(), b.getScore());
        compareToBuilder.append(a.getAcceptChance(), b.getAcceptChance());
        // moves are not compared
        return compareToBuilder.toComparison();
    }

}
