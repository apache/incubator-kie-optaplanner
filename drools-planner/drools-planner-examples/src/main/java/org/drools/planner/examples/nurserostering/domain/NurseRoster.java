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
    private List<ShiftPattern> shiftPatternList;
    private List<Contract> contractList;
    private List<Employee> employeeList;
    private List<SkillProficiency> skillProficiencyList;

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

    public List<ShiftPattern> getShiftPatternList() {
        return shiftPatternList;
    }

    public void setShiftPatternList(List<ShiftPattern> shiftPatternList) {
        this.shiftPatternList = shiftPatternList;
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
        facts.add(skillList);
        facts.addAll(shiftTypeList);
        facts.addAll(shiftTypeSkillRequirementList);
        facts.addAll(shiftPatternList);
        facts.addAll(contractList);
        facts.addAll(employeeList);
        facts.addAll(skillProficiencyList);
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
        clone.shiftPatternList = shiftPatternList;
        clone.contractList = contractList;
        clone.employeeList = employeeList;
        clone.skillProficiencyList = skillProficiencyList;
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
