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

import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.domain.solver.move.ListAppendMove;

public class ListAppendMoveFactory implements MoveListFactory<TaskAssigningSolution> {

    @Override
    public List<? extends Move<TaskAssigningSolution>> createMoveList(TaskAssigningSolution taskAssigningSolution) {
        return taskAssigningSolution.getEmployeeList().stream()
                .flatMap(employee -> taskAssigningSolution.getTaskList().stream()
                        .map(task -> new ListAppendMove(employee, task)))
                .collect(Collectors.toList());
    }
}
