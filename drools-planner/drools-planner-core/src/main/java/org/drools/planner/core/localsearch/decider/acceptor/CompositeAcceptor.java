package org.drools.planner.core.localsearch.decider.acceptor;

import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;

/**
 * Combines several acceptors into one.
 * Multiplies the accept chance of its acceptors.
 * For example: combine solution and property tabu to do tabu on both.
 * @author Geoffrey De Smet
 */
public class CompositeAcceptor extends AbstractAcceptor {

    protected List<Acceptor> acceptorList;

    public void setAcceptorList(List<Acceptor> acceptorList) {
        this.acceptorList = acceptorList;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        for (Acceptor acceptor : acceptorList) {
            acceptor.solvingStarted(localSearchSolverScope);
        }
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        for (Acceptor acceptor : acceptorList) {
            acceptor.beforeDeciding(stepScope);
        }
    }

    public double calculateAcceptChance(MoveScope moveScope) {
        double acceptChance = 1.0;
        for (Acceptor acceptor : acceptorList) {
            acceptChance *= acceptor.calculateAcceptChance(moveScope);
        }
        return acceptChance;
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        for (Acceptor acceptor : acceptorList) {
            acceptor.stepDecided(stepScope);
        }
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        for (Acceptor acceptor : acceptorList) {
            acceptor.stepTaken(stepScope);
        }
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        for (Acceptor acceptor : acceptorList) {
            acceptor.solvingEnded(localSearchSolverScope);
        }
    }

}
