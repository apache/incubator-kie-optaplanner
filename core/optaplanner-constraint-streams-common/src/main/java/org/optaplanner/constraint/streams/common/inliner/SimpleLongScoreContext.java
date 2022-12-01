package org.optaplanner.constraint.streams.common.inliner;

import java.util.function.LongConsumer;

import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleLongScoreContext extends ScoreContext<SimpleLongScore> {

    private final LongConsumer scoreUpdater;

    public SimpleLongScoreContext(Constraint constraint, SimpleLongScore constraintWeight, boolean constraintMatchEnabled,
            LongConsumer scoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.scoreUpdater = scoreUpdater;
    }

    public UndoScoreImpacter changeScoreBy(long change) {
        scoreUpdater.accept(change);
        return () -> scoreUpdater.accept(-change);
    }

}
