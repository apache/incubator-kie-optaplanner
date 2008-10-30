package org.drools.solver.examples.itc2007.curriculumcourse.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.WorkingMemory;
import org.drools.runtime.rule.FactHandle;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Course;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Period;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Room;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.UnavailablePeriodConstraint;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseStartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    @Override
    public boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope) {
        CurriculumCourseSchedule schedule = (CurriculumCourseSchedule) localSearchSolverScope.getWorkingSolution();
        return schedule.isInitialized();
    }

    public void initializeSolution(LocalSearchSolverScope localSearchSolverScope) {
        CurriculumCourseSchedule schedule = (CurriculumCourseSchedule) localSearchSolverScope.getWorkingSolution();
        initializeLectureList(localSearchSolverScope, schedule);
    }

    private void initializeLectureList(LocalSearchSolverScope localSearchSolverScope,
            CurriculumCourseSchedule schedule) {
        List<Period> periodList = schedule.getPeriodList();
        List<Room> roomList = schedule.getRoomList();
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();

        List<Lecture> lectureList = createLectureList(schedule);
        for (Lecture lecture : lectureList) {
            double unscheduledScore = localSearchSolverScope.calculateScoreFromWorkingMemory();
            FactHandle lectureHandle = null;

            List<PeriodScoring> periodScoringList = new ArrayList<PeriodScoring>(periodList.size());
            for (Period period : periodList) {
                if (lectureHandle == null) {
                    lecture.setPeriod(period);
                    lectureHandle = workingMemory.insert(lecture);
                } else {
                    workingMemory.modifyRetract(lectureHandle);
                    lecture.setPeriod(period);
                    workingMemory.modifyInsert(lectureHandle, lecture);
                }
                double score = localSearchSolverScope.calculateScoreFromWorkingMemory();
                periodScoringList.add(new PeriodScoring(period, score));
            }
            Collections.sort(periodScoringList);

            boolean almostPerfectMatch = false;
            double bestScore = Double.NEGATIVE_INFINITY;
            Period bestPeriod = null;
            Room bestRoom = null;
            for (PeriodScoring periodScoring : periodScoringList) {
                if (bestScore >= periodScoring.getScore()) {
                    // No need to check the rest
                    break;
                }
                workingMemory.modifyRetract(lectureHandle);
                lecture.setPeriod(periodScoring.getPeriod());
                workingMemory.modifyInsert(lectureHandle, lecture);

                for (Room room : roomList) {
                    workingMemory.modifyRetract(lectureHandle);
                    lecture.setRoom(room);
                    workingMemory.modifyInsert(lectureHandle, lecture);
                    double score = localSearchSolverScope.calculateScoreFromWorkingMemory();
                    if (score < unscheduledScore) {
                        if (score > bestScore) {
                            bestScore = score;
                            bestPeriod = periodScoring.getPeriod();
                            bestRoom = room;
                        }
                    } else if (score >= unscheduledScore) {
                        // TODO due to the score rules, the score can unscheduledScore can be higher than the score
                        // In theory every possibility should be looked into
                        almostPerfectMatch = true;
                        break;
                    }
                }
                if (almostPerfectMatch) {
                    break;
                }
            }
            if (!almostPerfectMatch) {
                if (bestPeriod == null || bestRoom == null) {
                    throw new IllegalStateException("The bestPeriod (" + bestPeriod + ") or the bestRoom ("
                            + bestRoom + ") cannot be null.");
                }
                workingMemory.modifyRetract(lectureHandle);
                lecture.setPeriod(bestPeriod);
                lecture.setRoom(bestRoom);
                workingMemory.modifyInsert(lectureHandle, lecture);
            }
            logger.debug("    Lecture ({}) initialized for starting solution.", lecture);
        }

        Collections.sort(lectureList, new PersistableIdComparator());
        schedule.setLectureList(lectureList);
    }

    public List<Lecture> createLectureList(CurriculumCourseSchedule schedule) {
        List<Course> courseList = schedule.getCourseList();
        List<CourseInitializationWeight> courseInitializationWeightList
                = new ArrayList<CourseInitializationWeight>(courseList.size());
        for (Course course : courseList) {
            courseInitializationWeightList.add(new CourseInitializationWeight(schedule, course));
        }
        Collections.sort(courseInitializationWeightList);

        List<Lecture> lectureList = new ArrayList<Lecture>(courseList.size() * 5);
        int lectureId = 0;
        for (CourseInitializationWeight courseInitializationWeight : courseInitializationWeightList) {
            Course course = courseInitializationWeight.getCourse();
            for (int i = 0; i < course.getLectureSize(); i++) {
                Lecture lecture = new Lecture();
                lecture.setId((long) lectureId);
                lecture.setCourse(course);
                lecture.setLectureIndexInCourse(i);
                lectureList.add(lecture);
                lectureId++;
            }
        }
        return lectureList;
    }

    private class CourseInitializationWeight implements Comparable<CourseInitializationWeight> {

        private Course course;
        private int unavailablePeriodConstraintCount;

        private CourseInitializationWeight(CurriculumCourseSchedule schedule, Course course) {
            this.course = course;
            unavailablePeriodConstraintCount = 0;
            // TODO this could be improved by iteration the unavailablePeriodConstraintList and using a hashmap
            for (UnavailablePeriodConstraint constraint : schedule.getUnavailablePeriodConstraintList()) {
                if (constraint.getCourse().equals(course)) {
                    unavailablePeriodConstraintCount++;
                }
            }
        }

        public Course getCourse() {
            return course;
        }

        public int compareTo(CourseInitializationWeight other) {

            return new CompareToBuilder()
                    .append(other.course.getCurriculumList().size(), course.getCurriculumList().size()) // Descending
                    .append(other.unavailablePeriodConstraintCount, unavailablePeriodConstraintCount) // Descending
                    .append(other.course.getLectureSize(), course.getLectureSize()) // Descending
                    .append(other.course.getStudentSize(), course.getStudentSize()) // Descending
                    .append(other.course.getMinWorkingDaySize(), course.getMinWorkingDaySize()) // Descending
                    .append(course.getId(), other.course.getId()) // Ascending
                    .toComparison();
        }

    }

    private class PeriodScoring implements Comparable<PeriodScoring> {

        private Period period;
        private double score;

        private PeriodScoring(Period period, double score) {
            this.period = period;
            this.score = score;
        }

        public Period getPeriod() {
            return period;
        }

        public double getScore() {
            return score;
        }

        public int compareTo(PeriodScoring other) {
            return -new CompareToBuilder().append(score, other.score).toComparison();
        }

    }

}