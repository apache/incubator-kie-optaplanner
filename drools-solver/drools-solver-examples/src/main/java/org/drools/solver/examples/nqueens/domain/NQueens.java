package org.drools.solver.examples.nqueens.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class NQueens extends AbstractPersistable implements Solution {

    private List<Queen> queenList;

    public List<Queen> getQueenList() {
        return queenList;
    }

    public void setQueenList(List<Queen> queenList) {
        this.queenList = queenList;
    }

    public int getN() {
        return queenList.size();
    }

    /**
     * @return a list of every possible n
     */
    public List<Integer> createNList() {
        int n = getN();
        List<Integer> nList = new ArrayList<Integer>(n);
        for (int i = 0; i < n; i++) {
            nList.add(i);
        }
        return nList;
    }

    
    public Collection<? extends Object> getFacts() {
        return queenList;
    }

    /**
     * Clone will only deep copy the queens
     */
    public NQueens cloneSolution() {
        NQueens clone = new NQueens();
        List<Queen> clonedQueenList = new ArrayList<Queen>(queenList.size());
        for (Queen queen : queenList) {
            clonedQueenList.add(queen.clone());
        }
        clone.queenList = clonedQueenList;
        return clone;
    }

}
