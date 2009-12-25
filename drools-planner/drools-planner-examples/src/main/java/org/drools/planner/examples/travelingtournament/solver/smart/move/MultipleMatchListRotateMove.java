package org.drools.planner.examples.travelingtournament.solver.smart.move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.FactHandle;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.travelingtournament.domain.Day;
import org.drools.planner.examples.travelingtournament.domain.Match;

/**
 * @author Geoffrey De Smet
 */
public class MultipleMatchListRotateMove implements Move, TabuPropertyEnabled {

    private List<Match> firstMatchList;
    private List<Match> secondMatchList;

    public MultipleMatchListRotateMove(List<Match> firstMatchList, List<Match> secondMatchList) {
        this.firstMatchList = firstMatchList;
        this.secondMatchList = secondMatchList;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return true;
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        List<Match> inverseFirstMatchList = new ArrayList<Match>(firstMatchList);
        Collections.reverse(inverseFirstMatchList);
        List<Match> inverseSecondMatchList = new ArrayList<Match>(secondMatchList);
        Collections.reverse(inverseSecondMatchList);
        return new MultipleMatchListRotateMove(inverseFirstMatchList, inverseSecondMatchList);
    }

    public void doMove(WorkingMemory workingMemory) {
        rotateList(firstMatchList, workingMemory);
        if (!secondMatchList.isEmpty()) { // TODO create SingleMatchListRotateMove
            rotateList(secondMatchList, workingMemory);
        }
    }

    private void rotateList(List<Match> matchList, WorkingMemory workingMemory) {
        Iterator<Match> it = matchList.iterator();
        Match firstMatch = it.next();
        Match secondMatch = null;
        Day startDay = firstMatch.getDay();
        while (it.hasNext()) {
            secondMatch = it.next();
            FactHandle firstMatchHandle = workingMemory.getFactHandle(firstMatch);
            workingMemory.modifyRetract(firstMatchHandle);
            firstMatch.setDay(secondMatch.getDay());
            workingMemory.modifyInsert(firstMatchHandle, firstMatch);
            firstMatch = secondMatch;
        }
        FactHandle secondMatchHandle = workingMemory.getFactHandle(firstMatch);
        workingMemory.modifyRetract(secondMatchHandle);
        secondMatch.setDay(startDay);
        workingMemory.modifyInsert(secondMatchHandle, secondMatch);
    }

    public Collection<? extends Object> getTabuProperties() {
        List<Match> tabuPropertyList = new ArrayList<Match>(firstMatchList.size() + secondMatchList.size());
        tabuPropertyList.addAll(firstMatchList);
        tabuPropertyList.addAll(secondMatchList);
        return tabuPropertyList;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MultipleMatchListRotateMove) {
            MultipleMatchListRotateMove other = (MultipleMatchListRotateMove) o;
            return new EqualsBuilder()
                    .append(firstMatchList, other.firstMatchList)
                    .append(secondMatchList, other.secondMatchList)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(firstMatchList)
                .append(secondMatchList)
                .toHashCode();
    }

    public String toString() {
        return firstMatchList + " & " + secondMatchList;
    }

}
