package org.optaplanner.constraint.streams.common.inliner;

import java.util.function.IntConsumer;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleScoreContext extends ScoreContext<SimpleScore> {

    private final IntConsumer scoreUpdater;

    public SimpleScoreContext(Constraint constraint, SimpleScore constraintWeight, boolean constraintMatchEnabled,
            IntConsumer scoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.scoreUpdater = scoreUpdater;
    }

    public UndoScoreImpacter changeScoreBy(int change) {
        scoreUpdater.accept(change);
        return () -> scoreUpdater.accept(-change);
    }

}
