package org.drools.solver.examples.itc2007.examination.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.FactHandle;
import org.drools.StatefulSession;
import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
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
        List<Topic> topicList = examination.getTopicList();
        List<Period> periodList = examination.getPeriodList();
        List<Room> roomList = examination.getRoomList();
        List<Exam> examList = new ArrayList<Exam>(topicList.size());
        examination.setExamList(examList);
        evaluationHandler.setSolution(examination);
        StatefulSession statefulSession = evaluationHandler.getStatefulSession();

        // Sort the order in which we 'll assign the topics into periods and rooms
        List<Topic> sortedTopicList = new ArrayList<Topic>(topicList);
        Collections.sort(sortedTopicList, new Comparator<Topic>() {
            public int compare(Topic a, Topic b) {
                return new CompareToBuilder()
                        .append(a.getStudentListSize(), b.getStudentListSize())
                        .toComparison();
            }
        });

        for (Topic topic : sortedTopicList) {
            double unscheduledScore = evaluationHandler.fireAllRulesAndCalculateStepScore();

            Exam exam = new Exam();
            exam.setId(topic.getId());
            exam.setTopic(topic);
            examList.add(exam);
            FactHandle examHandle = null;

            List<PeriodScoring> periodScoringList = new ArrayList<PeriodScoring>(periodList.size());

            for (Period period : periodList) {
                exam.setPeriod(period);
                if (examHandle == null) {
                    examHandle = statefulSession.insert(exam); // TODO move up
                } else {
                    statefulSession.update(examHandle, exam);
                }
                double score = evaluationHandler.fireAllRulesAndCalculateStepScore();
                periodScoringList.add(new PeriodScoring(period, score));
            }
            Collections.sort(periodScoringList);

            boolean perfectMatch = false;
            double bestScore = Double.NEGATIVE_INFINITY;
            Period bestPeriod = null;
            Room bestRoom = null;
            for (PeriodScoring periodScoring : periodScoringList) {
                if (bestScore >= periodScoring.getScore()) {
                    // No need to check the rest
                    break;
                }
                exam.setPeriod(periodScoring.getPeriod());
                for (Room room : roomList) {
                    exam.setRoom(room);
                    statefulSession.update(examHandle, exam);
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
                exam.setPeriod(bestPeriod);
                exam.setRoom(bestRoom);
                statefulSession.update(examHandle, exam);
            }
            logger.debug("    Exam ({}) initialized for starting solution.", exam);
        }
        Collections.sort(examList, new PersistableIdComparator());
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
            return - new CompareToBuilder().append(score, other.score).toComparison();
        }

    }

}
