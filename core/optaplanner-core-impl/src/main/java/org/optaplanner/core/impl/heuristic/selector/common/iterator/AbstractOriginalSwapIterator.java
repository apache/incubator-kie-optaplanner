/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.common.iterator;

import java.util.Collections;
import java.util.ListIterator;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.ListIterableSelector;

public abstract class AbstractOriginalSwapIterator<Solution_, Move_ extends Move<Solution_>, SubSelection_>
        extends UpcomingSelectionIterator<Move_> {

    public static <Solution_, SubSelection_> long getSize(ListIterableSelector<Solution_, SubSelection_> leftSubSelector,
            ListIterableSelector<Solution_, SubSelection_> rightSubSelector) {
        if (leftSubSelector != rightSubSelector) {
            return leftSubSelector.getSize() * rightSubSelector.getSize();
        } else {
            long leftSize = leftSubSelector.getSize();
            return leftSize * (leftSize - 1L) / 2L;
        }
    }

    protected final ListIterable<SubSelection_> leftSubSelector;
    protected final ListIterable<SubSelection_> rightSubSelector;
    protected final boolean leftEqualsRight;

    private final ListIterator<SubSelection_> leftSubSelectionIterator;
    private ListIterator<SubSelection_> rightSubSelectionIterator;

    private SubSelection_ leftSubSelection;

    public AbstractOriginalSwapIterator(ListIterable<SubSelection_> leftSubSelector,
            ListIterable<SubSelection_> rightSubSelector) {
        this.leftSubSelector = leftSubSelector;
        this.rightSubSelector = rightSubSelector;
        leftEqualsRight = (leftSubSelector == rightSubSelector);
        leftSubSelectionIterator = leftSubSelector.listIterator();
        rightSubSelectionIterator = Collections.<SubSelection_> emptyList().listIterator();
        // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
    }

    @Override
    protected Move_ createUpcomingSelection() {
        if (!rightSubSelectionIterator.hasNext()) {
            if (!leftSubSelectionIterator.hasNext()) {
                return noUpcomingSelection();
            }
            leftSubSelection = leftSubSelectionIterator.next();

            if (!leftEqualsRight) {
                rightSubSelectionIterator = rightSubSelector.listIterator();
                if (!rightSubSelectionIterator.hasNext()) {
                    return noUpcomingSelection();
                }
            } else {
                // Select A-B, A-C, B-C. Do not select B-A, C-A, C-B. Do not select A-A, B-B, C-C.
                if (!leftSubSelectionIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                rightSubSelectionIterator = rightSubSelector.listIterator(leftSubSelectionIterator.nextIndex());
                // rightEntityIterator's first hasNext() always returns true because of the nextIndex()
            }
        }
        SubSelection_ rightSubSelection = rightSubSelectionIterator.next();
        return newSwapSelection(leftSubSelection, rightSubSelection);
    }

    protected abstract Move_ newSwapSelection(SubSelection_ leftSubSelection, SubSelection_ rightSubSelection);

}
