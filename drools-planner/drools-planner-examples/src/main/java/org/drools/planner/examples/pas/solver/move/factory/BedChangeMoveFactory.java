package org.drools.planner.examples.pas.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.pas.domain.Bed;
import org.drools.planner.examples.pas.domain.BedDesignation;
import org.drools.planner.examples.pas.domain.PatientAdmissionSchedule;
import org.drools.planner.examples.pas.solver.move.BedChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class BedChangeMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        PatientAdmissionSchedule patientAdmissionSchedule = (PatientAdmissionSchedule) solution;
        List<Bed> bedList = patientAdmissionSchedule.getBedList();
        List<Move> moveList = new ArrayList<Move>();
        for (BedDesignation bedDesignation : patientAdmissionSchedule.getBedDesignationList()) {
            for (Bed bed : bedList) {
                moveList.add(new BedChangeMove(bedDesignation, bed));
            }
        }
        return moveList;
    }

}