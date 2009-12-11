package org.drools.planner.examples.pas.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.pas.domain.BedDesignation;
import org.drools.planner.examples.pas.domain.PatientAdmissionSchedule;
import org.drools.planner.examples.pas.solver.move.BedDesignationSwitchMove;

/**
 * @author Geoffrey De Smet
 */
public class BedDesignationSwitchMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        PatientAdmissionSchedule patientAdmissionSchedule = (PatientAdmissionSchedule) solution;
        List<BedDesignation> bedDesignationList = patientAdmissionSchedule.getBedDesignationList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<BedDesignation> leftIt = bedDesignationList.listIterator(); leftIt.hasNext();) {
            BedDesignation leftBedDesignation = leftIt.next();
            for (ListIterator<BedDesignation> rightIt = bedDesignationList.listIterator(leftIt.nextIndex());
                    rightIt.hasNext();) {
                BedDesignation rightBedDesignation = rightIt.next();
                if (leftBedDesignation.getAdmissionPart().calculateSameNightCount(rightBedDesignation.getAdmissionPart()) > 0) {
                    moveList.add(new BedDesignationSwitchMove(leftBedDesignation, rightBedDesignation));
                }
            }
        }
        return moveList;
    }

}