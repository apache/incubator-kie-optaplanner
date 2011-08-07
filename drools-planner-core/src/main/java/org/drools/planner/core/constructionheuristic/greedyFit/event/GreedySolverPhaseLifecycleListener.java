/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.core.constructionheuristic.greedyFit.event;

import java.util.EventListener;

import org.drools.planner.core.constructionheuristic.greedyFit.GreedyFitSolverPhaseScope;
import org.drools.planner.core.constructionheuristic.greedyFit.GreedyFitStepScope;

public interface GreedySolverPhaseLifecycleListener extends EventListener {

    void phaseStarted(GreedyFitSolverPhaseScope greedyFitSolverPhaseScope);

    void beforeDeciding(GreedyFitStepScope greedyFitStepScope);

    void stepDecided(GreedyFitStepScope greedyFitStepScope);

    void stepTaken(GreedyFitStepScope greedyFitStepScope);

    void phaseEnded(GreedyFitSolverPhaseScope greedyFitSolverPhaseScope);

}
