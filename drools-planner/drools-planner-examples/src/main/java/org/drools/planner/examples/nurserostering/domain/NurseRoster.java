package org.drools.planner.examples.nurserostering.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.core.score.HardAndSoftScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("NurseRoster")
public class NurseRoster extends AbstractPersistable implements Solution {

    private String code;

    private List<Skill> skillList;
    private List<ShiftType> shiftTypeList;
    private List<ShiftTypeSkillRequirement> shiftTypeSkillRequirementList;
    private List<Pattern> patternList;
    private List<Contract> contractList;
    private List<Employee> employeeList;
    private List<SkillProficiency> skillProficiencyList;
    private List<ShiftDate> shiftDateList;
    private List<Shift> shiftList;
    private List<DayOffRequest> dayOffRequestList;
    private List<DayOnRequest> dayOnRequestList;
    private List<ShiftOffRequest> shiftOffRequestList;
    private List<ShiftOnRequest> shiftOnRequestList;

    private List<NurseAssignment> nurseAssignmentList;

    private HardAndSoftScore score;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Skill> getSkillList() {
        return skillList;
    }

    public void setSkillList(List<Skill> skillList) {
        this.skillList = skillList;
    }

    public List<ShiftType> getShiftTypeList() {
        return shiftTypeList;
    }

    public void setShiftTypeList(List<ShiftType> shiftTypeList) {
        this.shiftTypeList = shiftTypeList;
    }

    public List<ShiftTypeSkillRequirement> getShiftTypeSkillRequirementList() {
        return shiftTypeSkillRequirementList;
    }

    public void setShiftTypeSkillRequirementList(List<ShiftTypeSkillRequirement> shiftTypeSkillRequirementList) {
        this.shiftTypeSkillRequirementList = shiftTypeSkillRequirementList;
    }

    public List<Pattern> getPatternList() {
        return patternList;
    }

    public void setPatternList(List<Pattern> patternList) {
        this.patternList = patternList;
    }

    public List<Contract> getContractList() {
        return contractList;
    }

    public void setContractList(List<Contract> contractList) {
        this.contractList = contractList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<SkillProficiency> getSkillProficiencyList() {
        return skillProficiencyList;
    }

    public void setSkillProficiencyList(List<SkillProficiency> skillProficiencyList) {
        this.skillProficiencyList = skillProficiencyList;
    }

    public List<ShiftDate> getShiftDateList() {
        return shiftDateList;
    }

    public void setShiftDateList(List<ShiftDate> shiftDateList) {
        this.shiftDateList = shiftDateList;
    }

    public List<Shift> getShiftList() {
        return shiftList;
    }

    public void setShiftList(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    public List<DayOffRequest> getDayOffRequestList() {
        return dayOffRequestList;
    }

    public void setDayOffRequestList(List<DayOffRequest> dayOffRequestList) {
        this.dayOffRequestList = dayOffRequestList;
    }

    public List<DayOnRequest> getDayOnRequestList() {
        return dayOnRequestList;
    }

    public void setDayOnRequestList(List<DayOnRequest> dayOnRequestList) {
        this.dayOnRequestList = dayOnRequestList;
    }

    public List<ShiftOffRequest> getShiftOffRequestList() {
        return shiftOffRequestList;
    }

    public void setShiftOffRequestList(List<ShiftOffRequest> shiftOffRequestList) {
        this.shiftOffRequestList = shiftOffRequestList;
    }

    public List<ShiftOnRequest> getShiftOnRequestList() {
        return shiftOnRequestList;
    }

    public void setShiftOnRequestList(List<ShiftOnRequest> shiftOnRequestList) {
        this.shiftOnRequestList = shiftOnRequestList;
    }

    public List<NurseAssignment> getNurseAssignmentList() {
        return nurseAssignmentList;
    }

    public void setNurseAssignmentList(List<NurseAssignment> nurseAssignmentList) {
        this.nurseAssignmentList = nurseAssignmentList;
    }

    public HardAndSoftScore getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = (HardAndSoftScore) score;
    }


    public boolean isInitialized() {
        return (nurseAssignmentList != null);
    }

    public Collection<? extends Object> getFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(skillList);
        facts.addAll(shiftTypeList);
        facts.addAll(shiftTypeSkillRequirementList);
        facts.addAll(patternList);
        facts.addAll(contractList);
        facts.addAll(employeeList);
        facts.addAll(skillProficiencyList);
        facts.addAll(shiftDateList);
        facts.addAll(shiftList);
        facts.addAll(dayOffRequestList);
        facts.addAll(dayOnRequestList);
        facts.addAll(shiftOffRequestList);
        facts.addAll(shiftOnRequestList);
        // TODO add more properties


        if (isInitialized()) {
            facts.addAll(nurseAssignmentList);
        }
        return facts;
    }

    /**
     * Clone will only deep copy the nurseAssignmentList
     */
    public NurseRoster cloneSolution() {
        NurseRoster clone = new NurseRoster();
        clone.id = id;
        clone.code = code;
        clone.skillList = skillList;
        clone.shiftTypeList = shiftTypeList;
        clone.shiftTypeSkillRequirementList = shiftTypeSkillRequirementList;
        clone.patternList = patternList;
        clone.contractList = contractList;
        clone.employeeList = employeeList;
        clone.skillProficiencyList = skillProficiencyList;
        clone.shiftDateList = shiftDateList;
        clone.shiftList = shiftList;
        clone.dayOffRequestList = dayOffRequestList;
        clone.dayOnRequestList = dayOnRequestList;
        clone.shiftOffRequestList = shiftOffRequestList;
        clone.shiftOnRequestList = shiftOnRequestList;
        // TODO add more properties


        // deep clone lectures
        List<NurseAssignment> clonedLectureList = new ArrayList<NurseAssignment>(nurseAssignmentList.size());
        for (NurseAssignment nurseAssignment : nurseAssignmentList) {
            NurseAssignment clonedNurseAssignment = nurseAssignment.clone();
            clonedLectureList.add(clonedNurseAssignment);
        }
        clone.nurseAssignmentList = clonedLectureList;
        clone.score = score;
        return clone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (id == null || !(o instanceof NurseRoster)) {
            return false;
        } else {
            NurseRoster other = (NurseRoster) o;
            if (nurseAssignmentList.size() != other.nurseAssignmentList.size()) {
                return false;
            }
            for (Iterator<NurseAssignment> it = nurseAssignmentList.iterator(), otherIt = other.nurseAssignmentList.iterator(); it.hasNext();) {
                NurseAssignment nurseAssignment = it.next();
                NurseAssignment otherNurseAssignment = otherIt.next();
                // Notice: we don't use equals()
                if (!nurseAssignment.solutionEquals(otherNurseAssignment)) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (NurseAssignment nurseAssignment : nurseAssignmentList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(nurseAssignment.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }

}
