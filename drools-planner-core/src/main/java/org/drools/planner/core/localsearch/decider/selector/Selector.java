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

package org.drools.planner.core.localsearch.decider.selector;

import java.util.Iterator;

import org.drools.planner.core.localsearch.LocalSearchStepScope;
import org.drools.planner.core.localsearch.decider.DeciderAware;
import org.drools.planner.core.localsearch.event.LocalSearchSolverPhaseLifecycleListener;
import org.drools.planner.core.move.Move;

/**
 * A Selector selects or generates moves for the Decider.
 * @see AbstractSelector
 */
public interface Selector extends DeciderAware, LocalSearchSolverPhaseLifecycleListener {

    Iterator<Move> moveIterator(LocalSearchStepScope localSearchStepScope);

}
