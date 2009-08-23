package org.drools.solver.benchmark.statistic;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import org.drools.solver.core.Solver;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.SimpleScore;
import org.drools.solver.core.score.HardAndSoftScore;
import org.apache.commons.io.IOUtils;

/**
 * @author Geoffrey De Smet
 */
public class BestSolutionStatistic implements SolverStatistic {

    // A LinkedHashMap because the order is important
    private Map<String, BestSolutionStatisticListener> bestSolutionStatisticListenerMap
            = new LinkedHashMap<String, BestSolutionStatisticListener>();

    public void addListener(Solver solver) {
        addListener(solver, "solver");
    }

    public void addListener(Solver solver, String configName) {
        if (bestSolutionStatisticListenerMap.containsKey(configName)) {
            throw new IllegalArgumentException("Cannot add a listener with the same configName (" + configName
                    + ") twice.");
        }
        BestSolutionStatisticListener bestSolutionStatisticListener = new BestSolutionStatisticListener();
        solver.addEventListener(bestSolutionStatisticListener);
        bestSolutionStatisticListenerMap.put(configName, bestSolutionStatisticListener);
    }

    public void writeStatistic(File solverStatisticFilesDirectory, String baseName) {
        Set<String> configNameSet = bestSolutionStatisticListenerMap.keySet();
        List<TimeMillisSpendDetail> timeMillisSpendDetailList = extractTimeMillisSpendDetailList();
        File statisticFile = new File(solverStatisticFilesDirectory, baseName + "Statistic.csv");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(statisticFile), "utf-8");
            writer.append("\"TimeMillisSpend\"");
            for (String configName : configNameSet) {
                writer.append(",\"").append(configName.replaceAll("\\\"","\\\"")).append("\"");
            }
            writer.append("\n");
            for (TimeMillisSpendDetail timeMillisSpendDetail : timeMillisSpendDetailList) {
                writer.write(Long.toString(timeMillisSpendDetail.getTimeMillisSpend()));
                for (String configName : configNameSet) {
                    writer.append(",");
                    Score score = timeMillisSpendDetail.getConfigNameToScoreMap().get(configName);
                    if (score != null) {
                        Integer scoreAlias;
                        if (score instanceof SimpleScore) {
                            SimpleScore simpleScore = (SimpleScore) score;
                            scoreAlias = simpleScore.getScore();
                        } else if (score instanceof HardAndSoftScore) {
                            HardAndSoftScore hardAndSoftScore = (HardAndSoftScore) score;
                            if (hardAndSoftScore.getHardScore() == 0) {
                                scoreAlias = hardAndSoftScore.getSoftScore();
                            } else {
                                scoreAlias = null;
                            }
                        } else {
                            throw new IllegalStateException("Score class (" + score.getClass() + ") not supported.");
                        }
                        if (scoreAlias != null) {
                            writer.append(scoreAlias.toString());
                        }
                    }
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing statisticFile: " + statisticFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private List<TimeMillisSpendDetail> extractTimeMillisSpendDetailList() {
        Map<Long, TimeMillisSpendDetail> timeMillisSpendToDetailMap = new HashMap<Long, TimeMillisSpendDetail>();
        for (Map.Entry<String, BestSolutionStatisticListener> listenerEntry : bestSolutionStatisticListenerMap.entrySet()) {
            String configName = listenerEntry.getKey();
            List<BestSolutionStatisticDetail> statisticDetailList = listenerEntry.getValue()
                    .getBestSolutionStatisticDetailList();
            for (BestSolutionStatisticDetail statisticDetail : statisticDetailList) {
                long timeMillisSpend = statisticDetail.getTimeMillisSpend();
                TimeMillisSpendDetail detail = timeMillisSpendToDetailMap.get(timeMillisSpend);
                if (detail == null) {
                    detail = new TimeMillisSpendDetail(timeMillisSpend);
                    timeMillisSpendToDetailMap.put(timeMillisSpend, detail);
                }
                detail.getConfigNameToScoreMap().put(configName, statisticDetail.getScore());
            }
        }
        List<TimeMillisSpendDetail> timeMillisSpendDetailList
                = new ArrayList<TimeMillisSpendDetail>(timeMillisSpendToDetailMap.values());
        Collections.sort(timeMillisSpendDetailList);
        return timeMillisSpendDetailList;
    }

    protected class TimeMillisSpendDetail implements Comparable<TimeMillisSpendDetail> {

        private long timeMillisSpend;
        private Map<String, Score> configNameToScoreMap;

        public TimeMillisSpendDetail(long timeMillisSpend) {
            this.timeMillisSpend = timeMillisSpend;
            configNameToScoreMap = new HashMap<String, Score>();
        }

        public long getTimeMillisSpend() {
            return timeMillisSpend;
        }

        public Map<String, Score> getConfigNameToScoreMap() {
            return configNameToScoreMap;
        }

        public int compareTo(TimeMillisSpendDetail other) {
            return timeMillisSpend < other.timeMillisSpend ? -1 : (timeMillisSpend > other.timeMillisSpend ? 1 : 0);
        }

    }

}
