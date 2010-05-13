package org.drools.planner.examples.nurserostering.persistence;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.AbstractXmlSolutionImporter;
import org.drools.planner.examples.nurserostering.domain.DayOfWeek;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Pattern;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;
import org.drools.planner.examples.nurserostering.domain.ShiftType;
import org.drools.planner.examples.nurserostering.domain.ShiftTypeSkillRequirement;
import org.drools.planner.examples.nurserostering.domain.Skill;
import org.drools.planner.examples.nurserostering.domain.SkillProficiency;
import org.drools.planner.examples.nurserostering.domain.contract.BooleanContractLine;
import org.drools.planner.examples.nurserostering.domain.contract.Contract;
import org.drools.planner.examples.nurserostering.domain.contract.ContractLine;
import org.drools.planner.examples.nurserostering.domain.contract.ContractLineType;
import org.drools.planner.examples.nurserostering.domain.contract.MinMaxContractLine;
import org.drools.planner.examples.nurserostering.domain.request.DayOffRequest;
import org.drools.planner.examples.nurserostering.domain.request.DayOnRequest;
import org.drools.planner.examples.nurserostering.domain.request.ShiftOffRequest;
import org.drools.planner.examples.nurserostering.domain.request.ShiftOnRequest;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringSolutionImporter extends AbstractXmlSolutionImporter {

    public static void main(String[] args) {
        new NurseRosteringSolutionImporter().convertAll();
    }

    public NurseRosteringSolutionImporter() {
        super(new NurseRosteringDaoImpl());
    }

    public XmlInputBuilder createXmlInputBuilder() {
        return new NurseRosteringInputBuilder();
    }

    public class NurseRosteringInputBuilder extends XmlInputBuilder {

        protected Map<String, ShiftDate> shiftDateMap;
        protected Map<String, Skill> skillMap;
        protected Map<String, ShiftType> shiftTypeMap;
        protected Map<List<String>, Shift> dateAndShiftTypeToShiftMap;
        protected Map<List<Object>, List<Shift>> dayOfWeekAndShiftTypeToShiftListMap;
        protected Map<String, Pattern> patternMap;
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
            generateShiftList(nurseRoster);
            readPatternList(nurseRoster, schedulingPeriodElement.getChild("Patterns"));
            readContractList(nurseRoster, schedulingPeriodElement.getChild("Contracts"));
            readEmployeeList(nurseRoster, schedulingPeriodElement.getChild("Employees"));
            readRequiredEmployeeSizes(nurseRoster, schedulingPeriodElement.getChild("CoverRequirements"));
            readDayOffRequestList(nurseRoster, schedulingPeriodElement.getChild("DayOffRequests"));
            readDayOnRequestList(nurseRoster, schedulingPeriodElement.getChild("DayOnRequests"));
            readShiftOffRequestList(nurseRoster, schedulingPeriodElement.getChild("ShiftOffRequests"));
            readShiftOnRequestList(nurseRoster, schedulingPeriodElement.getChild("ShiftOnRequests"));

            logger.info("NurseRoster {} with {} skills, {} shiftTypes, {} patterns, {} contracts, {} employees," +
                    " {} shiftDates, {} shifts and {} requests.",
                    new Object[]{nurseRoster.getCode(),
                        nurseRoster.getSkillList().size(),
                        nurseRoster.getShiftTypeList().size(),
                        nurseRoster.getPatternList().size(),
                        nurseRoster.getContractList().size(),
                        nurseRoster.getEmployeeList().size(),
                        nurseRoster.getShiftDateList().size(),
                        nurseRoster.getShiftList().size(),
                        nurseRoster.getDayOffRequestList().size() + nurseRoster.getDayOnRequestList().size()
                            + nurseRoster.getShiftOffRequestList().size() + nurseRoster.getShiftOnRequestList().size()
                    });

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
                shiftDate.setShiftList(new ArrayList<Shift>());
                shiftDateList.add(shiftDate);
                shiftDateMap.put(dateString, shiftDate);
                id++;
                dayIndex++;
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            nurseRoster.setShiftDateList(shiftDateList);
        }

        private void readSkillList(NurseRoster nurseRoster, Element skillsElement) throws JDOMException {
            List<Skill> skillList;
            if (skillsElement == null) {
                skillList = Collections.emptyList();
            } else {
                List<Element> skillElementList = (List<Element>) skillsElement.getChildren();
                skillList = new ArrayList<Skill>(skillElementList.size());
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
            }
            nurseRoster.setSkillList(skillList);
        }

        private void readShiftTypeList(NurseRoster nurseRoster, Element shiftTypesElement) throws JDOMException {
            List<Element> shiftTypeElementList = (List<Element>) shiftTypesElement.getChildren();
            List<ShiftType> shiftTypeList = new ArrayList<ShiftType>(shiftTypeElementList.size());
            shiftTypeMap = new HashMap<String, ShiftType>(shiftTypeElementList.size());
            long id = 0L;
            int index = 0;
            List<ShiftTypeSkillRequirement> shiftTypeSkillRequirementList
                    = new ArrayList<ShiftTypeSkillRequirement>(shiftTypeElementList.size() * 2);
            long shiftTypeSkillRequirementId = 0L;
            for (Element element : shiftTypeElementList) {
                assertElementName(element, "Shift");
                ShiftType shiftType = new ShiftType();
                shiftType.setId(id);
                shiftType.setCode(element.getAttribute("ID").getValue());
                shiftType.setIndex(index);
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
                index++;
            }
            nurseRoster.setShiftTypeList(shiftTypeList);
            nurseRoster.setShiftTypeSkillRequirementList(shiftTypeSkillRequirementList);
        }

        private void generateShiftList(NurseRoster nurseRoster) throws JDOMException {
            List<ShiftType> shiftTypeList = nurseRoster.getShiftTypeList();
            int shiftListSize = shiftDateMap.size() * shiftTypeList.size();
            List<Shift> shiftList = new ArrayList<Shift>(shiftListSize);
            dateAndShiftTypeToShiftMap = new HashMap<List<String>, Shift>(shiftListSize);
            dayOfWeekAndShiftTypeToShiftListMap = new HashMap<List<Object>, List<Shift>>(7 * shiftTypeList.size());
            long id = 0L;
            int index = 0;
            for (ShiftDate shiftDate : nurseRoster.getShiftDateList()) {
                for (ShiftType shiftType : shiftTypeList) {
                    Shift shift = new Shift();
                    shift.setId(id);
                    shift.setShiftDate(shiftDate);
                    shiftDate.getShiftList().add(shift);
                    shift.setShiftType(shiftType);
                    shift.setIndex(index);
                    shift.setRequiredEmployeeSize(0); // Filled in later
                    shiftList.add(shift);
                    dateAndShiftTypeToShiftMap.put(Arrays.asList(shiftDate.getDateString(), shiftType.getCode()), shift);
                    addShiftToDayOfWeekAndShiftTypeToShiftListMap(shiftDate, shiftType, shift);
                    id++;
                    index++;
                }
            }
            nurseRoster.setShiftList(shiftList);
        }

        private void addShiftToDayOfWeekAndShiftTypeToShiftListMap(ShiftDate shiftDate, ShiftType shiftType,
                Shift shift) {
            List<Object> key = Arrays.<Object>asList(shiftDate.getDayOfWeek(), shiftType);
            List<Shift> dayOfWeekAndShiftTypeToShiftList = dayOfWeekAndShiftTypeToShiftListMap.get(key);
            if (dayOfWeekAndShiftTypeToShiftList == null) {
                dayOfWeekAndShiftTypeToShiftList = new ArrayList<Shift>((shiftDateMap.size() + 6) / 7);
                dayOfWeekAndShiftTypeToShiftListMap.put(key, dayOfWeekAndShiftTypeToShiftList);
            }
            dayOfWeekAndShiftTypeToShiftList.add(shift);
        }

        private void readPatternList(NurseRoster nurseRoster, Element patternsElement) throws JDOMException {
            List<Pattern> patternList;
            if (patternsElement == null) {
                patternList = Collections.emptyList();
            } else {
                List<Element> patternElementList = (List<Element>) patternsElement.getChildren();
                patternList = new ArrayList<Pattern>(patternElementList.size());
                patternMap = new HashMap<String, Pattern>(patternElementList.size());
                long id = 0L;
                for (Element element : patternElementList) {
                    assertElementName(element, "Pattern");
                    Pattern pattern = new Pattern();
                    pattern.setId(id);
                    pattern.setCode(element.getAttribute("ID").getValue());
                    pattern.setWeight(element.getAttribute("weight").getIntValue());

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
                                        + ") of pattern (" + pattern.getCode() + ") does not exist.");
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

                    patternList.add(pattern);
                    patternMap.put(pattern.getCode(), pattern);
                    id++;
                }
            }
            nurseRoster.setPatternList(patternList);
        }

        private void readContractList(NurseRoster nurseRoster, Element contractsElement) throws JDOMException {
            int contractLineTypeListSize = ContractLineType.values().length;
            List<Element> contractElementList = (List<Element>) contractsElement.getChildren();
            List<Contract> contractList = new ArrayList<Contract>(contractElementList.size());
            contractMap = new HashMap<String, Contract>(contractElementList.size());
            long id = 0L;
            List<ContractLine> contractLineList = new ArrayList<ContractLine>(
                    contractElementList.size() * contractLineTypeListSize);
            long contractLineId = 0L;
            for (Element element : contractElementList) {
                assertElementName(element, "Contract");
                Contract contract = new Contract();
                contract.setId(id);
                contract.setCode(element.getAttribute("ID").getValue());
                contract.setDescription(element.getChild("Description").getText());

                List<ContractLine> contractLineListOfContract = new ArrayList<ContractLine>(contractLineTypeListSize);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("SingleAssignmentPerDay"),
                        ContractLineType.SINGLE_ASSIGNMENT_PER_DAY);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinNumAssignments"),
                        element.getChild("MaxNumAssignments"),
                        ContractLineType.TOTAL_ASSIGNMENTS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinConsecutiveWorkingDays"),
                        element.getChild("MaxConsecutiveWorkingDays"),
                        ContractLineType.CONSECUTIVE_WORKING_DAYS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinConsecutiveFreeDays"),
                        element.getChild("MaxConsecutiveFreeDays"),
                        ContractLineType.CONSECUTIVE_FREE_DAYS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinConsecutiveWorkingWeekends"),
                        element.getChild("MaxConsecutiveWorkingWeekends"),
                        ContractLineType.CONSECUTIVE_WORKING_WEEKENDS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, null,
                        element.getChild("MaxWorkingWeekendsInFourWeeks"),
                        ContractLineType.TOTAL_WORKING_WEEKENDS_IN_FOUR_WEEKS);
// TODO
//      <WeekendDefinition>SaturdaySunday</WeekendDefinition>
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("CompleteWeekends"),
                        ContractLineType.COMPLETE_WEEKENDS);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("IdenticalShiftTypesDuringWeekend"),
                        ContractLineType.IDENTICAL_SHIFT_TYPES_DURING_WEEKEND);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("NoNightShiftBeforeFreeWeekend"),
                        ContractLineType.NO_NIGHT_SHIFT_BEFORE_FREE_WEEKEND);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("AlternativeSkillCategory"),
                        ContractLineType.ALTERNATIVE_SKILL_CATEGORY);
                contract.setContractLineList(contractLineListOfContract);

                List<Element> unwantedPatternElementList = (List<Element>) element.getChild("UnwantedPatterns")
                        .getChildren();
                for (Element patternElement : unwantedPatternElementList) {
                    assertElementName(patternElement, "Pattern");
                    Pattern pattern = patternMap.get(patternElement.getText());
                    if (pattern == null) {
                        throw new IllegalArgumentException("The pattern (" + patternElement.getText()
                                + ") of contract (" + contract.getCode() + ") does not exist.");
                    }
                    // TODO unwanted pattern
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
            nurseRoster.setContractLineList(contractLineList);
        }

        private long readBooleanContractLine(Contract contract, List<ContractLine> contractLineList,
                List<ContractLine> contractLineListOfContract, long contractLineId, Element element,
                ContractLineType contractLineType) throws DataConversionException {
            boolean enabled = Boolean.valueOf(element.getText());
            int weight;
            if (enabled) {
                weight = element.getAttribute("weight").getIntValue();
                if (weight < 0) {
                    throw new IllegalArgumentException("The weight (" + weight
                            + ") of contract (" + contract.getCode() + ") and contractLineType (" + contractLineType
                            + ") should be 0 or at least 1.");
                } else if (weight == 0) {
                    // If the weight is zero, the constraint should not be considered.
                    enabled = false;
                    logger.warn("In contract ({}), the contractLineType ({}) is enabled with weight 0.",
                            contract.getCode(), contractLineType);
                }
            } else {
                weight = 0;
            }
            if (enabled) {
                BooleanContractLine contractLine = new BooleanContractLine();
                contractLine.setId(contractLineId);
                contractLine.setContract(contract);
                contractLine.setContractLineType(contractLineType);
                contractLine.setWeight(weight);
                contractLineList.add(contractLine);
                contractLineListOfContract.add(contractLine);
                contractLineId++;
            }
            return contractLineId;
        }

        private long readMinMaxContractLine(Contract contract, List<ContractLine> contractLineList,
                List<ContractLine> contractLineListOfContract, long contractLineId,
                Element minElement, Element maxElement,
                ContractLineType contractLineType) throws DataConversionException {
            boolean minimumEnabled = minElement == null ? false : minElement.getAttribute("on").getBooleanValue();
            int minimumWeight;
            if (minimumEnabled) {
                minimumWeight = minElement.getAttribute("weight").getIntValue();
                if (minimumWeight < 0) {
                    throw new IllegalArgumentException("The minimumWeight (" + minimumWeight
                            + ") of contract (" + contract.getCode() + ") and contractLineType (" + contractLineType
                            + ") should be 0 or at least 1.");
                } else if (minimumWeight == 0) {
                    // If the weight is zero, the constraint should not be considered.
                    minimumEnabled = false;
                    logger.warn("In contract ({}), the contractLineType ({}) minimum is enabled with weight 0.",
                            contract.getCode(), contractLineType);
                }
            } else {
                minimumWeight = 0;
            }
            boolean maximumEnabled = maxElement == null ? false : maxElement.getAttribute("on").getBooleanValue();
            int maximumWeight;
            if (maximumEnabled) {
                maximumWeight = maxElement.getAttribute("weight").getIntValue();
                if (maximumWeight < 0) {
                    throw new IllegalArgumentException("The maximumWeight (" + maximumWeight
                            + ") of contract (" + contract.getCode() + ") and contractLineType (" + contractLineType
                            + ") should be 0 or at least 1.");
                } else if (maximumWeight == 0) {
                    // If the weight is zero, the constraint should not be considered.
                    maximumEnabled = false;
                    logger.warn("In contract ({}), the contractLineType ({}) maximum is enabled with weight 0.",
                            contract.getCode(), contractLineType);
                }
            } else {
                maximumWeight = 0;
            }
            if (minimumEnabled || maximumEnabled) {
                MinMaxContractLine contractLine = new MinMaxContractLine();
                contractLine.setId(contractLineId);
                contractLine.setContract(contract);
                contractLine.setContractLineType(contractLineType);
                contractLine.setMinimumEnabled(minimumEnabled);
                if (minimumEnabled) {
                    int minimumValue = Integer.parseInt(minElement.getText());
                    if (minimumValue < 1) {
                        throw new IllegalArgumentException("The minimumValue (" + minimumValue
                                + ") of contract (" + contract.getCode() + ") should be at least 1.");
                    }
                    contractLine.setMinimumValue(minimumValue);
                    contractLine.setMinimumWeight(minimumWeight);
                }
                contractLine.setMaximumEnabled(maximumEnabled);
                if (maximumEnabled) {
                    int maximumValue = Integer.parseInt(maxElement.getText());
                    if (maximumValue < 1) {
                        throw new IllegalArgumentException("The maximumValue (" + maximumValue
                                + ") of contract (" + contract.getCode() + ") should be at least 1.");
                    }
                    contractLine.setMaximumValue(maximumValue);
                    contractLine.setMaximumWeight(maximumWeight);
                }
                contractLineList.add(contractLine);
                contractLineListOfContract.add(contractLine);
                contractLineId++;
            }
            return contractLineId;
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

        private void readRequiredEmployeeSizes(NurseRoster nurseRoster, Element coverRequirementsElement) {
            List<Element> coverRequirementElementList = (List<Element>) coverRequirementsElement.getChildren();
            for (Element element : coverRequirementElementList) {
                if (element.getName().equals("DayOfWeekCover")) {
                    Element dayOfWeekElement = element.getChild("Day");
                    DayOfWeek dayOfWeek = DayOfWeek.valueOfCode(dayOfWeekElement.getText());
                    if (dayOfWeek == null) {
                        throw new IllegalArgumentException("The dayOfWeek (" + dayOfWeekElement.getText()
                                + ") of an entity DayOfWeekCover does not exist.");
                    }

                    List<Element> coverElementList = (List<Element>) element.getChildren("Cover");
                    for (Element coverElement : coverElementList) {
                        Element shiftTypeElement = coverElement.getChild("Shift");
                        ShiftType shiftType = shiftTypeMap.get(shiftTypeElement.getText());
                        if (shiftType == null) {
                            if (shiftTypeElement.getText().equals("Any")) {
                                throw new IllegalStateException("The shiftType Any is not supported on DayOfWeekCover.");
                            } else if (shiftTypeElement.getText().equals("None")) {
                                throw new IllegalStateException("The shiftType None is not supported on DayOfWeekCover.");
                            } else {
                                throw new IllegalArgumentException("The shiftType (" + shiftTypeElement.getText()
                                        + ") of an entity DayOfWeekCover does not exist.");
                            }
                        }
                        List<Object> key = Arrays.<Object>asList(dayOfWeek, shiftType);
                        List<Shift> shiftList = dayOfWeekAndShiftTypeToShiftListMap.get(key);
                        if (shiftList == null) {
                            throw new IllegalArgumentException("The dayOfWeek (" + dayOfWeekElement.getText()
                                    + ") with the shiftType (" + shiftTypeElement.getText()
                                    + ") of an entity DayOfWeekCover does not have any shifts.");
                        }
                        int requiredEmployeeSize = Integer.parseInt(coverElement.getChild("Preferred").getText());
                        for (Shift shift : shiftList) {
                            shift.setRequiredEmployeeSize(shift.getRequiredEmployeeSize() + requiredEmployeeSize);
                        }
                    }
                } else if (element.getName().equals("DateSpecificCover")) {
                    Element dateElement = element.getChild("Date");
                    List<Element> coverElementList = (List<Element>) element.getChildren("Cover");
                    for (Element coverElement : coverElementList) {
                        Element shiftTypeElement = coverElement.getChild("Shift");
                        Shift shift = dateAndShiftTypeToShiftMap.get(Arrays.asList(dateElement.getText(), shiftTypeElement.getText()));
                        if (shift == null) {
                            throw new IllegalArgumentException("The date (" + dateElement.getText()
                                    + ") with the shiftType (" + shiftTypeElement.getText()
                                    + ") of an entity DateSpecificCover does not have a shift.");
                        }
                        int requiredEmployeeSize = Integer.parseInt(coverElement.getChild("Preferred").getText());
                        shift.setRequiredEmployeeSize(shift.getRequiredEmployeeSize() + requiredEmployeeSize);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown cover entity (" + element.getName() + ").");
                }
            }
        }

        private void readDayOffRequestList(NurseRoster nurseRoster, Element dayOffRequestsElement) throws JDOMException {
            List<DayOffRequest> dayOffRequestList;
            if (dayOffRequestsElement == null) {
                dayOffRequestList = Collections.emptyList();
            } else {
                List<Element> dayOffElementList = (List<Element>) dayOffRequestsElement.getChildren();
                dayOffRequestList = new ArrayList<DayOffRequest>(dayOffElementList.size());
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
            }
            nurseRoster.setDayOffRequestList(dayOffRequestList);
        }

        private void readDayOnRequestList(NurseRoster nurseRoster, Element dayOnRequestsElement) throws JDOMException {
            List<DayOnRequest> dayOnRequestList;
            if (dayOnRequestsElement == null) {
                dayOnRequestList = Collections.emptyList();
            } else {
                List<Element> dayOnElementList = (List<Element>) dayOnRequestsElement.getChildren();
                dayOnRequestList = new ArrayList<DayOnRequest>(dayOnElementList.size());
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
            }
            nurseRoster.setDayOnRequestList(dayOnRequestList);
        }

        private void readShiftOffRequestList(NurseRoster nurseRoster, Element shiftOffRequestsElement) throws JDOMException {
            List<ShiftOffRequest> shiftOffRequestList;
            if (shiftOffRequestsElement == null) {
                shiftOffRequestList = Collections.emptyList();
            } else {
                List<Element> shiftOffElementList = (List<Element>) shiftOffRequestsElement.getChildren();
                shiftOffRequestList = new ArrayList<ShiftOffRequest>(shiftOffElementList.size());
                long id = 0L;
                for (Element element : shiftOffElementList) {
                    assertElementName(element, "ShiftOff");
                    ShiftOffRequest shiftOffRequest = new ShiftOffRequest();
                    shiftOffRequest.setId(id);

                    Element employeeElement = element.getChild("EmployeeID");
                    Employee employee = employeeMap.get(employeeElement.getText());
                    if (employee == null) {
                        throw new IllegalArgumentException("The shift (" + employeeElement.getText()
                                + ") of shiftOffRequest (" + shiftOffRequest + ") does not exist.");
                    }
                    shiftOffRequest.setEmployee(employee);

                    Element dateElement = element.getChild("Date");
                    Element shiftTypeElement = element.getChild("ShiftTypeID");
                    Shift shift = dateAndShiftTypeToShiftMap.get(Arrays.asList(dateElement.getText(), shiftTypeElement.getText()));
                    if (shift == null) {
                        throw new IllegalArgumentException("The date (" + dateElement.getText()
                                + ") or the shiftType (" + shiftTypeElement.getText()
                                + ") of shiftOffRequest (" + shiftOffRequest + ") does not exist.");
                    }
                    shiftOffRequest.setShift(shift);

                    shiftOffRequest.setWeight(element.getAttribute("weight").getIntValue());

                    shiftOffRequestList.add(shiftOffRequest);
                    id++;
                }
            }
            nurseRoster.setShiftOffRequestList(shiftOffRequestList);
        }

        private void readShiftOnRequestList(NurseRoster nurseRoster, Element shiftOnRequestsElement) throws JDOMException {
            List<ShiftOnRequest> shiftOnRequestList;
            if (shiftOnRequestsElement == null) {
                shiftOnRequestList = Collections.emptyList();
            } else {
                List<Element> shiftOnElementList = (List<Element>) shiftOnRequestsElement.getChildren();
                shiftOnRequestList = new ArrayList<ShiftOnRequest>(shiftOnElementList.size());
                long id = 0L;
                for (Element element : shiftOnElementList) {
                    assertElementName(element, "ShiftOn");
                    ShiftOnRequest shiftOnRequest = new ShiftOnRequest();
                    shiftOnRequest.setId(id);

                    Element employeeElement = element.getChild("EmployeeID");
                    Employee employee = employeeMap.get(employeeElement.getText());
                    if (employee == null) {
                        throw new IllegalArgumentException("The shift (" + employeeElement.getText()
                                + ") of shiftOnRequest (" + shiftOnRequest + ") does not exist.");
                    }
                    shiftOnRequest.setEmployee(employee);

                    Element dateElement = element.getChild("Date");
                    Element shiftTypeElement = element.getChild("ShiftTypeID");
                    Shift shift = dateAndShiftTypeToShiftMap.get(Arrays.asList(dateElement.getText(), shiftTypeElement.getText()));
                    if (shift == null) {
                        throw new IllegalArgumentException("The date (" + dateElement.getText()
                                + ") or the shiftType (" + shiftTypeElement.getText()
                                + ") of shiftOnRequest (" + shiftOnRequest + ") does not exist.");
                    }
                    shiftOnRequest.setShift(shift);

                    shiftOnRequest.setWeight(element.getAttribute("weight").getIntValue());

                    shiftOnRequestList.add(shiftOnRequest);
                    id++;
                }
            }
            nurseRoster.setShiftOnRequestList(shiftOnRequestList);
        }

    }

}
