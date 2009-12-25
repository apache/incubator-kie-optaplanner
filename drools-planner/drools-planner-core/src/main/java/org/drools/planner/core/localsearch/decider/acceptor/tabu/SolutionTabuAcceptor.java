package org.drools.planner.core.localsearch.decider.acceptor.tabu;

import java.util.Collection;
import java.util.Collections;

import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;

/**
 * @author Geoffrey De Smet
 */
public class SolutionTabuAcceptor extends AbstractTabuAcceptor {

    public SolutionTabuAcceptor() {
        // Disable aspiration by default because it's useless on solution tabu
        aspirationEnabled = false;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(MoveScope moveScope) {
        return Collections.singletonList(moveScope.getWorkingSolution());
    }

    @Override
    protected Collection<? extends Object> findNewTabu(StepScope stepScope) {
        // TODO this should be better done in stepTaken
        return Collections.singletonList(stepScope.createOrGetClonedSolution());
    }
    
}
