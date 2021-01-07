/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.taskassigning.domain.solver.move;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.examples.taskassigning.domain.Employee;
import org.optaplanner.examples.taskassigning.domain.Task;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;

public class TaskListSwapMove extends AbstractMove<TaskAssigningSolution> {

    private final Task leftTask;
    private final Task rightTask;

    public TaskListSwapMove(Task leftTask, Task rightTask) {
        this.leftTask = leftTask;
        this.rightTask = rightTask;
    }

    @Override
    protected AbstractMove<TaskAssigningSolution> createUndoMove(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        return new TaskListSwapMove(rightTask, leftTask);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        // TODO lookup might not be necessary
        Employee leftEmployee = scoreDirector.lookUpWorkingObject(leftTask.getEmployee());
        Employee rightEmployee = scoreDirector.lookUpWorkingObject(rightTask.getEmployee());

        scoreDirector.beforeVariableChanged(leftEmployee, "tasks");
        leftEmployee.getTasks().set(leftTask.getIndex(), rightTask);
        scoreDirector.afterVariableChanged(leftEmployee, "tasks");

        scoreDirector.beforeVariableChanged(rightEmployee, "tasks");
        rightEmployee.getTasks().set(rightTask.getIndex(), leftTask);
        scoreDirector.afterVariableChanged(rightEmployee, "tasks");

        // HACK: mimic variable listeners
        scoreDirector.beforeProblemPropertyChanged(leftTask);
        leftTask.setEmployee(rightEmployee);
        scoreDirector.afterProblemPropertyChanged(leftTask);

        scoreDirector.beforeProblemPropertyChanged(rightTask);
        rightTask.setEmployee(leftEmployee);
        scoreDirector.afterProblemPropertyChanged(rightTask);

        int leftTaskIndex = leftTask.getIndex();
        scoreDirector.beforeProblemPropertyChanged(leftTask);
        leftTask.setIndex(rightTask.getIndex());
        scoreDirector.afterProblemPropertyChanged(leftTask);

        scoreDirector.beforeProblemPropertyChanged(rightTask);
        rightTask.setIndex(leftTaskIndex);
        scoreDirector.afterProblemPropertyChanged(rightTask);

        scoreDirector.triggerVariableListeners();
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        return scoreDirector.lookUpWorkingObject(leftTask).getEmployee() != null
                && scoreDirector.lookUpWorkingObject(rightTask).getEmployee() != null;
    }

    @Override
    public String toString() {
        return leftTask.getEmployee() + "[" + leftTask.getIndex() + "]:" + leftTask
                + "<->" + rightTask.getEmployee() + "[" + rightTask.getIndex() + "]:" + rightTask;
    }
}
