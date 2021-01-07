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

package org.optaplanner.examples.taskassigning.domain.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
import org.optaplanner.examples.taskassigning.domain.Employee;
import org.optaplanner.examples.taskassigning.domain.Task;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.domain.solver.move.TaskListChangeMove;

public class TaskListChangeMoveFactory implements MoveListFactory<TaskAssigningSolution> {

    @Override
    public List<? extends Move<TaskAssigningSolution>> createMoveList(TaskAssigningSolution taskAssigningSolution) {
        ArrayList<TaskListChangeMove> moves = new ArrayList<>();
        for (Task task : taskAssigningSolution.getTaskList()) {
            for (Employee employee : taskAssigningSolution.getEmployeeList()) {
                if (employee != task.getEmployee()) {
                    for (int i = 0; i <= employee.getTasks().size(); i++) {
                        moves.add(new TaskListChangeMove(task, employee, i));
                    }
                }
            }
        }
        return moves;
    }
}
