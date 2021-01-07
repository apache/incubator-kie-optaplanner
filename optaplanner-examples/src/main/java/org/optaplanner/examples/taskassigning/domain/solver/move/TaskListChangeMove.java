/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

public class TaskListChangeMove extends AbstractMove<TaskAssigningSolution> {

    private final Task task;
    private final Employee toEmployee;
    private final int toIndex;

    public TaskListChangeMove(Task task, Employee toEmployee, int toIndex) {
        this.task = task;
        this.toEmployee = toEmployee;
        this.toIndex = toIndex;
    }

    @Override
    protected AbstractMove<TaskAssigningSolution> createUndoMove(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        return new TaskListChangeMove(task, task.getEmployee(), task.getIndex());
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        Employee fromEmployee = task.getEmployee();
        scoreDirector.beforeVariableChanged(fromEmployee, "tasks");
        fromEmployee.getTasks().remove(task);
        scoreDirector.afterVariableChanged(fromEmployee, "tasks");

        scoreDirector.beforeVariableChanged(toEmployee, "tasks");
        toEmployee.getTasks().add(toIndex, task);
        scoreDirector.afterVariableChanged(toEmployee, "tasks");
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        return true;
    }

    @Override
    public String toString() {
        return task + "{" + task.getEmployee() + "[" + task.getIndex() + "]->" + toEmployee + "[" + toIndex + "]}";
    }
}
