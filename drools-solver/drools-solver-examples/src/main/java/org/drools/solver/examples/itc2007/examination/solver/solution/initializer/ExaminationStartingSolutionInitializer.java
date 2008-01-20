package org.drools.solver.examples.itc2007.examination.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.FactHandle;
import org.drools.StatefulSession;
import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.ExamCoincidence;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraint;
import org.drools.solver.examples.itc2007.examination.domain.PeriodHardConstraintType;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.domain.Topic;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationStartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    public void intializeSolution() {
        Examination examination = (Examination) solver.getEvaluationHandler().getSolution();
        if (!examination.isInitialized()) {
            initializeExamList(examination);
        }
    }

    private void initializeExamList(Examination examination) {
        EvaluationHandler evaluationHandler = solver.getEvaluationHandler();
        List<Period> periodList = examination.getPeriodList();
        List<Room> roomList = examination.getRoomList();
        List<Exam> examList = new ArrayList<Exam>(examination.getTopicList().size());
        examination.setExamList(examList);
        evaluationHandler.setSolution(examination);
        StatefulSession statefulSession = evaluationHandler.getStatefulSession();

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
                    examToHandle.getExam().setPeriod(period);
                    if (examToHandle.getExamHandle() == null) {
                        examToHandle.setExamHandle(statefulSession.insert(examToHandle.getExam()));
                        if (examToHandle.getExam().isCoincidenceLeader()) {
                            leaderHandle = examToHandle.getExamHandle();
                        }
                    } else {
                        statefulSession.update(examToHandle.getExamHandle(), examToHandle.getExam());
                    }
                }
                double score = evaluationHandler.fireAllRulesAndCalculateStepScore();
                periodScoringList.add(new PeriodScoring(period, score));
            }
            Collections.sort(periodScoringList);

            scheduleLeader(periodScoringList, roomList, evaluationHandler, statefulSession, unscheduledScore,
                    examToHandleList, leader, leaderHandle);
            examList.add(leader);

            // Schedule the non leaders
            for (ExamToHandle examToHandle : examToHandleList) {
                Exam exam = examToHandle.getExam();
                // Leader already has a room
                if (!exam.isCoincidenceLeader()) {
                    scheduleNonLeader(roomList, evaluationHandler, statefulSession, exam, examToHandle.getExamHandle());
                    examList.add(exam);
                }
            }
        }
        Collections.sort(examList, new PersistableIdComparator());
        examination.setExamList(examList);
    }

    private void scheduleLeader(List<PeriodScoring> periodScoringList, List<Room> roomList,
            EvaluationHandler evaluationHandler, StatefulSession statefulSession, double unscheduledScore,
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
                examToHandle.getExam().setPeriod(periodScoring.getPeriod());
                statefulSession.update(examToHandle.getExamHandle(), examToHandle.getExam());
            }
            for (Room room : roomList) {
                leader.setRoom(room);
                statefulSession.update(leaderHandle, leader);
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
            leader.setRoom(bestRoom);
            for (ExamToHandle examToHandle : examToHandleList) {
                examToHandle.getExam().setPeriod(bestPeriod);
                statefulSession.update(examToHandle.getExamHandle(), examToHandle.getExam());
            }
        }
        logger.debug("    Exam ({}) initialized for starting solution.", leader);
    }

    private void scheduleNonLeader(List<Room> roomList,
            EvaluationHandler evaluationHandler, StatefulSession statefulSession,
            Exam exam, FactHandle examHandle) {
        if (exam.getRoom() != null) {
            throw new IllegalStateException("Exam (" + exam + ") already has a room.");
        }
        double unscheduledScore = evaluationHandler.fireAllRulesAndCalculateStepScore();
        boolean perfectMatch = false;
        double bestScore = Double.NEGATIVE_INFINITY;
        Room bestRoom = null;
        for (Room room : roomList) {
            exam.setRoom(room);
            statefulSession.update(examHandle, exam);
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
            exam.setRoom(bestRoom);
            statefulSession.update(examHandle, exam);
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
        for (PeriodHardConstraint periodHardConstraint : examination.getPeriodHardConstraintList()) {
            if (periodHardConstraint.getPeriodHardConstraintType() == PeriodHardConstraintType.AFTER) {
                int afterSideIndex = -1;
                int beforeSideIndex = -1;
                for (int i = 0; i < examInitialWeightList.size(); i++) {
                    Topic topic = examInitialWeightList.get(i).getExam().getTopic();
                    if (topic.equals(periodHardConstraint.getLeftSideTopic())) { // TODO FIXME topic could be in coinc.
                        afterSideIndex = i;
                    }
                    if (topic.equals(periodHardConstraint.getRightSideTopic())) { // TODO FIXME topic could be in coinc.
                        beforeSideIndex = i;
                    }
                }
                if (afterSideIndex < beforeSideIndex) {
                    ExamInitialWeight beforeExamInitialWeight = examInitialWeightList.remove(beforeSideIndex);
                    examInitialWeightList.add(afterSideIndex, beforeExamInitialWeight);
                }
            }
        }

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

                Set<Exam> newCoincidenceExamSet = new HashSet<Exam>();
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
                leftExam.setExamCoincidence(newExamCoincidence);
                rightExam.setExamCoincidence(newExamCoincidence);
            }
        }
        return examList;
    }

    private class ExamInitialWeight implements Comparable<ExamInitialWeight> {

        private Exam exam;
        private int totalStudentSize;

        private ExamInitialWeight(Exam exam) {
            this.exam = exam;
            if (exam.getExamCoincidence() == null) {
                totalStudentSize = exam.getTopicStudentListSize();
            } else {
                totalStudentSize = 0;
                for (Exam coincidenceExam : exam.getExamCoincidence().getCoincidenceExamSet()) {
                    totalStudentSize += coincidenceExam.getTopicStudentListSize();
                }
            }

        }

        public Exam getExam() {
            return exam;
        }

        public int compareTo(ExamInitialWeight other) {
            // TODO calculate a assigningScore based on the properties of a topic and sort on that assigningScore
            return new CompareToBuilder()
                    .append(other.totalStudentSize, totalStudentSize) // Descending
                    .append(other.exam.getTopic().getDuration(), exam.getTopic().getDuration()) // Descending
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
