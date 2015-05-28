package org.optaplanner.core.impl.heuristic.selector.common.decorator;

import org.optaplanner.core.impl.score.director.ScoreDirector;

public class ReinitializeVariableEntityRejectingFilter implements SelectionFilter<Object> {

    @Override
    public boolean accept(ScoreDirector scoreDirector, Object selection) {
        return false;
    }
}
