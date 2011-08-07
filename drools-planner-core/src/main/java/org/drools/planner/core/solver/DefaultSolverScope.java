/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      hhttp://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core.solver;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.drools.WorkingMemory;
import org.drools.planner.core.domain.solution.SolutionDescriptor;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.definition.ScoreDefinition;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.solution.director.DefaultSolutionDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSolverScope {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected DefaultSolutionDirector solutionDirector;

    protected boolean restartSolver = false;
    protected long startingSystemTimeMillis;

    protected Random workingRandom;

    protected Score startingInitializedScore; // TODO after initialization => ambiguous with setStartingSolution

    protected Solution bestSolution;
    protected Score bestScore; // TODO remove me

    public DefaultSolutionDirector getSolutionDirector() {
        return solutionDirector;
    }

    public void setSolutionDirector(DefaultSolutionDirector solutionDirector) {
        this.solutionDirector = solutionDirector;
    }

    public SolutionDescriptor getSolutionDescriptor() {
        return solutionDirector.getSolutionDescriptor();
    }

    public ScoreDefinition getScoreDefinition() {
        return solutionDirector.getScoreDefinition();
    }

    public long getStartingSystemTimeMillis() {
        return startingSystemTimeMillis;
    }

    public void setStartingSystemTimeMillis(long startingSystemTimeMillis) {
        this.startingSystemTimeMillis = startingSystemTimeMillis;
    }

    public boolean isRestartSolver() {
        return restartSolver;
    }

    public void setRestartSolver(boolean restartSolver) {
        this.restartSolver = restartSolver;
    }

    public Solution getWorkingSolution() {
        return solutionDirector.getWorkingSolution();
    }

    public Collection<Object> getWorkingFacts() {
        return solutionDirector.getWorkingFacts();
    }

    public List<Object> getWorkingPlanningEntityList() {
        return solutionDirector.getWorkingPlanningEntityList();
    }

    public boolean isWorkingSolutionInitialized() {
        return solutionDirector.isWorkingSolutionInitialized();
    }

    public WorkingMemory getWorkingMemory() {
        return solutionDirector.getWorkingMemory();
    }

    public Score calculateScoreFromWorkingMemory() {
        return solutionDirector.calculateScoreFromWorkingMemory();
    }

    public void assertWorkingScore(Score presumedScore) {
        solutionDirector.assertWorkingScore(presumedScore);
    }

    public Random getWorkingRandom() {
        return workingRandom;
    }

    public void setWorkingRandom(Random workingRandom) {
        this.workingRandom = workingRandom;
    }

    public Score getStartingInitializedScore() {
        return startingInitializedScore;
    }

    public void setStartingInitializedScore(Score startingInitializedScore) {
        this.startingInitializedScore = startingInitializedScore;
    }

    public long getCalculateCount() {
        return solutionDirector.getCalculateCount();
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(Solution bestSolution) {
        this.bestSolution = bestSolution;
    }

    public Score getBestScore() {
        return bestScore;
    }

    public void setBestScore(Score bestScore) {
        this.bestScore = bestScore;
    }

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

    public void reset() {
        startingSystemTimeMillis = System.currentTimeMillis();
        solutionDirector.resetCalculateCount();
    }

    public long calculateTimeMillisSpend() {
        long now = System.currentTimeMillis();
        return now - startingSystemTimeMillis;
    }

}
