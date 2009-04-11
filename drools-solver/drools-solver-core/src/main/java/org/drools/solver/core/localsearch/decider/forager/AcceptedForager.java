package org.drools.solver.core.localsearch.decider.forager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.move.Move;

/**
 * An AcceptedForager is a Forager which forages accepted moves and ignores unaccepted moves.
 * @See Forager
 * @See Accepter
 * @author Geoffrey De Smet
 */
public class AcceptedForager extends AbstractForager {

    // final to allow better hotspot optimization. TODO prove that it indeed makes a difference
    protected final PickEarlyByScore pickEarlyByScore;
    protected final boolean pickEarlyRandomly;
    protected final AcceptedMoveScopeComparator acceptionComparator;

    protected List<MoveScope> acceptedList;
    protected boolean listSorted;
    protected double maxScore;
    protected double acceptChanceMaxScoreTotal;

    protected MoveScope earlyPickedMoveScope = null;

    public AcceptedForager() {
        this(PickEarlyByScore.NONE, false);
    }

    public AcceptedForager(PickEarlyByScore pickEarlyByScore, boolean pickEarlyRandomly) {
        this(pickEarlyByScore, pickEarlyRandomly, new AcceptedMoveScopeComparator());
    }

    public AcceptedForager(PickEarlyByScore pickEarlyByScore, boolean pickEarlyRandomly,
            AcceptedMoveScopeComparator acceptionComparator) {
        this.pickEarlyByScore = pickEarlyByScore;
        this.pickEarlyRandomly = pickEarlyRandomly;
        this.acceptionComparator = acceptionComparator;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void beforeDeciding(StepScope stepScope) {
        acceptedList = new ArrayList<MoveScope>(); // TODO use size of moveList in decider
        listSorted = false;
        maxScore = Double.NEGATIVE_INFINITY;
        acceptChanceMaxScoreTotal = 0.0;
        earlyPickedMoveScope = null;
    }

    public void addMove(MoveScope moveScope) {
        if (moveScope.getAcceptChance() > 0.0) {
            checkPickEarly(moveScope);
            addMoveScopeToAcceptedList(moveScope);
        }
    }

    protected void checkPickEarly(MoveScope moveScope) {
        switch (pickEarlyByScore) {
            case NONE:
                break;
            case FIRST_BEST_SCORE_IMPROVING:
                if (moveScope.getScore() > moveScope.getStepScope().getLocalSearchSolverScope().getBestScore()) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            case FIRST_LAST_STEP_SCORE_IMPROVING:
                if (moveScope.getScore() > moveScope.getStepScope().getLocalSearchSolverScope()
                        .getLastCompletedStepScope().getScore()) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            default:
                throw new IllegalStateException("pickEarlyByScore (" + pickEarlyByScore + ") not implemented");
        }
        if (pickEarlyRandomly) {
            if (moveScope.getAcceptChance() >= 1.0) {
                earlyPickedMoveScope = moveScope;
            } else {
                double randomChance = moveScope.getWorkingRandom().nextDouble();
                if (randomChance <= moveScope.getAcceptChance()) {
                    earlyPickedMoveScope = moveScope;
                }
            }
        }
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

    public boolean isQuitEarly() {
        return earlyPickedMoveScope != null;
    }

    public MoveScope pickMove(StepScope stepScope) {
        if (earlyPickedMoveScope != null) {
            return earlyPickedMoveScope;
        } else {
            return pickMaxScoreMoveScopeFromAcceptedList(stepScope);
        }
    }

    protected MoveScope pickMaxScoreMoveScopeFromAcceptedList(StepScope stepScope) {
        if (acceptedList.isEmpty()) {
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
            // TODO so < is nonsence (do a testcase though)
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
