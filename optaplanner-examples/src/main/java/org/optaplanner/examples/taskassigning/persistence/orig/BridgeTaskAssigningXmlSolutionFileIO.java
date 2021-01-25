/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.taskassigning.persistence.orig;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.optaplanner.examples.taskassigning.domain.Employee;
import org.optaplanner.examples.taskassigning.domain.Task;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.persistence.TaskAssigningXmlSolutionFileIO;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

/**
 * Reads a solution that was serialized from the original domain model on master into the new non-chained model.
 */
public class BridgeTaskAssigningXmlSolutionFileIO implements SolutionFileIO<TaskAssigningSolution> {

    private final OrigTaskAssigningSolutionIO origIO = new OrigTaskAssigningSolutionIO();
    private final TaskAssigningXmlSolutionFileIO currentIO = new TaskAssigningXmlSolutionFileIO();

    @Override
    public TaskAssigningSolution read(File inputSolutionFile) {
        if (inputSolutionFile.getName().contains("orig")) {
            return convert(origIO.read(inputSolutionFile));
        }
        return currentIO.read(inputSolutionFile);
    }

    private TaskAssigningSolution convert(OrigTaskAssigningSolution origSolution) {
        Map<Long, Employee> employeeByIdMap = origSolution.getEmployeeList().stream()
                .map(origEmployee -> {
                    Employee employee = new Employee(origEmployee.getId(), origEmployee.getFullName());
                    employee.setAffinityMap(origEmployee.getAffinityMap());
                    employee.setSkillSet(origEmployee.getSkillSet());
                    // Tasks will be added later.
                    return employee;
                })
                .collect(Collectors.toMap(Employee::getId, Function.identity()));

        Map<Long, Task> taskByIdMap = origSolution.getTaskList().stream()
                .map(origTask -> {
                    Task task = new Task(
                            origTask.getId(),
                            origTask.getTaskType(),
                            origTask.getIndexInTaskType(),
                            origTask.getCustomer(),
                            origTask.getReadyTime(),
                            origTask.getPriority());
                    task.setStartTime(origTask.getStartTime());
                    // Index will be set later.
                    task.setEmployee(employeeByIdMap.get(origTask.getEmployee().getId()));
                    return task;
                })
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        for (OrigEmployee origEmployee : origSolution.getEmployeeList()) {
            Employee employee = employeeByIdMap.get(origEmployee.getId());
            employee.setTasks(new ArrayList<>());
            OrigTask nextTask = origEmployee.getNextTask();
            int index = 0;
            while (nextTask != null) {
                Task task = taskByIdMap.get(nextTask.getId());
                // Set index.
                task.setIndex(index++);
                // Add task to the employee's taskList.
                employee.getTasks().add(task);
                nextTask = nextTask.getNextTask();
            }
        }

        return new TaskAssigningSolution(
                origSolution.getId(),
                origSolution.getSkillList(),
                origSolution.getTaskTypeList(),
                origSolution.getCustomerList(),
                new ArrayList<>(employeeByIdMap.values()),
                new ArrayList<>(taskByIdMap.values()));
    }

    @Override
    public void write(TaskAssigningSolution taskAssigningSolution, File outputSolutionFile) {
        currentIO.write(taskAssigningSolution, outputSolutionFile);
    }

    @Override
    public String getInputFileExtension() {
        return "xml";
    }

    private static class OrigTaskAssigningSolutionIO extends XStreamSolutionFileIO<OrigTaskAssigningSolution> {

        public OrigTaskAssigningSolutionIO() {
            super(OrigTaskAssigningSolution.class);
        }
    }
}
