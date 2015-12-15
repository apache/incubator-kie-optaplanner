/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.move.decorator;

import java.util.Iterator;
import java.util.List;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.move.AbstractMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class FilteringMoveSelector extends AbstractMoveSelector {

    protected final MoveSelector childMoveSelector;
    protected final List<SelectionFilter> filterList;
    protected final boolean bailOutEnabled;

    protected ScoreDirector scoreDirector = null;

    public FilteringMoveSelector(MoveSelector childMoveSelector, List<SelectionFilter> filterList) {
        this.childMoveSelector = childMoveSelector;
        this.filterList = filterList;
        bailOutEnabled = childMoveSelector.isNeverEnding();
        phaseLifecycleSupport.addEventListener(childMoveSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(AbstractPhaseScope phaseScope) {
        super.phaseStarted(phaseScope);
        scoreDirector = phaseScope.getScoreDirector();
    }

    @Override
    public void phaseEnded(AbstractPhaseScope phaseScope) {
        super.phaseEnded(phaseScope);
        scoreDirector = null;
    }

    public boolean isCountable() {
        return childMoveSelector.isCountable();
    }

    public boolean isNeverEnding() {
        return childMoveSelector.isNeverEnding();
    }

    public long getSize() {
        return childMoveSelector.getSize();
    }

    public Iterator<Move> iterator() {
        return new JustInTimeFilteringMoveIterator(childMoveSelector.iterator());
    }

    private class JustInTimeFilteringMoveIterator extends UpcomingSelectionIterator<Move> {

        private final Iterator<Move> childMoveIterator;
        private final long bailOutSize;

        public JustInTimeFilteringMoveIterator(Iterator<Move> childMoveIterator) {
            this.childMoveIterator = childMoveIterator;
            this.bailOutSize = determineBailOutSize();
        }

        @Override
        protected Move createUpcomingSelection() {
            Move next;
            long attemptsBeforeBailOut = bailOutSize;
            do {
                if (!childMoveIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                if (bailOutEnabled) {
                    // if childMoveIterator is neverEnding and nothing is accepted, bail out of the infinite loop
                    if (attemptsBeforeBailOut <= 0L) {
                        logger.warn("Bailing out of neverEnding selector ({}) to avoid infinite loop.",
                                FilteringMoveSelector.this);
                        return noUpcomingSelection();
                    }
                    attemptsBeforeBailOut--;
                }
                next = childMoveIterator.next();
            } while (!accept(scoreDirector, next));
            return next;
        }

    }

    protected long determineBailOutSize() {
        if (!bailOutEnabled) {
            return -1L;
        }
        return childMoveSelector.getSize() * 10L;
    }

    private boolean accept(ScoreDirector scoreDirector, Move move) {
        for (SelectionFilter filter : filterList) {
            if (!filter.accept(scoreDirector, move)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Filtering(" + childMoveSelector + ")";
    }

}
