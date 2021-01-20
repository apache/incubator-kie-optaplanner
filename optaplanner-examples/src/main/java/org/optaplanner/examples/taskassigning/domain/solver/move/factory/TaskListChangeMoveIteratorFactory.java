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

package org.optaplanner.examples.taskassigning.domain.solver.move.factory;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.examples.taskassigning.domain.Employee;
import org.optaplanner.examples.taskassigning.domain.Task;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.domain.solver.move.TaskListChangeMove;

public class TaskListChangeMoveIteratorFactory implements MoveIteratorFactory<TaskAssigningSolution, TaskListChangeMove> {

    @Override
    public long getSize(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        return (long) scoreDirector.getWorkingSolution().getEmployeeList().stream()
                .mapToInt(employee -> employee.getTasks().size() + 1)
                .sum() * scoreDirector.getWorkingSolution().getTaskList().size();
    }

    @Override
    public Iterator<TaskListChangeMove> createOriginalMoveIterator(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        List<Task> taskList = scoreDirector.getWorkingSolution().getTaskList();
        List<Employee> employeeList = scoreDirector.getWorkingSolution().getEmployeeList();

        return new Iterator<TaskListChangeMove>() {
            int taskIndex = 0;
            int employeeIndex = 0;
            int toIndex = 0;

            @Override
            public boolean hasNext() {
                return taskIndex < taskList.size();
            }

            @Override
            public TaskListChangeMove next() {
                Task task = taskList.get(taskIndex);
                Employee toEmployee = employeeList.get(employeeIndex);
                TaskListChangeMove changeMove = new TaskListChangeMove(task, toEmployee, toIndex);
                if (toIndex == toEmployee.getTasks().size()) {
                    toIndex = 0;
                    if (employeeIndex == employeeList.size() - 1) {
                        employeeIndex = 0;
                        taskIndex++;
                    } else {
                        employeeIndex++;
                    }
                } else {
                    toIndex++;
                }
                return changeMove;
            }
        };
    }

    @Override
    public Iterator<TaskListChangeMove> createRandomMoveIterator(ScoreDirector<TaskAssigningSolution> scoreDirector,
            Random workingRandom) {
        List<Task> taskList = scoreDirector.getWorkingSolution().getTaskList();
        List<Employee> employeeList = scoreDirector.getWorkingSolution().getEmployeeList();

        return new Iterator<TaskListChangeMove>() {
            @Override
            public boolean hasNext() {
                return employeeList.size() > 1 && taskList.size() > 0;
            }

            @Override
            public TaskListChangeMove next() {
                Task task = taskList.get(workingRandom.nextInt(taskList.size()));
                Employee toEmployee = employeeList.get(workingRandom.nextInt(employeeList.size()));
                int toIndex = workingRandom.nextInt(toEmployee.getTasks().size() + 1);
                return new TaskListChangeMove(task, toEmployee, toIndex);
            }
        };
    }
}
