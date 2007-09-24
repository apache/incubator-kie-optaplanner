package org.drools.solver.examples.travelingtournament.solver.smart;

import java.util.Iterator;

import org.drools.WorkingMemory;
import org.drools.base.ClassObjectFilter;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.travelingtournament.solver.domain.Hop;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentScoreCalculator { // DELETEME

//    private int hardConstraintsWeight      = 50000;
    private int hardConstraintsWeight    = 1000000;
    private int maxHardConstraintsWeight = 1000000;
    private int minHardConstraintsWeight    = 300;

    public double calculateStepScore(Solution solution, WorkingMemory workingMemory) {
        int hardConstraints = calculateHardConstraintsScore(workingMemory);
//        // adjust it for decision calculation
//        if (hardConstraints == 0) {
//            hardConstraintsWeight /= 1.2;
//            hardConstraintsWeight = Math.max(hardConstraintsWeight, minHardConstraintsWeight);
//        } else {
//            hardConstraintsWeight *= 1.2;
//            hardConstraintsWeight = Math.min(hardConstraintsWeight, maxHardConstraintsWeight);
//        }
        return (hardConstraints * maxHardConstraintsWeight)
                + calculateSoftConstraintsScore(workingMemory);
    }

    public double calculateDecisionScore(Solution solution, WorkingMemory workingMemory) {
        return (calculateHardConstraintsScore(workingMemory) * hardConstraintsWeight)
                + calculateSoftConstraintsScore(workingMemory);
    }

    private double calculateSoftConstraintsScore(WorkingMemory workingMemory) {
        int score = 0;
        for (Iterator<Hop> it = workingMemory.iterateObjects(new ClassObjectFilter(Hop.class)); it.hasNext();) {
            Hop hop = it.next();
            score -= hop.getDistance();
        }
        return score;
    }

    private int calculateHardConstraintsScore(WorkingMemory workingMemory) {
        int score = 0;
        score -= workingMemory.getQueryResults("fourConsecutiveHomeMatches").size();
        score -= workingMemory.getQueryResults("fourConsecutiveAwayMatches").size();
        score -= workingMemory.getQueryResults("matchRepeater").size();
        return score;
    }

}
