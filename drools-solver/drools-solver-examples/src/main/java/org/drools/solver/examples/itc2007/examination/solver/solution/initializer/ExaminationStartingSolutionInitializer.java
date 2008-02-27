package org.drools.solver.examples.itc2007.examination.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraint;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraintType;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.domain.Topic;
import org.drools.solver.examples.itc2007.examination.domain.solver.ExamBefore;
import org.drools.solver.examples.itc2007.examination.domain.solver.ExamCoincidence;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationStartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    @Override
    public boolean isSolutionInitialized(Solution solution) {
        Examination examination = (Examination) solution;
        return examination.isInitialized();
    }

    public void initializeSolution(Solution solution) {
        Examination examination = (Examination) solution;
        initializeExamList(examination);
    }

    private void initializeExamList(Examination examination) {
        EvaluationHandler evaluationHandler = solver.getEvaluationHandler();
        List<Period> periodList = examination.getPeriodList();
        List<Room> roomList = examination.getRoomList();
        List<Exam> examList = new ArrayList<Exam>(examination.getTopicList().size());
        examination.setExamList(examList);
        evaluationHandler.setSolution(examination);
        WorkingMemory workingMemory = evaluationHandler.getStatefulSession();

        List<ExamInitialWeight> examInitialWeightList = createExamAssigningScoreList(examination);

        for (ExamInitialWeight examInitialWeight : examInitialWeightList) {
            double unscheduledScore = evaluationHandler.fireAllRulesAndCalculateStepScore();
            Exam leader = examInitialWeight.getExam();
            FactHandle leaderHandle = null;

            List<ExamToHandle> examToHandleList = new ArrayList<ExamToHandle>(5);
            if (leader.getExamCoincidence() == null) {
                examToHandleList.add(new ExamToHandle(leader));
            } else {
                for (Exam coincidenceExam : leader.getExamCoincidence().getCoincidenceExamSet()) {
                    examToHandleList.add(new ExamToHandle(coincidenceExam));
                }
            }
            
            List<PeriodScoring> periodScoringList = new ArrayList<PeriodScoring>(periodList.size());
            for (Period period : periodList) {
                for (ExamToHandle examToHandle : examToHandleList) {
                    if (examToHandle.getExamHandle() == null) {
                        examToHandle.getExam().setPeriod(period);
                        examToHandle.setExamHandle(workingMemory.insert(examToHandle.getExam()));
                        if (examToHandle.getExam().isCoincidenceLeader()) {
                            leaderHandle = examToHandle.getExamHandle();
                        }
                    } else {
                        workingMemory.modifyRetract(examToHandle.getExamHandle());
                        examToHandle.getExam().setPeriod(period);
                        workingMemory.modifyInsert(examToHandle.getExamHandle(), examToHandle.getExam());
                    }
                }
                double score = evaluationHandler.fireAllRulesAndCalculateStepScore();
                periodScoringList.add(new PeriodScoring(period, score));
            }
            Collections.sort(periodScoringList);

            scheduleLeader(periodScoringList, roomList, evaluationHandler, workingMemory, unscheduledScore,
                    examToHandleList, leader, leaderHandle);
            examList.add(leader);

            // Schedule the non leaders
            for (ExamToHandle examToHandle : examToHandleList) {
                Exam exam = examToHandle.getExam();
                // Leader already has a room
                if (!exam.isCoincidenceLeader()) {
                    scheduleNonLeader(roomList, evaluationHandler, workingMemory, exam, examToHandle.getExamHandle());
                    examList.add(exam);
                }
            }
        }
        Collections.sort(examList, new PersistableIdComparator());
        examination.setExamList(examList);
    }

    private void scheduleLeader(List<PeriodScoring> periodScoringList, List<Room> roomList,
            EvaluationHandler evaluationHandler, WorkingMemory workingMemory, double unscheduledScore,
            List<ExamToHandle> examToHandleList, Exam leader, FactHandle leaderHandle) {
        boolean perfectMatch = false;
        double bestScore = Double.NEGATIVE_INFINITY;
        Period bestPeriod = null;
        Room bestRoom = null;
        for (PeriodScoring periodScoring : periodScoringList) {
            if (bestScore >= periodScoring.getScore()) {
                // No need to check the rest
                break;
            }
            for (ExamToHandle examToHandle : examToHandleList) {
                workingMemory.modifyRetract(examToHandle.getExamHandle());
                examToHandle.getExam().setPeriod(periodScoring.getPeriod());
                workingMemory.modifyInsert(examToHandle.getExamHandle(), examToHandle.getExam());
            }
            for (Room room : roomList) {
                workingMemory.modifyRetract(leaderHandle);
                leader.setRoom(room);
                workingMemory.modifyInsert(leaderHandle, leader);
                double score = evaluationHandler.fireAllRulesAndCalculateStepScore();
                if (score < unscheduledScore) {
                    if (score > bestScore) {
                        bestScore = score;
                        bestPeriod = periodScoring.getPeriod();
                        bestRoom = room;
                    }
                } else if (score == unscheduledScore) {
                    perfectMatch = true;
                    break;
                } else {
                    throw new IllegalStateException("The score (" + score
                            + ") cannot be higher than unscheduledScore (" + unscheduledScore + ").");
                }
            }
            if (perfectMatch) {
                break;
            }
        }
        if (!perfectMatch) {
            if (bestPeriod == null || bestRoom == null) {
                throw new IllegalStateException("The bestPeriod (" + bestPeriod + ") or the bestRoom ("
                        + bestRoom + ") cannot be null.");
            }
            workingMemory.modifyRetract(leaderHandle);
            leader.setRoom(bestRoom);
            workingMemory.modifyInsert(leaderHandle, leader);
            for (ExamToHandle examToHandle : examToHandleList) {
                workingMemory.modifyRetract(examToHandle.getExamHandle());
                examToHandle.getExam().setPeriod(bestPeriod);
                workingMemory.modifyInsert(examToHandle.getExamHandle(), examToHandle.getExam());
            }
        }
        logger.debug("    Exam ({}) initialized for starting solution.", leader);
    }

    private void scheduleNonLeader(List<Room> roomList,
            EvaluationHandler evaluationHandler, WorkingMemory workingMemory,
            Exam exam, FactHandle examHandle) {
        if (exam.getRoom() != null) {
            throw new IllegalStateException("Exam (" + exam + ") already has a room.");
        }
        double unscheduledScore = evaluationHandler.fireAllRulesAndCalculateStepScore();
        boolean perfectMatch = false;
        double bestScore = Double.NEGATIVE_INFINITY;
        Room bestRoom = null;
        for (Room room : roomList) {
            workingMemory.modifyRetract(examHandle);
            exam.setRoom(room);
            workingMemory.modifyInsert(examHandle, exam);
            double score = evaluationHandler.fireAllRulesAndCalculateStepScore();
            if (score < unscheduledScore) {
                if (score > bestScore) {
                    bestScore = score;
                    bestRoom = room;
                }
            } else if (score == unscheduledScore) {
                perfectMatch = true;
                break;
            } else {
                throw new IllegalStateException("The score (" + score
                        + ") cannot be higher than unscheduledScore (" + unscheduledScore + ").");
            }
        }
        if (!perfectMatch) {
            if (bestRoom == null) {
                throw new IllegalStateException("The bestRoom ("
                        + bestRoom + ") cannot be null.");
            }
            workingMemory.modifyRetract(examHandle);
            exam.setRoom(bestRoom);
            workingMemory.modifyInsert(examHandle, exam);
        }
        logger.debug("    Exam ({}) initialized for starting solution. *", exam);
    }

    public static class ExamToHandle {

        private Exam exam;
        private FactHandle examHandle;

        public ExamToHandle(Exam exam) {
            this.exam = exam;
        }

        public Exam getExam() {
            return exam;
        }

        public FactHandle getExamHandle() {
            return examHandle;
        }

        public void setExamHandle(FactHandle examHandle) {
            this.examHandle = examHandle;
        }
    }

    /**
     * Create and order the exams in the order which we 'll assign them into periods and rooms.
     * @param examination not null
     * @return not null
     */
    private List<ExamInitialWeight> createExamAssigningScoreList(Examination examination) {
        List<Exam> examList = createExamList(examination);
        List<ExamInitialWeight> examInitialWeightList = new ArrayList<ExamInitialWeight>(examList.size());
        for (Exam exam : examList) {
            if (exam.isCoincidenceLeader()) {
                examInitialWeightList.add(new ExamInitialWeight(exam));
            }
        }
        Collections.sort(examInitialWeightList);
        return examInitialWeightList;
    }

    public List<Exam> createExamList(Examination examination) {
        List<Topic> topicList = examination.getTopicList();
        List<Exam> examList = new ArrayList<Exam>(topicList.size());
        Map<Topic, Exam> topicToExamMap = new HashMap<Topic, Exam>(topicList.size());
        for (Topic topic : topicList) {
            Exam exam = new Exam();
            exam.setId(topic.getId());
            exam.setTopic(topic);
            examList.add(exam);
            topicToExamMap.put(topic, exam);
        }
        for (PeriodHardConstraint periodHardConstraint : examination.getPeriodHardConstraintList()) {
            if (periodHardConstraint.getPeriodHardConstraintType() == PeriodHardConstraintType.EXAM_COINCIDENCE) {
                Exam leftExam = topicToExamMap.get(periodHardConstraint.getLeftSideTopic());
                Exam rightExam = topicToExamMap.get(periodHardConstraint.getRightSideTopic());

                Set<Exam> newCoincidenceExamSet = new LinkedHashSet<Exam>(4);
                ExamCoincidence leftExamCoincidence = leftExam.getExamCoincidence();
                if (leftExamCoincidence != null) {
                    newCoincidenceExamSet.addAll(leftExamCoincidence.getCoincidenceExamSet());
                } else {
                    newCoincidenceExamSet.add(leftExam);
                }
                ExamCoincidence rightExamCoincidence = rightExam.getExamCoincidence();
                if (rightExamCoincidence != null) {
                    newCoincidenceExamSet.addAll(rightExamCoincidence.getCoincidenceExamSet());
                } else {
                    newCoincidenceExamSet.add(rightExam);
                }
                ExamCoincidence newExamCoincidence = new ExamCoincidence(newCoincidenceExamSet);
                for (Exam exam : newCoincidenceExamSet) {
                    exam.setExamCoincidence(newExamCoincidence);
                }
            } else if (periodHardConstraint.getPeriodHardConstraintType() == PeriodHardConstraintType.AFTER) {
                Exam afterExam = topicToExamMap.get(periodHardConstraint.getLeftSideTopic());
                Exam beforeExam = topicToExamMap.get(periodHardConstraint.getRightSideTopic());
                ExamBefore examBefore = beforeExam.getExamBefore();
                if (examBefore == null) {
                    examBefore = new ExamBefore(new LinkedHashSet<Exam>(2));
                    beforeExam.setExamBefore(examBefore);
                }
                examBefore.getAfterExamSet().add(afterExam);
            }
        }
        return examList;
    }

    private class ExamInitialWeight implements Comparable<ExamInitialWeight> {

        private Exam exam;
        private int totalStudentSize;
        private int maximumDuration;

        private ExamInitialWeight(Exam exam) {
            this.exam = exam;
            totalStudentSize = calculateTotalStudentSize(exam);
            maximumDuration = calculateMaximumDuration(exam);
        }

        private int calculateTotalStudentSize(Exam innerExam) {
            int innerTotalStudentSize = 0;
            if (innerExam.getExamCoincidence() == null) {
                innerTotalStudentSize = innerExam.getTopicStudentSize();
            } else {
                for (Exam coincidenceExam : innerExam.getExamCoincidence().getCoincidenceExamSet()) {
                    innerTotalStudentSize += coincidenceExam.getTopicStudentSize();
                }
            }
            if (innerExam.getExamBefore() != null) {
                for (Exam afterExam : innerExam.getExamBefore().getAfterExamSet()) {
                    innerTotalStudentSize += calculateTotalStudentSize(afterExam); // recursive
                }
            }
            return innerTotalStudentSize;
        }

        private int calculateMaximumDuration(Exam innerExam) {
            int innerMaximumDuration = innerExam.getTopic().getDuration();
            if (innerExam.getExamCoincidence() != null) {
                for (Exam coincidenceExam : innerExam.getExamCoincidence().getCoincidenceExamSet()) {
                    innerMaximumDuration = Math.max(innerMaximumDuration, coincidenceExam.getTopicStudentSize());
                }
            }
            if (innerExam.getExamBefore() != null) {
                for (Exam afterExam : innerExam.getExamBefore().getAfterExamSet()) {
                    innerMaximumDuration = Math.max(innerMaximumDuration, calculateMaximumDuration(afterExam)); // recursive
                }
            }
            return innerMaximumDuration;
        }

        public Exam getExam() {
            return exam;
        }

        public int compareTo(ExamInitialWeight other) {
            // TODO calculate a assigningScore based on the properties of a topic and sort on that assigningScore
            return new CompareToBuilder()
                    .append(other.totalStudentSize, totalStudentSize) // Descending
                    .append(other.maximumDuration, maximumDuration) // Descending
                    .append(exam.getId(), other.exam.getId()) // Ascending
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
