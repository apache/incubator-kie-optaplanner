/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.examples.nurserostering.domain;

import java.time.DayOfWeek;
import java.util.Comparator;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.nurserostering.domain.contract.Contract;
import org.optaplanner.examples.nurserostering.domain.solver.EmployeeStrengthComparator;
import org.optaplanner.examples.nurserostering.domain.solver.ShiftAssignmentDifficultyComparator;
import org.optaplanner.examples.nurserostering.domain.solver.ShiftAssignmentPinningFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity(pinningFilter = ShiftAssignmentPinningFilter.class,
        difficultyComparatorClass = ShiftAssignmentDifficultyComparator.class)
public class ShiftAssignment extends AbstractPersistable implements Comparable<ShiftAssignment> {

    private static final Comparator<Shift> COMPARATOR = Comparator.comparing(Shift::getShiftDate)
            .thenComparing(a -> a.getShiftType().getStartTimeString())
            .thenComparing(a -> a.getShiftType().getEndTimeString());

    private Shift shift;
    private int indexInShift;

    public ShiftAssignment() {
    }

    public ShiftAssignment(long id, Shift shift, int indexInShift) {
        super(id);
        this.shift = shift;
        this.indexInShift = indexInShift;
    }

    // Planning variables: changes during planning, between score calculations.
    @PlanningVariable(strengthComparatorClass = EmployeeStrengthComparator.class)
    private Employee employee;

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public int getIndexInShift() {
        return indexInShift;
    }

    public void setIndexInShift(int indexInShift) {
        this.indexInShift = indexInShift;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public ShiftDate getShiftDate() {
        return shift.getShiftDate();
    }

    @JsonIgnore
    public ShiftType getShiftType() {
        return shift.getShiftType();
    }

    @JsonIgnore
    public int getShiftDateDayIndex() {
        return shift.getShiftDate().getDayIndex();
    }

    @JsonIgnore
    public DayOfWeek getShiftDateDayOfWeek() {
        return shift.getShiftDate().getDayOfWeek();
    }

    @JsonIgnore
    public Contract getContract() {
        if (employee == null) {
            return null;
        }
        return employee.getContract();
    }

    @JsonIgnore
    public boolean isWeekend() {
        if (employee == null) {
            return false;
        }
        WeekendDefinition weekendDefinition = employee.getContract().getWeekendDefinition();
        DayOfWeek dayOfWeek = shift.getShiftDate().getDayOfWeek();
        return weekendDefinition.isWeekend(dayOfWeek);
    }

    @JsonIgnore
    public int getWeekendSundayIndex() {
        return shift.getShiftDate().getWeekendSundayIndex();
    }

    @Override
    public String toString() {
        return shift.toString();
    }

    @Override
    public int compareTo(ShiftAssignment o) {
        return COMPARATOR.compare(shift, o.shift);
    }
}
