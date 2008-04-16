package org.drools.solver.core.localsearch.decider.forager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public abstract class AcceptedListBasedForager extends AbstractForager {

    protected AcceptedMoveScopeComparator acceptionComparator = new AcceptedMoveScopeComparator();
    protected List<MoveScope> acceptedList;
    protected boolean listSorted;
    protected double maxScore;
    protected double acceptChanceMaxScoreTotal;

    public void setAcceptionComparator(AcceptedMoveScopeComparator acceptionComparator) {
        this.acceptionComparator = acceptionComparator;
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        acceptedList = new ArrayList<MoveScope>(); // TODO use size of moveList in decider
        listSorted = false;
        maxScore = Double.NEGATIVE_INFINITY;
        acceptChanceMaxScoreTotal = 0.0;
    }

    protected void addMoveScopeToAcceptedList(MoveScope moveScope) {
        acceptedList.add(moveScope);
        listSorted = false;
        if (moveScope.getScore() > maxScore) {
            acceptChanceMaxScoreTotal = moveScope.getAcceptChance();
            maxScore = moveScope.getScore();
        } else if (moveScope.getScore() == maxScore) {
            acceptChanceMaxScoreTotal += moveScope.getAcceptChance();
        }
    }

    @Override
    public boolean isQuitEarly() {
        return false;
    }

    protected MoveScope pickMaxScoreMoveScopeFromAcceptedList(StepScope stepScope) {
        if (acceptedList.isEmpty())
        {
            return null;
        }
        sortAcceptedList();
        MoveScope pickedMoveScope = null;
        double randomChance = stepScope.getWorkingRandom().nextDouble();
        double acceptMark = acceptChanceMaxScoreTotal * randomChance;
        for (ListIterator<MoveScope> it = acceptedList.listIterator(acceptedList.size()); it.hasPrevious();) {
            MoveScope moveScope = it.previous();
            acceptMark -= moveScope.getAcceptChance();
            // TODO That underflow warn is nonsence. randomChance can be 0.0 and the last acceptMark can end up 0.0
            // TODO so < is nonsence
            if (acceptMark < 0.0) {
                pickedMoveScope = moveScope;
                break;
            }
        }
        if (pickedMoveScope == null) {
            // TODO This isn't really underflow when an forager accepts only moves with acceptChance 0.0
            logger.warn("Underflow occured with acceptChanceMaxScoreTotal ({}) " +
                    "and randomChance ({}).", acceptChanceMaxScoreTotal, randomChance);
            // Deal with it anyway (no fail-fast here)
            pickedMoveScope = acceptedList.get(acceptedList.size() - 1);
        }
        return pickedMoveScope;
    }

    public int getAcceptedMovesSize() {
        return acceptedList.size();
    }

    public List<Move> getTopList(int topSize) {
        sortAcceptedList();
        int size = acceptedList.size();
        List<Move> topList = new ArrayList<Move>(Math.min(topSize, size));
        List<MoveScope> subAcceptedList = acceptedList.subList(Math.max(0, size - topSize), size);
        for (MoveScope moveScope : subAcceptedList) {
            topList.add(moveScope.getMove());
        }
        return topList;
    }

    protected void sortAcceptedList() {
        if (!listSorted) {
            Collections.sort(acceptedList, acceptionComparator);
            listSorted = true;
        }
    }

}
