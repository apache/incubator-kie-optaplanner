package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collection;
import java.util.Collections;

import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;

/**
 * @author Geoffrey De Smet
 */
public class SolutionTabuAccepter extends AbstractTabuAccepter {

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
