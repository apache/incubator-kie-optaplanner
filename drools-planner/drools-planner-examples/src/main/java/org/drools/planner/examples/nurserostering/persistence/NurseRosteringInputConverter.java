package org.drools.planner.examples.nurserostering.persistence;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.AbstractXmlInputConverter;
import org.drools.planner.examples.nurserostering.domain.Contract;
import org.drools.planner.examples.nurserostering.domain.DayOfWeek;
import org.drools.planner.examples.nurserostering.domain.DayOffRequest;
import org.drools.planner.examples.nurserostering.domain.DayOnRequest;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;
import org.drools.planner.examples.nurserostering.domain.ShiftPattern;
import org.drools.planner.examples.nurserostering.domain.ShiftType;
import org.drools.planner.examples.nurserostering.domain.ShiftTypeSkillRequirement;
import org.drools.planner.examples.nurserostering.domain.Skill;
import org.drools.planner.examples.nurserostering.domain.SkillProficiency;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringInputConverter extends AbstractXmlInputConverter {

    public static void main(String[] args) {
        new NurseRosteringInputConverter().convertAll();
    }

    public NurseRosteringInputConverter() {
        super(new NurseRosteringDaoImpl());
    }

    public XmlInputBuilder createXmlInputBuilder() {
        return new NurseRosteringInputBuilder();
    }

    public class NurseRosteringInputBuilder extends XmlInputBuilder {

        protected Map<String, ShiftDate> shiftDateMap;
        protected Map<String, Skill> skillMap;
        protected Map<String, ShiftType> shiftTypeMap;
        protected Map<String, ShiftPattern> shiftPatternMap;
        protected Map<String, Contract> contractMap;
        protected Map<String, Employee> employeeMap;

        public Solution readSolution() throws IOException, JDOMException {
            // Note: javax.xml is terrible. JDom is much much easier.

            Element schedulingPeriodElement = document.getRootElement();
            assertElementName(schedulingPeriodElement, "SchedulingPeriod");
            NurseRoster nurseRoster = new NurseRoster();
            nurseRoster.setId(0L);
            nurseRoster.setCode(schedulingPeriodElement.getAttribute("ID").getValue());

            generateShiftDateList(nurseRoster,
                    schedulingPeriodElement.getChild("StartDate"),
                    schedulingPeriodElement.getChild("EndDate"));
            readSkillList(nurseRoster, schedulingPeriodElement.getChild("Skills"));
            readShiftTypeList(nurseRoster, schedulingPeriodElement.getChild("ShiftTypes"));
            readShiftPatternList(nurseRoster, schedulingPeriodElement.getChild("Patterns"));
            readContractList(nurseRoster, schedulingPeriodElement.getChild("Contracts"));
            readEmployeeList(nurseRoster, schedulingPeriodElement.getChild("Employees"));
//            readTodoList(nurseRoster, schedulingPeriodElement.getChild("CoverRequirements"));
            readDayOffRequestList(nurseRoster, schedulingPeriodElement.getChild("DayOffRequests"));
            readDayOnRequestList(nurseRoster, schedulingPeriodElement.getChild("DayOnRequests"));
//            readShiftOffRequestList(nurseRoster, schedulingPeriodElement.getChild("ShiftOffRequests"));
//            readShiftOnRequestList(nurseRoster, schedulingPeriodElement.getChild("ShiftOnRequests"));

            logger.info("NurseRoster {} with TODO.",
                    new Object[]{nurseRoster.getCode()});
            // TODO log other stats

            return nurseRoster;
        }

        private void generateShiftDateList(NurseRoster nurseRoster,
                Element startDateElement, Element endDateElement) throws JDOMException {
            // Mimic JSR-310 LocalDate
            TimeZone LOCAL_TIMEZONE = TimeZone.getTimeZone("GMT");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(LOCAL_TIMEZONE);
            calendar.clear();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setCalendar(calendar);
            Date startDate;
            try {
                startDate = dateFormat.parse(startDateElement.getText());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid startDate (" + startDateElement.getText() + ").", e);
            }
            calendar.setTime(startDate);
            int startDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            int startYear = calendar.get(Calendar.YEAR);
            Date endDate;
            try {
                endDate = dateFormat.parse(endDateElement.getText());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid endDate (" + endDateElement.getText() + ").", e);
            }
            calendar.setTime(endDate);
            int endDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            int endYear = calendar.get(Calendar.YEAR);
            int maxDayIndex = endDayOfYear - startDayOfYear;
            if (startYear > endYear) {
                throw new IllegalStateException("The startYear (" + startYear
                        + " must be before endYear (" + endYear + ").");
            } if (startYear < endYear) {
                int tmpYear = startYear;
                calendar.setTime(startDate);
                while (tmpYear < endYear) {
                    maxDayIndex += calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
                    calendar.add(Calendar.YEAR, 1);
                    tmpYear++;
                }
            }
            int shiftDateSize = maxDayIndex + 1;
            List<ShiftDate> shiftDateList = new ArrayList<ShiftDate>(shiftDateSize);
            shiftDateMap = new HashMap<String, ShiftDate>(shiftDateSize);
            long id = 0L;
            int dayIndex = 0;
            calendar.setTime(startDate);
            for (int i = 0; i < shiftDateSize; i++) {
                ShiftDate shiftDate = new ShiftDate();
                shiftDate.setId(id);
                shiftDate.setDayIndex(dayIndex);
                String dateString = dateFormat.format(calendar.getTime());
                shiftDate.setDateString(dateString);
                shiftDate.setDayOfWeek(DayOfWeek.valueOfCalendar(calendar.get(Calendar.DAY_OF_WEEK)));
                shiftDateList.add(shiftDate);
                this.shiftDateMap.put(dateString, shiftDate);
                id++;
                dayIndex++;
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            nurseRoster.setShiftDateList(shiftDateList);
        }

        private void readSkillList(NurseRoster nurseRoster, Element skillsElement) throws JDOMException {
            if (skillsElement == null) {
                return;
            }
            List<Element> skillElementList = (List<Element>) skillsElement.getChildren();
            List<Skill> skillList = new ArrayList<Skill>(skillElementList.size());
            skillMap = new HashMap<String, Skill>(skillElementList.size());
            long id = 0L;
            for (Element element : skillElementList) {
                assertElementName(element, "Skill");
                Skill skill = new Skill();
                skill.setId(id);
                skill.setCode(element.getText());
                skillList.add(skill);
                skillMap.put(skill.getCode(), skill);
                id++;
            }
            nurseRoster.setSkillList(skillList);
        }

        private void readShiftTypeList(NurseRoster nurseRoster, Element shiftTypesElement) throws JDOMException {
            List<Element> shiftElementList = (List<Element>) shiftTypesElement.getChildren();
            List<ShiftType> shiftTypeList = new ArrayList<ShiftType>(shiftElementList.size());
            shiftTypeMap = new HashMap<String, ShiftType>(shiftElementList.size());
            long id = 0L;
            List<ShiftTypeSkillRequirement> shiftTypeSkillRequirementList
                    = new ArrayList<ShiftTypeSkillRequirement>(shiftElementList.size() * 2);
            long shiftTypeSkillRequirementId = 0L;
            for (Element element : shiftElementList) {
                assertElementName(element, "Shift");
                ShiftType shiftType = new ShiftType();
                shiftType.setId(id);
                shiftType.setCode(element.getAttribute("ID").getValue());
                shiftType.setStartTimeString(element.getChild("StartTime").getText());
                shiftType.setEndTimeString(element.getChild("EndTime").getText());
                shiftType.setDescription(element.getChild("Description").getText());

                Element skillsElement = element.getChild("Skills");
                if (skillsElement != null) {
                    List<Element> skillElementList = (List<Element>) skillsElement.getChildren();
                    for (Element skillElement : skillElementList) {
                        assertElementName(skillElement, "Skill");
                        ShiftTypeSkillRequirement shiftTypeSkillRequirement = new ShiftTypeSkillRequirement();
                        shiftTypeSkillRequirement.setId(shiftTypeSkillRequirementId);
                        shiftTypeSkillRequirement.setShiftType(shiftType);
                        Skill skill = skillMap.get(skillElement.getText());
                        if (skill == null) {
                            throw new IllegalArgumentException("The skill (" + skillElement.getText()
                                    + ") of shiftType (" + shiftType.getCode() + ") does not exist.");
                        }
                        shiftTypeSkillRequirement.setSkill(skill);
                        shiftTypeSkillRequirementList.add(shiftTypeSkillRequirement);
                        shiftTypeSkillRequirementId++;
                    }
                }

                shiftTypeList.add(shiftType);
                shiftTypeMap.put(shiftType.getCode(), shiftType);
                id++;
            }
            nurseRoster.setShiftTypeList(shiftTypeList);
            nurseRoster.setShiftTypeSkillRequirementList(shiftTypeSkillRequirementList);
        }

        private void readShiftPatternList(NurseRoster nurseRoster, Element patternsElement) throws JDOMException {
            if (patternsElement == null) {
                return;
            }
            List<Element> patternElementList = (List<Element>) patternsElement.getChildren();
            List<ShiftPattern> shiftPatternList = new ArrayList<ShiftPattern>(patternElementList.size());
            shiftPatternMap = new HashMap<String, ShiftPattern>(patternElementList.size());
            long id = 0L;
            for (Element element : patternElementList) {
                assertElementName(element, "Pattern");
                ShiftPattern shiftPattern = new ShiftPattern();
                shiftPattern.setId(id);
                shiftPattern.setCode(element.getAttribute("ID").getValue());
                shiftPattern.setWeight(element.getAttribute("weight").getIntValue());

                List<Element> patternEntryElementList = (List<Element>) element.getChild("PatternEntries")
                        .getChildren();
                for (Element patternEntryElement : patternEntryElementList) {
                    assertElementName(patternEntryElement, "PatternEntry");
                    Element shiftTypeElement = patternEntryElement.getChild("ShiftType");
                    ShiftType shiftType = shiftTypeMap.get(shiftTypeElement.getText());
                    if (shiftType == null) {
                        if (shiftTypeElement.getText().equals("Any")) {
                            // TODO


                        } else if (shiftTypeElement.getText().equals("None")) {
                            // TODO

                            
                        } else {
                            throw new IllegalArgumentException("The shiftType (" + shiftTypeElement.getText()
                                    + ") of shiftPattern (" + shiftPattern.getCode() + ") does not exist.");
                        }
                    }
                    // TODO shiftType & day etc

//        <PatternEntry index="0">
//          <ShiftType>None</ShiftType>
//          <Day>Friday</Day>
//        </PatternEntry>
//        <PatternEntry index="1">
//          <ShiftType>Any</ShiftType>
//          <Day>Saturday</Day>
//        </PatternEntry>
//        <PatternEntry index="2">
//          <ShiftType>Any</ShiftType>
//          <Day>Sunday</Day>
//        </PatternEntry>

                }

                shiftPatternList.add(shiftPattern);
                shiftPatternMap.put(shiftPattern.getCode(), shiftPattern);
                id++;
            }
            nurseRoster.setShiftPatternList(shiftPatternList);
        }

        private void readContractList(NurseRoster nurseRoster, Element contractsElement) throws JDOMException {
            List<Element> contractElementList = (List<Element>) contractsElement.getChildren();
            List<Contract> contractList = new ArrayList<Contract>(contractElementList.size());
            contractMap = new HashMap<String, Contract>(contractElementList.size());
            long id = 0L;
            for (Element element : contractElementList) {
                assertElementName(element, "Contract");
                Contract contract = new Contract();
                contract.setId(id);
                contract.setCode(element.getAttribute("ID").getValue());
                contract.setDescription(element.getChild("Description").getText());
                // TODO the rest of the contract
//      <SingleAssignmentPerDay weight="1">true</SingleAssignmentPerDay>
//      <MaxNumAssignments on="1" weight="1">16</MaxNumAssignments>
//      <MinNumAssignments on="1" weight="1">6</MinNumAssignments>
//      <MaxConsecutiveWorkingDays on="1" weight="1">7</MaxConsecutiveWorkingDays>
//      <MinConsecutiveWorkingDays on="1" weight="1">1</MinConsecutiveWorkingDays>
//      <MaxConsecutiveFreeDays on="1" weight="1">5</MaxConsecutiveFreeDays>
//      <MinConsecutiveFreeDays on="1" weight="1">1</MinConsecutiveFreeDays>
//      <MaxConsecutiveWorkingWeekends on="0" weight="0">7</MaxConsecutiveWorkingWeekends>
//      <MinConsecutiveWorkingWeekends on="0" weight="0">1</MinConsecutiveWorkingWeekends>
//      <MaxWorkingWeekendsInFourWeeks on="0" weight="0">0</MaxWorkingWeekendsInFourWeeks>
//      <WeekendDefinition>SaturdaySunday</WeekendDefinition>
//      <CompleteWeekends weight="1">true</CompleteWeekends>
//      <IdenticalShiftTypesDuringWeekend weight="1">true</IdenticalShiftTypesDuringWeekend>
//      <NoNightShiftBeforeFreeWeekend weight="0">false</NoNightShiftBeforeFreeWeekend>
//      <AlternativeSkillCategory weight="0">false</AlternativeSkillCategory>


                List<Element> unwantedPatternElementList = (List<Element>) element.getChild("UnwantedPatterns")
                        .getChildren();
                for (Element patternElement : unwantedPatternElementList) {
                    assertElementName(patternElement, "Pattern");
                    ShiftPattern shiftPattern = shiftPatternMap.get(patternElement.getText());
                    if (shiftPattern == null) {
                        throw new IllegalArgumentException("The shiftPattern (" + patternElement.getText()
                                + ") of contract (" + contract.getCode() + ") does not exist.");
                    }
                    // TODO unwanted shiftPattern
//      <UnwantedPatterns>
//        <Pattern>0</Pattern>
//        <Pattern>1</Pattern>
//        <Pattern>2</Pattern>
//      </UnwantedPatterns>


                }

                contractList.add(contract);
                contractMap.put(contract.getCode(), contract);
                id++;
            }
            nurseRoster.setContractList(contractList);
        }

        private void readEmployeeList(NurseRoster nurseRoster, Element employeesElement) throws JDOMException {
            List<Element> employeeElementList = (List<Element>) employeesElement.getChildren();
            List<Employee> employeeList = new ArrayList<Employee>(employeeElementList.size());
            employeeMap = new HashMap<String, Employee>(employeeElementList.size());
            long id = 0L;
            List<SkillProficiency> skillProficiencyList
                    = new ArrayList<SkillProficiency>(employeeElementList.size() * 2);
            long skillProficiencyId = 0L;
            for (Element element : employeeElementList) {
                assertElementName(element, "Employee");
                Employee employee = new Employee();
                employee.setId(id);
                employee.setCode(element.getAttribute("ID").getValue());
                employee.setName(element.getChild("Name").getText());
                Element contractElement = element.getChild("ContractID");
                Contract contract = contractMap.get(contractElement.getText());
                if (contract == null) {
                    throw new IllegalArgumentException("The contract (" + contractElement.getText()
                            + ") of employee (" + employee.getCode() + ") does not exist.");
                }
                employee.setContract(contract);

                Element skillsElement = element.getChild("Skills");
                if (skillsElement != null) {
                    List<Element> skillElementList = (List<Element>) skillsElement.getChildren();
                    for (Element skillElement : skillElementList) {
                        assertElementName(skillElement, "Skill");
                        Skill skill = skillMap.get(skillElement.getText());
                        if (skill == null) {
                            throw new IllegalArgumentException("The skill (" + skillElement.getText()
                                    + ") of employee (" + employee.getCode() + ") does not exist.");
                        }
                        SkillProficiency skillProficiency = new SkillProficiency();
                        skillProficiency.setId(skillProficiencyId);
                        skillProficiency.setEmployee(employee);
                        skillProficiency.setSkill(skill);
                        skillProficiencyList.add(skillProficiency);
                        skillProficiencyId++;
                    }
                }

                employeeList.add(employee);
                employeeMap.put(employee.getCode(), employee);
                id++;
            }
            nurseRoster.setEmployeeList(employeeList);
            nurseRoster.setSkillProficiencyList(skillProficiencyList);
        }

        private void readDayOffRequestList(NurseRoster nurseRoster, Element dayOffRequestsElement) throws JDOMException {
            if (dayOffRequestsElement == null) {
                return;
            }
            List<Element> dayOffElementList = (List<Element>) dayOffRequestsElement.getChildren();
            List<DayOffRequest> dayOffRequestList = new ArrayList<DayOffRequest>(dayOffElementList.size());
            long id = 0L;
            for (Element element : dayOffElementList) {
                assertElementName(element, "DayOff");
                DayOffRequest dayOffRequest = new DayOffRequest();
                dayOffRequest.setId(id);
                
                Element employeeElement = element.getChild("EmployeeID");
                Employee employee = employeeMap.get(employeeElement.getText());
                if (employee == null) {
                    throw new IllegalArgumentException("The shiftDate (" + employeeElement.getText()
                            + ") of dayOffRequest (" + dayOffRequest + ") does not exist.");
                }
                dayOffRequest.setEmployee(employee);

                Element dateElement = element.getChild("Date");
                ShiftDate shiftDate = shiftDateMap.get(dateElement.getText());
                if (shiftDate == null) {
                    throw new IllegalArgumentException("The date (" + dateElement.getText()
                            + ") of dayOffRequest (" + dayOffRequest + ") does not exist.");
                }
                dayOffRequest.setShiftDate(shiftDate);

                dayOffRequest.setWeight(element.getAttribute("weight").getIntValue());

                dayOffRequestList.add(dayOffRequest);
                id++;
            }
            nurseRoster.setDayOffRequestList(dayOffRequestList);
        }

        private void readDayOnRequestList(NurseRoster nurseRoster, Element dayOnRequestsElement) throws JDOMException {
            if (dayOnRequestsElement == null) {
                return;
            }
            List<Element> dayOnElementList = (List<Element>) dayOnRequestsElement.getChildren();
            List<DayOnRequest> dayOnRequestList = new ArrayList<DayOnRequest>(dayOnElementList.size());
            long id = 0L;
            for (Element element : dayOnElementList) {
                assertElementName(element, "DayOn");
                DayOnRequest dayOnRequest = new DayOnRequest();
                dayOnRequest.setId(id);

                Element employeeElement = element.getChild("EmployeeID");
                Employee employee = employeeMap.get(employeeElement.getText());
                if (employee == null) {
                    throw new IllegalArgumentException("The shiftDate (" + employeeElement.getText()
                            + ") of dayOnRequest (" + dayOnRequest + ") does not exist.");
                }
                dayOnRequest.setEmployee(employee);

                Element dateElement = element.getChild("Date");
                ShiftDate shiftDate = shiftDateMap.get(dateElement.getText());
                if (shiftDate == null) {
                    throw new IllegalArgumentException("The date (" + dateElement.getText()
                            + ") of dayOnRequest (" + dayOnRequest + ") does not exist.");
                }
                dayOnRequest.setShiftDate(shiftDate);

                dayOnRequest.setWeight(element.getAttribute("weight").getIntValue());

                dayOnRequestList.add(dayOnRequest);
                id++;
            }
            nurseRoster.setDayOnRequestList(dayOnRequestList);
        }

    }

}
