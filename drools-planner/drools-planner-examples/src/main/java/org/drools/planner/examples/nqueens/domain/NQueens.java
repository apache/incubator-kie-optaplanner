package org.drools.planner.examples.nqueens.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.SimpleScore;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class NQueens extends AbstractPersistable implements Solution {

    private List<Queen> queenList;

    private SimpleScore score;

    public List<Queen> getQueenList() {
        return queenList;
    }

    public void setQueenList(List<Queen> queenList) {
        this.queenList = queenList;
    }

    public SimpleScore getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = (SimpleScore) score;
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
     * Clone will only deep copy the queenList
     */
    public NQueens cloneSolution() {
        NQueens clone = new NQueens();
        clone.id = id;
        List<Queen> clonedQueenList = new ArrayList<Queen>(queenList.size());
        for (Queen queen : queenList) {
            clonedQueenList.add(queen.clone());
        }
        clone.queenList = clonedQueenList;
        clone.score = score;
        return clone;
    }

}
