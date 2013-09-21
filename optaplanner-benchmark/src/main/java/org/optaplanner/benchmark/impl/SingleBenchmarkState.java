/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.benchmark.impl;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.HashMap;
import java.util.Map;
import org.optaplanner.benchmark.impl.statistic.SingleStatistic;
import org.optaplanner.benchmark.impl.statistic.SingleStatisticState;
import org.optaplanner.benchmark.impl.statistic.StatisticType;
import org.optaplanner.core.api.score.Score;

/**
 *
 * @author matej
 */
@XStreamAlias("singleBenchmarkState")
public class SingleBenchmarkState {
    
    private Long singleBenchmarkStateId;
    private long timeMillisSpend;
    private Long calculateCount = 0L;
    private Score score;
    private Integer planningEntityCount;
    private Long usedMemoryAfterInputSolution;
    private Map<StatisticType, SingleStatisticState> singleStatisticMap = new HashMap<StatisticType, SingleStatisticState>();
    private Boolean succeeded;
    private Long problemScale;
    
    public SingleBenchmarkState() {
    }

    public SingleBenchmarkState(Long singleBenchmarkStateId) {
        this.singleBenchmarkStateId = singleBenchmarkStateId;
    }
    
    public Long getSingleBenchmarkStateId() {
        return singleBenchmarkStateId;
    }

    public void setSingleBenchmarkStateId(Long singleBenchmarkStateId) {
        this.singleBenchmarkStateId = singleBenchmarkStateId;
    }

    public long getTimeMillisSpend() {
        return timeMillisSpend;
    }

    public void setTimeMillisSpend(long timeMillisSpend) {
        this.timeMillisSpend = timeMillisSpend;
    }

    public Long getCalculateCount() {
        return calculateCount;
    }

    public void setCalculateCount(Long calculateCount) {
        this.calculateCount = calculateCount;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Integer getPlanningEntityCount() {
        return planningEntityCount;
    }

    public void setPlanningEntityCount(Integer planningEntityCount) {
        this.planningEntityCount = planningEntityCount;
    }

    public Long getUsedMemoryAfterInputSolution() {
        return usedMemoryAfterInputSolution;
    }

    public void setUsedMemoryAfterInputSolution(Long usedMemoryAfterInputSolution) {
        this.usedMemoryAfterInputSolution = usedMemoryAfterInputSolution;
    }

    public Map<StatisticType, SingleStatisticState> getSingleStatisticMap() {
        return singleStatisticMap;
    }
    
    public Boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(Boolean succeeded) {
        this.succeeded = succeeded;
    }

    public Long getProblemScale() {
        return problemScale;
    }

    public void setProblemScale(Long problemScale) {
        this.problemScale = problemScale;
    }
    
}
