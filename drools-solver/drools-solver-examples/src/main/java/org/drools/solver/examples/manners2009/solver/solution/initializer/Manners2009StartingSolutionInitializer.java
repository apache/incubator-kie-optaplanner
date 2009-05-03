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
import org.drools.solver.core.score.DefaultSimpleScore;
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
import org.drools.solver.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Course;
import org.drools.solver.examples.manners2009.domain.Manners2009;
import org.drools.solver.examples.manners2009.domain.Guest;
import org.drools.solver.examples.manners2009.domain.SeatDesignation;
import org.drools.solver.examples.manners2009.domain.Seat;

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
        List<SeatDesignation> seatDesignationList = createSeatDesignationList(manners2009);
        // Assign one guest at a time
        List<Seat> undesignatedSeatList = manners2009.getSeatList();
        for (SeatDesignation seatDesignation : seatDesignationList) {
            Score bestScore = DefaultSimpleScore.valueOf(Integer.MIN_VALUE);
            Seat bestSeat = null;

            FactHandle seatDesignationHandle = null;
            // Try every seat for that guest
            // TODO by reordening the seats so index 0 has a different table then index 1 and so on,
            // this will probably be faster because perfectMatch will be true sooner
            for (Seat seat : undesignatedSeatList) {
                if (seatDesignation.getGuest().getGender() == seat.getRequiredGender()) {
                    if (seatDesignationHandle == null) {
                        seatDesignation.setSeat(seat);
                        seatDesignationHandle = workingMemory.insert(seatDesignation);
                    } else {
                        workingMemory.modifyRetract(seatDesignationHandle);
                        seatDesignation.setSeat(seat);
                        workingMemory.modifyInsert(seatDesignationHandle, seatDesignation);
                    }
                    Score score = localSearchSolverScope.calculateScoreFromWorkingMemory();
                    if (score.compareTo(bestScore) > 0) {
                        bestScore = score;
                        bestSeat = seat;
                    }
                }
            }
            if (bestSeat == null) {
                throw new IllegalStateException("The bestSeat (" + bestSeat + ") cannot be null.");
            }
            workingMemory.modifyRetract(seatDesignationHandle);
            seatDesignation.setSeat(bestSeat);
            workingMemory.modifyInsert(seatDesignationHandle, seatDesignation);
            undesignatedSeatList.remove(bestSeat);
        }
        Collections.sort(seatDesignationList); // For the GUI's combobox list mainly, not really needed
        manners2009.setSeatDesignationList(seatDesignationList);
    }

    private List<SeatDesignation> createSeatDesignationList(Manners2009 manners2009) {
        List<SeatDesignation> seatDesignationList = new ArrayList<SeatDesignation>(manners2009.getGuestList().size());
        for (Guest guest : manners2009.getGuestList()) {
            SeatDesignation seatDesignation = new SeatDesignation();
            seatDesignation.setId(guest.getId());
            seatDesignation.setGuest(guest);
            seatDesignationList.add(seatDesignation);
        }
        return seatDesignationList;
    }

}