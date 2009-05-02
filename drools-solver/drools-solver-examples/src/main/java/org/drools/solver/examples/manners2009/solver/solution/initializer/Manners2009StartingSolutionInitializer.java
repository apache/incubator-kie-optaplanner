package org.drools.solver.examples.manners2009.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.WorkingMemory;
import org.drools.FactHandle;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.DefaultHardAndSoftScore;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraint;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraintType;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.domain.Topic;
import org.drools.solver.examples.itc2007.examination.domain.solver.ExamBefore;
import org.drools.solver.examples.itc2007.examination.domain.solver.ExamCoincidence;
import org.drools.solver.examples.manners2009.domain.Manners2009;
import org.drools.solver.examples.manners2009.domain.Guest;
import org.drools.solver.examples.manners2009.domain.SeatDesignation;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009StartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    @Override
    public boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope) {
        Manners2009 manners2009 = (Manners2009) localSearchSolverScope.getWorkingSolution();
        return manners2009.isInitialized();
    }

    public void initializeSolution(LocalSearchSolverScope localSearchSolverScope) {
        Manners2009 manners2009 = (Manners2009) localSearchSolverScope.getWorkingSolution();
        initializeSeatDesignationList(localSearchSolverScope, manners2009);
    }

    private void initializeSeatDesignationList(LocalSearchSolverScope localSearchSolverScope, Manners2009 manners2009) {
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();
        List<SeatDesignation> seatDesignationList = new ArrayList<SeatDesignation>(manners2009.getGuestList().size());
        for (Guest guest : manners2009.getGuestList()) {
            SeatDesignation seatDesignation = new SeatDesignation();
            seatDesignation.setGuest(guest);
            seatDesignation.setSeat(manners2009.getSeatList().get(guest.getId().intValue())); // TODO FIXME
            seatDesignationList.add(seatDesignation);
            workingMemory.insert(seatDesignation);
        }
        Collections.sort(seatDesignationList);
        manners2009.setSeatDesignationList(seatDesignationList);
    }

}