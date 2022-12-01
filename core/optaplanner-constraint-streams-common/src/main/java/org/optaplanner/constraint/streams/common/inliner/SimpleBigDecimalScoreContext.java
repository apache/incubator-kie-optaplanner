package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleBigDecimalScoreContext extends ScoreContext<SimpleBigDecimalScore> {

    private final Consumer<BigDecimal> scoreUpdater;

    public SimpleBigDecimalScoreContext(Constraint constraint, SimpleBigDecimalScore constraintWeight,
            boolean constraintMatchEnabled, Consumer<BigDecimal> scoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.scoreUpdater = scoreUpdater;
    }

    public UndoScoreImpacter changeScoreBy(BigDecimal change) {
        scoreUpdater.accept(change);
        return () -> scoreUpdater.accept(change.negate());
    }

}
