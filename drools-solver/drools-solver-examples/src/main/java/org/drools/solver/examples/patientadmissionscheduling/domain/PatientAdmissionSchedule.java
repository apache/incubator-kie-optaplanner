package org.drools.solver.examples.patientadmissionscheduling.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.common.domain.AbstractPersistable;
import org.drools.solver.examples.itc2007.examination.domain.*;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionSchedule extends AbstractPersistable implements Solution {

    private List<Specialism> specialismList;
    private List<Department> departmentList;
    private List<DepartmentSpecialism> departmentSpecialismList;

    private List<Exam> examList;

    public List<Specialism> getSpecialismList() {
        return specialismList;
    }

    public void setSpecialismList(List<Specialism> specialismList) {
        this.specialismList = specialismList;
    }

    public List<Department> getDepartmentList() {
        return departmentList;
    }

    public void setDepartmentList(List<Department> departmentList) {
        this.departmentList = departmentList;
    }

    public boolean isInitialized() {
        return (examList != null);
    }

    public List<DepartmentSpecialism> getDepartmentSpecialismList() {
        return departmentSpecialismList;
    }

    public void setDepartmentSpecialismList(List<DepartmentSpecialism> departmentSpecialismList) {
        this.departmentSpecialismList = departmentSpecialismList;
    }

    public Collection<? extends Object> getFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(specialismList);
        facts.addAll(departmentList);
        facts.addAll(departmentSpecialismList);


//        facts.addAll(roomList);
//        facts.addAll(periodHardConstraintList);
//        facts.addAll(roomHardConstraintList);
        if (isInitialized()) {
            facts.addAll(examList);
        }
        return facts;
    }

    /**
     * Clone will only deep copy the exams
     */
    public PatientAdmissionSchedule cloneSolution() {
        PatientAdmissionSchedule clone = new PatientAdmissionSchedule();
        clone.id = id;
        clone.specialismList = specialismList;
        clone.departmentList = departmentList;
        clone.departmentSpecialismList = departmentSpecialismList;

        
//        clone.roomList = roomList;
//        clone.periodHardConstraintList = periodHardConstraintList;
//        clone.roomHardConstraintList = roomHardConstraintList;
        // deep clone exams
        List<Exam> clonedExamList = new ArrayList<Exam>(examList.size());
        for (Exam exam : examList) {
            Exam clonedExam = exam.clone();
            clonedExamList.add(clonedExam);
        }
        clone.examList = clonedExamList;
        return clone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (id == null || !(o instanceof PatientAdmissionSchedule)) {
            return false;
        } else {
            PatientAdmissionSchedule other = (PatientAdmissionSchedule) o;
            if (examList.size() != other.examList.size()) {
                return false;
            }
            for (Iterator<Exam> it = examList.iterator(), otherIt = other.examList.iterator(); it.hasNext();) {
                Exam exam = it.next();
                Exam otherExam = otherIt.next();
                // Notice: we don't use equals()
                if (!exam.solutionEquals(otherExam)) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (Exam exam : examList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(exam.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }

}