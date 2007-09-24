package org.drools.solver.core.localsearch.decider.forager;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Geoffrey De Smet
 */
public class AcceptionComparator implements Comparator<Acception> {

    public int compare(Acception a, Acception b) {
        CompareToBuilder compareToBuilder = new CompareToBuilder();
        compareToBuilder.append(a.getScore(), b.getScore());
        compareToBuilder.append(a.getAcceptChance(), b.getAcceptChance());
        // moves are not compared
        return compareToBuilder.toComparison();
    }

}
