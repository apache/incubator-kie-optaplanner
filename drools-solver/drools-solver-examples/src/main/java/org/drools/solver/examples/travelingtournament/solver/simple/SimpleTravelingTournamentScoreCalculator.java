package org.drools.solver.examples.travelingtournament.solver.simple;

import java.util.Iterator;

import org.drools.WorkingMemory;
import org.drools.base.ClassObjectFilter;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.travelingtournament.solver.domain.Hop;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentScoreCalculator { // DELETEME

    public double calculateStepScore(Solution solution, WorkingMemory workingMemory) {
        double score = 0;
        // Hard constraints
//        score -= statefulSession.getQueryResults("multipleMatchesPerTeamPerDay").size();
        score -= workingMemory.getQueryResults("multipleMatchesPerTeamPerDay1").size();
        score -= workingMemory.getQueryResults("multipleMatchesPerTeamPerDay2").size();
        score -= workingMemory.getQueryResults("multipleMatchesPerTeamPerDay3").size();
        score -= workingMemory.getQueryResults("multipleMatchesPerTeamPerDay4").size();
        score -= workingMemory.getQueryResults("fourConsecutiveHomeMatches").size();
        score -= workingMemory.getQueryResults("fourConsecutiveAwayMatches").size();
        score -= workingMemory.getQueryResults("matchRepeater").size();
        score *= 1000000.0;
        // Soft constraints
        for (Iterator<Hop> it = workingMemory.iterateObjects(new ClassObjectFilter(Hop.class)); it.hasNext();) {
            Hop hop = it.next();
            score -= hop.getDistance();
        }
        return score;
    }
    
}
