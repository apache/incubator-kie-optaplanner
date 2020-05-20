package com.example.persistence;

import org.optaplanner.core.api.solver.SolverStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import com.example.domain.Lesson;
import com.example.domain.Room;
import com.example.domain.Timeslot;
import com.example.solver.TimeTableController;

@Component
@RepositoryEventHandler
public class ProblemChangedRepositoryEventListener {

    @Autowired
    private TimeTableController timeTableController;

    // TODO Future work: Give the CRUD operations "right of way", by calling something like this:
    // before: solverManager.freeze(TIME_TABLE_ID);
    // after: reloadProblem(TIME_TABLE_ID, timeTableRepository::findById);

    @HandleBeforeCreate
    @HandleBeforeSave
    @HandleBeforeDelete
    private void timeslotCreateSaveDelete(Timeslot timeslot) {
        assertNotSolving();
    }

    @HandleBeforeCreate
    @HandleBeforeSave
    @HandleBeforeDelete
    private void roomCreateSaveDelete(Room room) {
        assertNotSolving();
    }

    @HandleBeforeCreate
    @HandleBeforeSave
    @HandleBeforeDelete
    private void lessonCreateSaveDelete(Lesson lesson) {
        assertNotSolving();
    }

    public void assertNotSolving() {
        // TODO Race condition: if a timeTableSolverService.solve() call arrives concurrently,
        // the solver might start before the CRUD transaction completes. That's not very harmful, though.
        if (timeTableController.getSolverStatus() != SolverStatus.NOT_SOLVING) {
            throw new IllegalStateException("The solver is solving. Please stop solving first.");
        }
    }

}
