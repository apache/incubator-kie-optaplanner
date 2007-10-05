package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.drools.solver.core.localsearch.decider.accepter.AbstractAccepter;
import org.drools.solver.core.move.Move;

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
    public void solvingStarted() {
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

    public double calculateAcceptChance(Move move, double score) {
        List<? extends Object> tabuList = findTabu(move);
        int maximumTabuStepIndex = -1;
        for (Object tabu : tabuList) {
            Integer tabuStepIndexInteger = tabuToStepIndexMap.get(tabu);
            if (tabuStepIndexInteger != null) {
                maximumTabuStepIndex = Math.max(tabuStepIndexInteger, maximumTabuStepIndex);
            }
        }
        if (maximumTabuStepIndex < 0) {
            return 1.0;
        }
        int tabuStepCount = localSearchSolver.getStepIndex() - maximumTabuStepIndex - 1;
        if (tabuStepCount < completeTabuSize) {
            logger.debug("    Proposed move ({}) is complete tabu.", move);
            return 0.0;
        }
        double acceptChance = calculatePartialTabuAcceptChance(tabuStepCount - completeTabuSize);
        logger.debug("    Proposed move ({}) is partially tabu with accept chance ({}).", move, acceptChance);
        return acceptChance;
    }

    protected double calculatePartialTabuAcceptChance(int partialTabuTime) {
        // The + 1's are because acceptChance should not be 0.0 or 1.0
        // when (partialTabuTime == 0) or (partialTabuTime + 1 == partialTabuSize)
        return ((double) (partialTabuTime + 1)) / ((double) (partialTabuSize + 1));
    }

    @Override
    public void stepDecided(Move step) {
        if (step != null) { // TODO fixme by better use of lifecycle method
            List<? extends Object> tabuList = findNewTabu(step);
            for (Object tabu : tabuList) {
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
                tabuToStepIndexMap.put(tabu, localSearchSolver.getStepIndex());
                tabuSequenceList.add(tabu);
            }
        }
    }

    protected abstract List<? extends Object> findTabu(Move move);

    protected List<? extends Object> findNewTabu(Move step) {
        return findTabu(step);
    }

}
