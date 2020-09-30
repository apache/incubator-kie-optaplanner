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

package org.optaplanner.examples.nurserostering.domain.solver;

import static java.util.Comparator.comparingInt;

import java.util.Comparator;

import org.optaplanner.examples.nurserostering.domain.Employee;

public class EmployeeStrengthComparator implements Comparator<Employee> {

    private static final Comparator<Employee> COMPARATOR = comparingInt((Employee employee) -> -employee.getWeekendLength()) // Descending
            .thenComparingLong(Employee::getId);

    @Override
    public int compare(Employee a, Employee b) {
        // TODO refactor to DifficultyWeightFactory and use getContract().getContractLineList()
        //  to sum maximumValue and minimumValue etc
        return COMPARATOR.compare(a, b);
    }

}
