package org.drools.solver.examples.itc2007.examination.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Examination extends AbstractPersistable implements Solution {

    private List<Student> studentList;
    private List<Period> periodList;
    private List<Room> roomList;

    private List<Exam> examList;

    private List<PeriodHardConstraint> periodHardConstraintList;
    private List<RoomHardConstraint> roomHardConstraintList;
    private InstitutionalWeighting institutionalWeighting;

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public List<Period> getPeriodList() {
        return periodList;
    }

    public void setPeriodList(List<Period> periodList) {
        this.periodList = periodList;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public List<Exam> getExamList() {
        return examList;
    }

    public void setExamList(List<Exam> examList) {
        this.examList = examList;
    }

    public List<PeriodHardConstraint> getPeriodHardConstraintList() {
        return periodHardConstraintList;
    }

    public void setPeriodHardConstraintList(List<PeriodHardConstraint> periodHardConstraintList) {
        this.periodHardConstraintList = periodHardConstraintList;
    }

    public List<RoomHardConstraint> getRoomHardConstraintList() {
        return roomHardConstraintList;
    }

    public void setRoomHardConstraintList(List<RoomHardConstraint> roomHardConstraintList) {
        this.roomHardConstraintList = roomHardConstraintList;
    }

    public InstitutionalWeighting getInstitutionalWeighting() {
        return institutionalWeighting;
    }

    public void setInstitutionalWeighting(InstitutionalWeighting institutionalWeighting) {
        this.institutionalWeighting = institutionalWeighting;
    }


    public Collection<? extends Object> getFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(studentList);
        facts.addAll(periodList);
        facts.addAll(roomList);
        facts.addAll(examList);
        facts.addAll(periodHardConstraintList);
        facts.addAll(roomHardConstraintList);
        facts.add(institutionalWeighting);
        return facts;
    }

    /**
     * Clone will only deep copy the exams
     */
    public Examination cloneSolution() {
        Examination clone = new Examination();
        clone.studentList = studentList;
        clone.periodList = periodList;
        clone.roomList = roomList;
        List<Exam> clonedExamList = new ArrayList<Exam>(examList.size());
        Map<Long, Exam> idToClonedExamMap = new HashMap<Long, Exam>(examList.size());
        for (Exam exam : examList) {
            Exam clonedExam = exam.clone();
            clonedExamList.add(clonedExam);
            idToClonedExamMap.put(clonedExam.getId(), clonedExam);
        }
        clone.examList = clonedExamList;
        List<PeriodHardConstraint> clonedPeriodHardConstraintList = new ArrayList<PeriodHardConstraint>(
                periodHardConstraintList.size());
        for (PeriodHardConstraint periodHardConstraint : periodHardConstraintList) {
            clonedPeriodHardConstraintList.add(periodHardConstraint.clone(idToClonedExamMap));
        }
        clone.periodHardConstraintList = clonedPeriodHardConstraintList;
        List<RoomHardConstraint> clonedRoomHardConstraintList = new ArrayList<RoomHardConstraint>(
                roomHardConstraintList.size());
        for (RoomHardConstraint roomHardConstraint : roomHardConstraintList) {
            clonedRoomHardConstraintList.add(roomHardConstraint.clone(idToClonedExamMap));
        }
        clone.roomHardConstraintList = clonedRoomHardConstraintList;
        clone.institutionalWeighting = institutionalWeighting;
        return clone;
    }

//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (id == null || !(o instanceof Examination)) {
//            return false;
//        } else {
//            Examination other = (Examination) o;
//            if (matchList.size() != other.matchList.size()) {
//                return false;
//            }
//            for (Iterator<Match> it = matchList.iterator(), otherIt = other.matchList.iterator(); it.hasNext();) {
//                Match match = it.next();
//                Match otherMatch = otherIt.next();
//                // Not delegated to a custom Match.equals(o) so Matches can be fetched from the WorkingMemory's HashSet
//                if (!match.getId().equals(otherMatch.getId()) || !match.getDay().equals(otherMatch.getDay())) {
//                    return false;
//                }
//            }
//            return true;
//        }
//    }
//
//    public int hashCode() {
//        int hashCode = 0;
//        for (Match match : matchList) {
//            // Not delegated to a custom Match.hashCode() so Matches can be fetched from the WorkingMemory's HashSet
//            hashCode = (hashCode * 31 + match.getId().hashCode()) * 31 + match.getDay().hashCode();
//        }
//        return hashCode;
//    }

}
