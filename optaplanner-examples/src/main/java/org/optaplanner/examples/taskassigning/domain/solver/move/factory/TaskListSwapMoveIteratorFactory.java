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
import org.optaplanner.examples.taskassigning.domain.Task;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.domain.solver.move.TaskListSwapMove;

public class TaskListSwapMoveIteratorFactory implements MoveIteratorFactory<TaskAssigningSolution, TaskListSwapMove> {

    @Override
    public long getSize(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        int taskCount = scoreDirector.getWorkingSolution().getTaskList().size();
        return (long) taskCount * (taskCount - 1);
    }

    @Override
    public Iterator<TaskListSwapMove> createOriginalMoveIterator(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        List<Task> taskList = scoreDirector.getWorkingSolution().getTaskList();

        return new Iterator<TaskListSwapMove>() {
            int leftTask = 0;
            int rightTask = 1;

            @Override
            public boolean hasNext() {
                return leftTask < taskList.size() && rightTask < taskList.size();
            }

            @Override
            public TaskListSwapMove next() {
                TaskListSwapMove move = new TaskListSwapMove(taskList.get(leftTask), taskList.get(rightTask));
                rightTask++;
                if (rightTask == leftTask) {
                    rightTask++;
                }
                if (rightTask == taskList.size()) {
                    rightTask = 0;
                    leftTask++;
                }
                return move;
            }
        };
    }

    @Override
    public Iterator<TaskListSwapMove> createRandomMoveIterator(ScoreDirector<TaskAssigningSolution> scoreDirector,
            Random workingRandom) {
        List<Task> taskList = scoreDirector.getWorkingSolution().getTaskList();
        return new Iterator<TaskListSwapMove>() {
            @Override
            public boolean hasNext() {
                return taskList.size() > 1;
            }

            @Override
            public TaskListSwapMove next() {
                int leftTask = workingRandom.nextInt(taskList.size());
                int rightTask = leftTask;
                while (rightTask == leftTask) {
                    rightTask = workingRandom.nextInt(taskList.size());
                }
                return new TaskListSwapMove(taskList.get(leftTask), taskList.get(rightTask));
            }
        };
    }
}
