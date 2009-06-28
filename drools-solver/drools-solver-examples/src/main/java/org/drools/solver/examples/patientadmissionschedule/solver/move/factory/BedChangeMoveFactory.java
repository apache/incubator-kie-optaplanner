package org.drools.solver.examples.patientadmissionschedule.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.solver.move.PeriodChangeMove;
import org.drools.solver.examples.patientadmissionschedule.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.patientadmissionschedule.domain.Bed;
import org.drools.solver.examples.patientadmissionschedule.domain.AdmissionPart;
import org.drools.solver.examples.patientadmissionschedule.domain.BedDesignation;
import org.drools.solver.examples.patientadmissionschedule.solver.move.BedChangeMove;

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
                if (bed.allowsAdmissionPart(bedDesignation.getAdmissionPart())) {
                    moveList.add(new BedChangeMove(bedDesignation, bed));
                }
            }
        }
        return moveList;
    }

}