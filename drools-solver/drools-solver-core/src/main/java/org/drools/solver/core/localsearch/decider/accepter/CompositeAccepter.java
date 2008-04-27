package org.drools.solver.core.localsearch.decider.accepter;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;

/**
 * Combines several accepters into one.
 * Multiplies the accept chance of its accepters.
 * For example: combine solution and property tabu to do tabu on both.
 * @author Geoffrey De Smet
 */
public class CompositeAccepter extends AbstractAccepter {

    protected List<Accepter> accepterList;

    public void setAccepterList(List<Accepter> accepterList) {
        this.accepterList = accepterList;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        for (Accepter accepter : accepterList) {
            accepter.solvingStarted(localSearchSolverScope);
        }
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        for (Accepter accepter : accepterList) {
            accepter.beforeDeciding(stepScope);
        }
    }

    @Override
    public double calculateAcceptChance(MoveScope moveScope) {
        double acceptChance = 1.0;
        for (Accepter accepter : accepterList) {
            acceptChance *= accepter.calculateAcceptChance(moveScope);
        }
        return acceptChance;
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        for (Accepter accepter : accepterList) {
            accepter.stepDecided(stepScope);
        }
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        for (Accepter accepter : accepterList) {
            accepter.stepTaken(stepScope);
        }
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        for (Accepter accepter : accepterList) {
            accepter.solvingEnded(localSearchSolverScope);
        }
    }

}
