package org.drools.planner.examples.lessonschedule.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.HardAndSoftScore;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class LessonSchedule extends AbstractPersistable implements Solution {

    private List<Timeslot> timeslotList;
    private List<Teacher> teacherList;
    private List<Group> groupList;

    private List<Lesson> lessonList;

    private HardAndSoftScore score;

    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public void setTimeslotList(List<Timeslot> timeslotList) {
        this.timeslotList = timeslotList;
    }

    public List<Teacher> getTeacherList() {
        return teacherList;
    }

    public void setTeacherList(List<Teacher> teacherList) {
        this.teacherList = teacherList;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    public List<Lesson> getLessonList() {
        return lessonList;
    }

    public void setLessonList(List<Lesson> lessonList) {
        this.lessonList = lessonList;
    }

    public HardAndSoftScore getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = (HardAndSoftScore) score;
    }


    public Collection<? extends Object> getFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(timeslotList);
        facts.addAll(teacherList);
        facts.addAll(groupList);
        facts.addAll(lessonList);
        return facts;
    }

    /**
     * Clone will only deep copy the lessonList
     */
    public LessonSchedule cloneSolution() {
        LessonSchedule clone = new LessonSchedule();
        clone.id = id;
        clone.timeslotList = timeslotList; // No Deep copy
        clone.teacherList = teacherList; // No Deep copy
        clone.groupList = groupList; // No Deep copy
        List<Lesson> clonedLessonList = new ArrayList<Lesson>(lessonList.size());
        for (Lesson lesson : lessonList) {
            clonedLessonList.add(lesson.clone());
        }
        clone.lessonList = clonedLessonList;
        clone.score = score;
        return clone;
    }
    
}
