/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.nurserostering.optional.score;

import java.time.DayOfWeek;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.common.QuadTuple;
import org.optaplanner.core.impl.util.ConsecutiveData;
import org.optaplanner.core.impl.util.Sequence;
import org.optaplanner.examples.common.ExperimentalConstraintCollectors;
import org.optaplanner.examples.nurserostering.domain.Employee;
import org.optaplanner.examples.nurserostering.domain.ShiftAssignment;
import org.optaplanner.examples.nurserostering.domain.ShiftDate;
import org.optaplanner.examples.nurserostering.domain.ShiftType;
import org.optaplanner.examples.nurserostering.domain.ShiftTypeSkillRequirement;
import org.optaplanner.examples.nurserostering.domain.SkillProficiency;
import org.optaplanner.examples.nurserostering.domain.contract.BooleanContractLine;
import org.optaplanner.examples.nurserostering.domain.contract.ContractLine;
import org.optaplanner.examples.nurserostering.domain.contract.ContractLineType;
import org.optaplanner.examples.nurserostering.domain.contract.MinMaxContractLine;
import org.optaplanner.examples.nurserostering.domain.contract.PatternContractLine;
import org.optaplanner.examples.nurserostering.domain.pattern.FreeBefore2DaysWithAWorkDayPattern;
import org.optaplanner.examples.nurserostering.domain.pattern.ShiftType2DaysPattern;
import org.optaplanner.examples.nurserostering.domain.pattern.ShiftType3DaysPattern;
import org.optaplanner.examples.nurserostering.domain.request.DayOffRequest;
import org.optaplanner.examples.nurserostering.domain.request.DayOnRequest;
import org.optaplanner.examples.nurserostering.domain.request.ShiftOffRequest;
import org.optaplanner.examples.nurserostering.domain.request.ShiftOnRequest;
import org.optaplanner.examples.nurserostering.score.drools.EmployeeConsecutiveAssignmentEnd;
import org.optaplanner.examples.nurserostering.score.drools.EmployeeConsecutiveAssignmentStart;

public class NurseRosteringConstraintProvider implements ConstraintProvider {

    public <C extends ContractLine> BiConstraintStream<ImmutablePair<Employee, C>, Sequence<ShiftAssignment>>
            getConsecutiveShifts(UniConstraintStream<C> constraintStream) {
        return constraintStream.join(Employee.class, Joiners.equal(ContractLine::getContract, Employee::getContract))
                .join(ShiftAssignment.class, Joiners.equal((contract, employee) -> employee, ShiftAssignment::getEmployee))
                .groupBy((contract, employee, shift) -> ImmutablePair.of(employee, contract),
                        ExperimentalConstraintCollectors.consecutive((contract, employee, shift) -> shift,
                                ShiftAssignment::getShiftDateDayIndex))
                .flattenLast(ConsecutiveData::getConsecutiveSequences);
    }

    public BiConstraintStream<ImmutablePair<Employee, MinMaxContractLine>, Integer>
            getBreaks(UniConstraintStream<MinMaxContractLine> constraintStream) {
        return constraintStream.join(Employee.class, Joiners.equal(MinMaxContractLine::getContract, Employee::getContract))
                .join(ShiftAssignment.class, Joiners.equal((contract, employee) -> employee, ShiftAssignment::getEmployee))
                .groupBy((contract, employee, shift) -> ImmutablePair.of(employee, contract),
                        ExperimentalConstraintCollectors.consecutive((contract, employee, shift) -> shift,
                                ShiftAssignment::getShiftDateDayIndex))
                .flattenLast(ConsecutiveData::getBreaks);
    }

    public BiConstraintStream<ImmutablePair<Employee, MinMaxContractLine>, Sequence<ShiftAssignment>>
            getConsecutiveWorkingWeekends(UniConstraintStream<MinMaxContractLine> constraintStream) {
        return constraintStream.join(Employee.class, Joiners.equal(MinMaxContractLine::getContract, Employee::getContract))
                .join(ShiftAssignment.class, Joiners.equal((contract, employee) -> employee, ShiftAssignment::getEmployee),
                        Joiners.filtering((c, e, s) -> s.isWeekend()))
                .groupBy((contract, employee, shift) -> ImmutablePair.of(employee, contract),
                        ExperimentalConstraintCollectors.consecutive((contract, employee, shift) -> shift,
                                ShiftAssignment::getWeekendSundayIndex))
                .flattenLast(ConsecutiveData::getConsecutiveSequences);
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                oneShiftPerDay(constraintFactory),
                minimumAndMaximumNumberOfAssignments(constraintFactory),
                minimumNumberOfAssignmentsNoAssignments(constraintFactory),
                minimumConsecutiveWorkingDays(constraintFactory),
                maximumConsecutiveWorkingDays(constraintFactory),
                minimumConsecutiveFreeDays(constraintFactory),
                maximumConsecutiveFreeDays(constraintFactory),
                minimumConsecutiveWorkingWeekends(constraintFactory),
                maximumConsecutiveWorkingWeekends(constraintFactory),
                startOnNotFirstDayOfWeekend(constraintFactory),
                endOnNotLastDayOfWeekend(constraintFactory),
                identicalShiftTypesDuringWeekend(constraintFactory),
                dayOffRequest(constraintFactory),
                dayOnRequest(constraintFactory),
                shiftOffRequest(constraintFactory),
                shiftOnRequest(constraintFactory),
                alternativeSkill(constraintFactory),
                unwantedPatternFreeBefore2DaysWithAWorkDayPattern(constraintFactory),
                unwantedPatternShiftType2DaysPattern(constraintFactory),
                unwantedPatternShiftType3DaysPattern(constraintFactory),
        };
    }

    // ############################################################################
    // Hard constraints
    // ############################################################################

    // A nurse can only work one shift per day, i.e. no two shift can be assigned to the same nurse on a day.
    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(ShiftAssignment.class,
                        Joiners.equal(ShiftAssignment::getEmployee, ShiftAssignment::getShiftDate))
                .penalize("oneShiftPerDay", HardSoftScore.ONE_HARD);
    }

    // ############################################################################
    // Soft constraints
    // ############################################################################
    Constraint minimumAndMaximumNumberOfAssignments(ConstraintFactory constraintFactory) {
        return constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine.getContractLineType() == ContractLineType.TOTAL_ASSIGNMENTS &&
                        minMaxContractLine.isEnabled())
                .join(Employee.class,
                        Joiners.equal(MinMaxContractLine::getContract, Employee::getContract))
                .join(ShiftAssignment.class,
                        Joiners.equal((contractLine, employee) -> employee, ShiftAssignment::getEmployee))
                .groupBy((line, employee, shift) -> ImmutablePair.of(employee, line), ConstraintCollectors.countTri())
                .filter((employeeContractPair, shiftCount) -> employeeContractPair.getRight().isViolated(shiftCount))
                .penalize("Minimum and maximum number of assignments", HardSoftScore.ONE_SOFT,
                        (employeeContractPair, shiftCount) -> employeeContractPair.getRight().getViolationAmount(shiftCount));
    }

    Constraint minimumNumberOfAssignmentsNoAssignments(ConstraintFactory constraintFactory) {
        return constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine.getContractLineType() == ContractLineType.TOTAL_ASSIGNMENTS &&
                        minMaxContractLine.isEnabled())
                .join(Employee.class,
                        Joiners.equal(MinMaxContractLine::getContract, Employee::getContract))
                .ifNotExists(ShiftAssignment.class,
                        Joiners.equal((contractLine, employee) -> employee, ShiftAssignment::getEmployee))
                .filter((contract, employee) -> contract.isViolated(0))
                .penalize("Minimum and maximum number of assignments (no assignments)", HardSoftScore.ONE_SOFT,
                        (contract, employee) -> contract.getViolationAmount(0));
    }

    // Min/Max consecutive working days
    Constraint minimumConsecutiveWorkingDays(ConstraintFactory constraintFactory) {
        return getConsecutiveShifts(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_WORKING_DAYS &&
                        minMaxContractLine.isMinimumEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> shiftList.getLength() < employeeContractPair.getRight().getMinimumValue())
                                .penalize("minimumConsecutiveWorkingDays", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> employeeContractPair.getRight()
                                                .getViolationAmount(shiftList.getLength()));
    }

    Constraint maximumConsecutiveWorkingDays(ConstraintFactory constraintFactory) {
        return getConsecutiveShifts(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_WORKING_DAYS &&
                        minMaxContractLine.isMaximumEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> shiftList.getLength() > employeeContractPair.getRight().getMaximumValue())
                                .penalize("maximumConsecutiveWorkingDays", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> employeeContractPair.getRight()
                                                .getViolationAmount(shiftList.getLength()));
    }

    // Min/Max consecutive free days
    Constraint minimumConsecutiveFreeDays(ConstraintFactory constraintFactory) {
        return getBreaks(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_FREE_DAYS &&
                        minMaxContractLine.isMinimumEnabled()))
                                .filter((employeeContractPair,
                                        breakLength) -> breakLength < employeeContractPair.getRight().getMinimumValue())
                                .penalize("minimumConsecutiveFreeDays", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, breakLength) -> employeeContractPair.getRight()
                                                .getViolationAmount(breakLength));
    }

    Constraint maximumConsecutiveFreeDays(ConstraintFactory constraintFactory) {
        return getBreaks(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_FREE_DAYS &&
                        minMaxContractLine.isMaximumEnabled()))
                                .filter((employeeContractPair,
                                        breakLength) -> breakLength > employeeContractPair.getRight().getMaximumValue())
                                .penalize("maximumConsecutiveFreeDays", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, breakLength) -> employeeContractPair.getRight()
                                                .getViolationAmount(breakLength));
    }

    // Min/Max consecutive working weekends
    Constraint minimumConsecutiveWorkingWeekends(ConstraintFactory constraintFactory) {
        return getConsecutiveWorkingWeekends(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_WORKING_WEEKENDS &&
                        minMaxContractLine.isMinimumEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> shiftList.getLength() < employeeContractPair.getRight().getMinimumValue())
                                .penalize("minimumConsecutiveWorkingWeekends", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> employeeContractPair.getRight()
                                                .getViolationAmount(shiftList.getLength()));
    }

    Constraint maximumConsecutiveWorkingWeekends(ConstraintFactory constraintFactory) {
        return getConsecutiveWorkingWeekends(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_WORKING_WEEKENDS &&
                        minMaxContractLine.isMaximumEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> shiftList.getLength() > employeeContractPair.getRight().getMaximumValue())
                                .penalize("maximumConsecutiveWorkingWeekends", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> employeeContractPair.getRight()
                                                .getViolationAmount(shiftList.getLength()));
    }

    // Complete Weekends
    Constraint startOnNotFirstDayOfWeekend(ConstraintFactory constraintFactory) {
        return getConsecutiveShifts(constraintFactory.from(BooleanContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.COMPLETE_WEEKENDS &&
                        minMaxContractLine.isEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> new EmployeeConsecutiveAssignmentStart(
                                                employeeContractPair.getLeft(), shiftList.getItems().first().getShiftDate())
                                                        .isWeekendAndNotFirstDayOfWeekend())
                                .penalize("startOnNotFirstDayOfWeekend", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> new EmployeeConsecutiveAssignmentStart(
                                                employeeContractPair.getLeft(), shiftList.getItems().first().getShiftDate())
                                                        .getDistanceToFirstDayOfWeekend()
                                                * employeeContractPair.getRight().getWeight());
    }

    Constraint endOnNotLastDayOfWeekend(ConstraintFactory constraintFactory) {
        return getConsecutiveShifts(constraintFactory.from(BooleanContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.COMPLETE_WEEKENDS &&
                        minMaxContractLine.isEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> new EmployeeConsecutiveAssignmentEnd(
                                                employeeContractPair.getLeft(), shiftList.getItems().last().getShiftDate())
                                                        .isWeekendAndNotLastDayOfWeekend())
                                .penalize("endOnNotLastDayOfWeekend", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> new EmployeeConsecutiveAssignmentEnd(
                                                employeeContractPair.getLeft(), shiftList.getItems().last().getShiftDate())
                                                        .getDistanceToLastDayOfWeekend()
                                                * employeeContractPair.getRight().getWeight());
    }

    // Identical shiftTypes during a weekend
    Constraint identicalShiftTypesDuringWeekend(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BooleanContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.IDENTICAL_SHIFT_TYPES_DURING_WEEKEND &&
                        minMaxContractLine.isEnabled())
                .join(Employee.class, Joiners.equal(ContractLine::getContract, Employee::getContract))
                .join(ShiftDate.class, Joiners.filtering((c, e, date) -> date.getDayOfWeek() == DayOfWeek.SUNDAY))
                .join(ShiftType.class)
                .map(QuadTuple::new)
                .join(ShiftAssignment.class,
                        Joiners.equal(t -> t.c.getWeekendSundayIndex(), ShiftAssignment::getWeekendSundayIndex),
                        Joiners.equal(t -> t.b, ShiftAssignment::getEmployee),
                        Joiners.equal(t -> t.d, ShiftAssignment::getShiftType),
                        Joiners.filtering((t, shift) -> shift.isWeekend()))
                .groupBy((tuple, sa) -> tuple, ConstraintCollectors.countBi())
                .filter((tuple, count) -> count < tuple.b.getWeekendLength())
                .penalize("identicalShiftTypesDuringWeekend", HardSoftScore.ONE_SOFT,
                        (tuple, count) -> (tuple.b.getWeekendLength() - count) * tuple.a.getWeight());
    }

    // Requested day on/off
    Constraint dayOffRequest(ConstraintFactory constraintFactory) {
        return constraintFactory.from(DayOffRequest.class)
                .join(ShiftAssignment.class, Joiners.equal(DayOffRequest::getEmployee, ShiftAssignment::getEmployee),
                        Joiners.equal(DayOffRequest::getShiftDate, ShiftAssignment::getShiftDate))
                .penalize("dayOffRequest", HardSoftScore.ONE_SOFT,
                        (dayOffRequest, shiftAssignment) -> dayOffRequest.getWeight());
    }

    Constraint dayOnRequest(ConstraintFactory constraintFactory) {
        return constraintFactory.from(DayOnRequest.class)
                .ifNotExists(ShiftAssignment.class, Joiners.equal(DayOnRequest::getEmployee, ShiftAssignment::getEmployee),
                        Joiners.equal(DayOnRequest::getShiftDate, ShiftAssignment::getShiftDate))
                .penalize("dayOnRequest", HardSoftScore.ONE_SOFT, DayOnRequest::getWeight);
    }

    // Requested shift on/off
    Constraint shiftOffRequest(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ShiftOffRequest.class)
                .join(ShiftAssignment.class, Joiners.equal(ShiftOffRequest::getEmployee, ShiftAssignment::getEmployee),
                        Joiners.equal(ShiftOffRequest::getShift, ShiftAssignment::getShift))
                .penalize("shiftOffRequest", HardSoftScore.ONE_SOFT,
                        (shiftOffRequest, shiftAssignment) -> shiftOffRequest.getWeight());
    }

    Constraint shiftOnRequest(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ShiftOnRequest.class)
                .ifNotExists(ShiftAssignment.class, Joiners.equal(ShiftOnRequest::getEmployee, ShiftAssignment::getEmployee),
                        Joiners.equal(ShiftOnRequest::getShift, ShiftAssignment::getShift))
                .penalize("shiftOnRequest", HardSoftScore.ONE_SOFT, ShiftOnRequest::getWeight);
    }

    // Alternative skill
    Constraint alternativeSkill(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BooleanContractLine.class)
                .filter(booleanContractLine -> booleanContractLine.getContractLineType()
                        .equals(ContractLineType.ALTERNATIVE_SKILL_CATEGORY))
                .join(ShiftAssignment.class, Joiners.equal(BooleanContractLine::getContract, ShiftAssignment::getContract))
                .join(ShiftTypeSkillRequirement.class,
                        Joiners.equal((contract, sa) -> sa.getShiftType(), ShiftTypeSkillRequirement::getShiftType))
                .ifNotExists(SkillProficiency.class,
                        Joiners.equal((contract, sa, stsr) -> sa.getEmployee(), SkillProficiency::getEmployee),
                        Joiners.equal((contract, sa, stsr) -> stsr.getSkill(), SkillProficiency::getSkill))
                .penalize("alternativeSkill", HardSoftScore.ONE_SOFT,
                        (contractLine, sa, stsr) -> contractLine.getWeight());
    }

    // Unwanted patterns
    Constraint unwantedPatternFreeBefore2DaysWithAWorkDayPattern(ConstraintFactory constraintFactory) {
        return constraintFactory.from(FreeBefore2DaysWithAWorkDayPattern.class)
                .join(PatternContractLine.class, Joiners.equal(p -> p, PatternContractLine::getPattern))
                .join(ShiftDate.class,
                        Joiners.equal((pattern, contract) -> pattern.getFreeDayOfWeek(), ShiftDate::getDayOfWeek))
                .join(Employee.class,
                        Joiners.equal((pattern, contractLine, date) -> contractLine.getContract(), Employee::getContract))
                .ifNotExists(ShiftAssignment.class,
                        Joiners.equal((pattern, contractLine, date, employee) -> employee, ShiftAssignment::getEmployee),
                        Joiners.equal((pattern, contractLine, date, employee) -> date.getDayIndex(),
                                ShiftAssignment::getShiftDateDayIndex))
                .ifExists(ShiftAssignment.class,
                        Joiners.equal((pattern, contractLine, date, employee) -> employee, ShiftAssignment::getEmployee),
                        // TODO: Replace with pair of greaterThan/lessThan joiners?
                        Joiners.filtering((pattern, contractLine, date, employee,
                                shift) -> shift.getShiftDateDayIndex() == (date.getDayIndex() + 1)
                                        || shift.getShiftDateDayIndex() == (date.getDayIndex() + 2)))
                .penalize("unwantedPatternFreeBefore2DaysWithAWorkDayPattern", HardSoftScore.ONE_SOFT,
                        (pattern, contractLine, date, employee) -> pattern.getWeight());
    }

    Constraint unwantedPatternShiftType2DaysPattern(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ShiftType2DaysPattern.class)
                .join(PatternContractLine.class, Joiners.equal(p -> p, PatternContractLine::getPattern))
                .join(ShiftAssignment.class,
                        Joiners.equal((pattern, contractLine) -> pattern.getDayIndex0ShiftType(),
                                ShiftAssignment::getShiftType),
                        Joiners.equal((pattern, contractLine) -> contractLine.getContract(), ShiftAssignment::getContract))
                .join(ShiftAssignment.class,
                        Joiners.equal((pattern, contractLine, shift) -> shift.getEmployee(), ShiftAssignment::getEmployee),
                        Joiners.equal((pattern, contractLine, shift) -> shift.getShiftDateDayIndex() + 1,
                                ShiftAssignment::getShiftDateDayIndex),
                        Joiners.filtering((pattern, contractLine, shift1, shift2) -> pattern.getDayIndex1ShiftType() == null
                                || shift2.getShiftType() == pattern.getDayIndex1ShiftType()))
                .penalize("unwantedPatternShiftType2DaysPattern", HardSoftScore.ONE_SOFT,
                        (pattern, contractLine, shift1, shift2) -> pattern.getWeight());
    }

    Constraint unwantedPatternShiftType3DaysPattern(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ShiftType3DaysPattern.class)
                .join(PatternContractLine.class, Joiners.equal(p -> p, PatternContractLine::getPattern))
                .join(ShiftAssignment.class,
                        Joiners.equal((pattern, contractLine) -> pattern.getDayIndex0ShiftType(),
                                ShiftAssignment::getShiftType),
                        Joiners.equal((pattern, contractLine) -> contractLine.getContract(), ShiftAssignment::getContract))
                .ifExists(ShiftAssignment.class,
                        Joiners.equal((pattern, contractLine, shift) -> shift.getEmployee(), ShiftAssignment::getEmployee),
                        Joiners.equal((pattern, contractLine, shift) -> shift.getShiftDateDayIndex() + 1,
                                ShiftAssignment::getShiftDateDayIndex),
                        Joiners.equal((pattern, contractLine, shift) -> pattern.getDayIndex1ShiftType(),
                                ShiftAssignment::getShiftType))
                .ifExists(ShiftAssignment.class,
                        Joiners.equal((pattern, contractLine, shift) -> shift.getEmployee(), ShiftAssignment::getEmployee),
                        Joiners.equal((pattern, contractLine, shift) -> shift.getShiftDateDayIndex() + 2,
                                ShiftAssignment::getShiftDateDayIndex),
                        Joiners.equal((pattern, contractLine, shift) -> pattern.getDayIndex2ShiftType(),
                                ShiftAssignment::getShiftType))
                .penalize("unwantedPatternShiftType3DaysPattern", HardSoftScore.ONE_SOFT,
                        (pattern, contractLine, shift1) -> pattern.getWeight());
    }
}
