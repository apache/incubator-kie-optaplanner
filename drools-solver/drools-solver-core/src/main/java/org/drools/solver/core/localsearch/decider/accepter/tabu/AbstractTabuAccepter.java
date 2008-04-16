package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.localsearch.decider.accepter.AbstractAccepter;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractTabuAccepter extends AbstractAccepter {

    protected int completeTabuSize = -1;
    protected int partialTabuSize = 0;

    protected Map<Object, Integer> tabuToStepIndexMap;
    protected List<Object> tabuSequenceList;

    public int getCompleteTabuSize() {
        return completeTabuSize;
    }

    public void setCompleteTabuSize(int completeTabuSize) {
        this.completeTabuSize = completeTabuSize;
    }

    public void setPartialTabuSize(int partialTabuSize) {
        this.partialTabuSize = partialTabuSize;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        if (completeTabuSize < 0) {
            throw new IllegalArgumentException("Property completeTabuSize (" + completeTabuSize
                    + ") is negative.");
        }
        if (partialTabuSize < 0) {
            throw new IllegalArgumentException("Property partialTabuSize (" + partialTabuSize
                    + ") is negative.");
        }
        if (completeTabuSize + partialTabuSize == 0) {
            throw new IllegalArgumentException("The sum of completeTabuSize and partialTabuSize should be at least 1.");
        }
        tabuToStepIndexMap = new HashMap<Object, Integer>(completeTabuSize + partialTabuSize);
        tabuSequenceList = new LinkedList<Object>();
    }

    @Override
    public double calculateAcceptChance(MoveScope moveScope) {
        // TODO aspiration

        Collection<? extends Object> tabus = findTabu(moveScope);
        int maximumTabuStepIndex = -1;
        for (Object tabu : tabus) {
            Integer tabuStepIndexInteger = tabuToStepIndexMap.get(tabu);
            if (tabuStepIndexInteger != null) {
                maximumTabuStepIndex = Math.max(tabuStepIndexInteger, maximumTabuStepIndex);
            }
        }
        if (maximumTabuStepIndex < 0) {
            // The move isn't tabu at all
            return 1.0;
        }
        int tabuStepCount = moveScope.getStepScope().getStepIndex() - maximumTabuStepIndex - 1;
        if (tabuStepCount < completeTabuSize) {
            logger.debug("    Proposed move ({}) is complete tabu.", moveScope.getMove());
            return 0.0;
        }
        double acceptChance = calculatePartialTabuAcceptChance(tabuStepCount - completeTabuSize);
        logger.debug("    Proposed move ({}) is partially tabu with accept chance ({}).",
                moveScope.getMove(), acceptChance);
        return acceptChance;
    }

    protected double calculatePartialTabuAcceptChance(int partialTabuTime) {
        // The + 1's are because acceptChance should not be 0.0 or 1.0
        // when (partialTabuTime == 0) or (partialTabuTime + 1 == partialTabuSize)
        return ((double) (partialTabuTime + 1)) / ((double) (partialTabuSize + 1));
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        if (stepScope.getStep() != null) { // TODO fixme by better use of lifecycle method
            Collection<? extends Object> tabus = findNewTabu(stepScope);
            for (Object tabu : tabus) {
                // required to push tabu to the end of the line
                if (tabuToStepIndexMap.containsKey(tabu)) {
                    tabuToStepIndexMap.remove(tabu);
                    tabuSequenceList.remove(tabu);
                }
                int maximumTabuListSize = completeTabuSize + partialTabuSize; // is at least 1
                while (tabuSequenceList.size() >= maximumTabuListSize) {
                    Iterator<Object> it = tabuSequenceList.iterator();
                    Object removeTabu = it.next();
                    it.remove();
                    tabuToStepIndexMap.remove(removeTabu);
                }
                tabuToStepIndexMap.put(tabu, stepScope.getStepIndex());
                tabuSequenceList.add(tabu);
            }
        }
    }

    protected abstract Collection<? extends Object> findTabu(MoveScope moveScope);

    protected abstract Collection<? extends Object> findNewTabu(StepScope stepScope);

}
