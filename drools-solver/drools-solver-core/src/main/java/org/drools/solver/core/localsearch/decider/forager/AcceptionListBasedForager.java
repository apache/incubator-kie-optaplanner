package org.drools.solver.core.localsearch.decider.forager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public abstract class AcceptionListBasedForager extends AbstractForager {

    protected AcceptionComparator acceptionComparator = new AcceptionComparator();
    protected List<Acception> acceptionList;
    protected boolean listSorted;
    protected double maxScore;
    protected double acceptChanceMaxScoreTotal;

    public void setAcceptionComparator(AcceptionComparator acceptionComparator) {
        this.acceptionComparator = acceptionComparator;
    }

    @Override
    public void beforeDeciding() {
        acceptionList = new ArrayList<Acception>(); // TODO use size of moveList in decider
        listSorted = false;
        maxScore = Double.NEGATIVE_INFINITY;
        acceptChanceMaxScoreTotal = 0.0;
    }

    protected void addMoveToAcceptionList(Move move, double score, double acceptChance) {
        acceptionList.add(new Acception(move, score, acceptChance));
        listSorted = false;
        if (score > maxScore) {
            acceptChanceMaxScoreTotal = acceptChance;
            maxScore = score;
        } else if (score == maxScore) {
            acceptChanceMaxScoreTotal += acceptChance;
        }
    }

    protected Move pickMaxScoreMove() {
        if (acceptionList.isEmpty())
        {
            return null;
        }
        sortAcceptionList();
        Move pickedMove = null;
        double random = localSearchSolver.getRandom().nextDouble();
        double acceptMark = acceptChanceMaxScoreTotal * random;
        for (ListIterator<Acception> it = acceptionList.listIterator(acceptionList.size()); it.hasPrevious();) {
            Acception acception = it.previous();
            acceptMark -= acception.getAcceptChance();
            if (acceptMark < 0.0) {
                pickedMove = acception.getMove();
                break;
            }
        }
        if (pickedMove == null) {
            logger.warn("Underflow occured with acceptChanceMaxScoreTotal ({}) " +
                    "and random ({}).", acceptChanceMaxScoreTotal, random);
            // Deal with it anyway (no fail-fast here)
            pickedMove = acceptionList.get(acceptionList.size() - 1).getMove();
        }
        return pickedMove;
    }

    public int getAcceptedMovesSize() {
        return acceptionList.size();
    }

    public List<Move> getTopList(int topSize) {
        sortAcceptionList();
        int size = acceptionList.size();
        List<Move> topList = new ArrayList<Move>(Math.min(topSize, size));
        List<Acception> subAcceptionList = acceptionList.subList(Math.max(0, size - topSize), size);
        for (Acception acception : subAcceptionList) {
            topList.add(acception.getMove());
        }
        return topList;
    }

    protected void sortAcceptionList() {
        if (!listSorted) {
            Collections.sort(acceptionList, acceptionComparator);
            listSorted = true;
        }
    }
}
