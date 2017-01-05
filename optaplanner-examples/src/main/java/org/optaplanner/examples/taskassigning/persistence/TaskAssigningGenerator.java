/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.taskassigning.persistence;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.optaplanner.examples.common.app.LoggingMain;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.persistence.SolutionDao;
import org.optaplanner.examples.common.persistence.StringDataGenerator;
import org.optaplanner.examples.taskassigning.domain.Affinity;
import org.optaplanner.examples.taskassigning.domain.Customer;
import org.optaplanner.examples.taskassigning.domain.Employee;
import org.optaplanner.examples.taskassigning.domain.Priority;
import org.optaplanner.examples.taskassigning.domain.Skill;
import org.optaplanner.examples.taskassigning.domain.Task;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.domain.TaskType;

public class TaskAssigningGenerator extends LoggingMain {

    public static final int BASE_DURATION_MINIMUM = 30;
    public static final int BASE_DURATION_MAXIMUM = 90;
    public static final int BASE_DURATION_AVERAGE = BASE_DURATION_MINIMUM + BASE_DURATION_MAXIMUM / 2;
    private static final int SKILL_SET_SIZE_MINIMUM = 2;
    private static final int SKILL_SET_SIZE_MAXIMUM = 4;

    public static void main(String[] args) {
        TaskAssigningGenerator generator = new TaskAssigningGenerator();
        generator.writeTaskAssigningSolution(24, 8);
        generator.writeTaskAssigningSolution(50, 5);
        generator.writeTaskAssigningSolution(100, 5);
        generator.writeTaskAssigningSolution(500, 20);
        // For more tasks, switch to BendableLongScore to avoid overflow in the score.
    }

    private final StringDataGenerator skillNameGenerator = new StringDataGenerator()
            .addPart(
                    "Problem",
                    "Team",
                    "Business",
                    "Risk",
                    "Creative",
                    "Strategic",
                    "Customer",
                    "Conflict",
                    "IT",
                    "Academic")
            .addPart(
                    "Solving",
                    "Building",
                    "Storytelling",
                    "Management",
                    "Thinking",
                    "Planning",
                    "Service",
                    "Resolution",
                    "Engineering",
                    "Research");
    private final StringDataGenerator taskTypeNameGenerator = new StringDataGenerator()
            .addPart(
                    "Improve",
                    "Expand",
                    "Shrink",
                    "Approve",
                    "Localize",
                    "Review",
                    "Clean",
                    "Merge",
                    "Double",
                    "Optimize")
            .addPart(
                    "Sales",
                    "Tax",
                    "VAT",
                    "Legal",
                    "Cloud",
                    "Marketing",
                    "IT",
                    "Contract",
                    "Financial",
                    "Advertisement")
            .addPart(
                    "Software",
                    "Development",
                    "Accounting",
                    "Management",
                    "Facilities",
                    "Writing",
                    "Productization",
                    "Lobbying",
                    "Engineering",
                    "Research");
    private final StringDataGenerator customerNameGenerator = StringDataGenerator.build1kCompanyNames();
    private final StringDataGenerator employeeNameGenerator = StringDataGenerator.build10kFullNames();

    protected final SolutionDao solutionDao;
    protected final File outputDir;
    protected Random random;

    public TaskAssigningGenerator() {
        solutionDao = new TaskAssigningDao();
        outputDir = new File(solutionDao.getDataDir(), "unsolved");
    }

    private void writeTaskAssigningSolution(int taskListSize, int employeeListSize) {
        int skillListSize = SKILL_SET_SIZE_MAXIMUM + (int) Math.log(employeeListSize);
        int taskTypeListSize = taskListSize / 5;
        int customerListSize = Math.min(taskTypeListSize, employeeListSize * 3);
        String fileName = determineFileName(taskListSize, employeeListSize);
        File outputFile = new File(outputDir, fileName + ".xml");
        TaskAssigningSolution solution = createTaskAssigningSolution(fileName,
                taskListSize, skillListSize, employeeListSize, taskTypeListSize, customerListSize);
        solutionDao.writeSolution(solution, outputFile);
    }

    private String determineFileName(int taskListSize, int employeeListSize) {
        return taskListSize + "tasks-" + employeeListSize + "employees";
    }

    public TaskAssigningSolution createTaskAssigningSolution(String fileName, int taskListSize, int skillListSize,
            int employeeListSize, int taskTypeListSize, int customerListSize) {
        random = new Random(37);
        skillNameGenerator.reset();
        taskTypeNameGenerator.reset();
        customerNameGenerator.reset();
        employeeNameGenerator.reset();
        TaskAssigningSolution solution = new TaskAssigningSolution();
        solution.setId(0L);

        createSkillList(solution, skillListSize);
        createCustomerList(solution, customerListSize);
        createEmployeeList(solution, employeeListSize);
        createTaskTypeList(solution, taskTypeListSize);
        createTaskList(solution, taskListSize);

        BigInteger possibleSolutionSize
                = AbstractSolutionImporter.InputBuilder.factorial(taskListSize + employeeListSize - 1)
                .divide(AbstractSolutionImporter.InputBuilder.factorial(employeeListSize - 1));
        logger.info("TaskAssigningSolution {} has {} tasks, {} skills, {} employees, {} task types and {} customers with a search space of {}.",
                fileName,
                taskListSize,
                skillListSize,
                employeeListSize,
                taskTypeListSize,
                customerListSize,
                AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return solution;
    }

    private void createSkillList(TaskAssigningSolution solution, int skillListSize) {
        List<Skill> skillList = new ArrayList<>(skillListSize);
        for (int i = 0; i < skillListSize; i++) {
            Skill skill = new Skill();
            skill.setId((long) i);
            String skillName = skillNameGenerator.generateNextValue();
            skill.setName(skillName);
            logger.trace("Created skill with skillName ({}).", skillName);
            skillList.add(skill);
        }
        solution.setSkillList(skillList);
    }

    private void createCustomerList(TaskAssigningSolution solution, int customerListSize) {
        List<Customer> customerList = new ArrayList<>(customerListSize);
        for (int i = 0; i < customerListSize; i++) {
            Customer customer = new Customer();
            customer.setId((long) i);
            String customerName = customerNameGenerator.generateNextValue();
            customer.setName(customerName);
            logger.trace("Created skill with customerName ({}).", customerName);
            customerList.add(customer);
        }
        solution.setCustomerList(customerList);
    }

    private void createEmployeeList(TaskAssigningSolution solution, int employeeListSize) {
        List<Skill> skillList = solution.getSkillList();
        List<Customer> customerList = solution.getCustomerList();
        Affinity[] affinities = Affinity.values();
        List<Employee> employeeList = new ArrayList<>(employeeListSize);
        int skillListIndex = 0;
        for (int i = 0; i < employeeListSize; i++) {
            Employee employee = new Employee();
            employee.setId((long) i);
            String fullName = employeeNameGenerator.generateNextValue();
            employee.setFullName(fullName);
            int skillSetSize = SKILL_SET_SIZE_MINIMUM + random.nextInt(SKILL_SET_SIZE_MAXIMUM - SKILL_SET_SIZE_MINIMUM);
            if (skillSetSize > skillList.size()) {
                skillSetSize = skillList.size();
            }
            Set<Skill> skillSet = new LinkedHashSet<>(skillSetSize);
            for (int j = 0; j < skillSetSize; j++) {
                skillSet.add(skillList.get(skillListIndex));
                skillListIndex = (skillListIndex + 1) % skillList.size();
            }
            employee.setSkillSet(skillSet);
            Map<Customer, Affinity> affinityMap = new LinkedHashMap<>(customerList.size());
            for (Customer customer : customerList) {
                affinityMap.put(customer, affinities[random.nextInt(affinities.length)]);
            }
            employee.setAffinityMap(affinityMap);
            logger.trace("Created employee with fullName ({}).", fullName);
            employeeList.add(employee);
        }
        solution.setEmployeeList(employeeList);
    }

    private void createTaskTypeList(TaskAssigningSolution solution, int taskTypeListSize) {
        List<Employee> employeeList = solution.getEmployeeList();
        List<TaskType> taskTypeList = new ArrayList<>(taskTypeListSize);
        Set<String> codeSet = new LinkedHashSet<>(taskTypeListSize);
        for (int i = 0; i < taskTypeListSize; i++) {
            TaskType taskType = new TaskType();
            taskType.setId((long) i);
            String title = taskTypeNameGenerator.generateNextValue();
            taskType.setTitle(title);
            String code = title.replaceAll("(\\w)\\w* (\\w)\\w* (\\w)\\w*", "$1$2$3");
            if (codeSet.contains(code)) {
                int codeSuffixNumber = 1;
                while (codeSet.contains(code + codeSuffixNumber)) {
                    codeSuffixNumber++;
                }
                code = code + codeSuffixNumber;
            }
            codeSet.add(code);
            taskType.setCode(code);
            taskType.setBaseDuration(
                    BASE_DURATION_MINIMUM + random.nextInt(BASE_DURATION_MAXIMUM - BASE_DURATION_MINIMUM));
            Employee randomEmployee = employeeList.get(random.nextInt(employeeList.size()));
            ArrayList<Skill> randomSkillList = new ArrayList<>(randomEmployee.getSkillSet());
            Collections.shuffle(randomSkillList, random);
            int requiredSkillListSize = 1 + random.nextInt(randomSkillList.size() - 1);
            taskType.setRequiredSkillList(new ArrayList<>(randomSkillList.subList(0, requiredSkillListSize)));
            logger.trace("Created taskType with title ({}).", title);
            taskTypeList.add(taskType);
        }
        solution.setTaskTypeList(taskTypeList);
    }

    private void createTaskList(TaskAssigningSolution solution, int taskListSize) {
        List<TaskType> taskTypeList = solution.getTaskTypeList();
        List<Customer> customerList = solution.getCustomerList();
        Priority[] priorities = Priority.values();
        List<Task> taskList = new ArrayList<>(taskListSize);
        Map<TaskType, Integer> maxIndexInTaskTypeMap = new LinkedHashMap<>(taskTypeList.size());
        for (int i = 0; i < taskListSize; i++) {
            Task task = new Task();
            task.setId((long) i);
            TaskType taskType = taskTypeList.get(random.nextInt(taskTypeList.size()));
            task.setTaskType(taskType);
            Integer indexInTaskType = maxIndexInTaskTypeMap.get(taskType);
            if (indexInTaskType == null) {
                indexInTaskType = 1;
            } else {
                indexInTaskType++;
            }
            task.setIndexInTaskType(indexInTaskType);
            maxIndexInTaskTypeMap.put(taskType, indexInTaskType);
            task.setCustomer(customerList.get(random.nextInt(customerList.size())));
            task.setReadyTime(0);
            task.setPriority(priorities[random.nextInt(priorities.length)]);
            taskList.add(task);
        }
        solution.setTaskList(taskList);
    }

}
