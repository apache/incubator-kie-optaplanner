package org.drools.solver.core.localsearch.decider.accepter;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class CompositeAccepter extends AbstractAccepter {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected List<Accepter> accepterList;

    public void setAccepterList(List<Accepter> accepterList) {
        this.accepterList = accepterList;
    }

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        super.setLocalSearchSolver(localSearchSolver);
        for (Accepter accepter : accepterList) {
            accepter.setLocalSearchSolver(localSearchSolver);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted() {
        for (Accepter accepter : accepterList) {
            accepter.solvingStarted();
        }
    }

    public void beforeDeciding() {
        for (Accepter accepter : accepterList) {
            accepter.beforeDeciding();
        }
    }

    public double calculateAcceptChance(Move move, double score) {
        double acceptChance = 1.0;
        for (Accepter accepter : accepterList) {
            acceptChance *= accepter.calculateAcceptChance(move, score);
        }
        return acceptChance;
    }

    public void stepDecided(Move step) {
        for (Accepter accepter : accepterList) {
            accepter.stepDecided(step);
        }
    }

    public void stepTaken() {
        for (Accepter accepter : accepterList) {
            accepter.stepTaken();
        }
    }

    public void solvingEnded() {
        for (Accepter accepter : accepterList) {
            accepter.solvingEnded();
        }
    }

}
