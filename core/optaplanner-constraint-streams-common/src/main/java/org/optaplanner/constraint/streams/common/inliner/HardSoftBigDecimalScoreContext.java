
package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftBigDecimalScoreContext extends ScoreContext<HardSoftBigDecimalScore> {

    private final Consumer<BigDecimal> softScoreUpdater;
    private final Consumer<BigDecimal> hardScoreUpdater;

    public HardSoftBigDecimalScoreContext(Constraint constraint, HardSoftBigDecimalScore constraintWeight,
            boolean constraintMatchEnabled, Consumer<BigDecimal> hardScoreUpdater, Consumer<BigDecimal> softScoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreUpdater = softScoreUpdater;
        this.hardScoreUpdater = hardScoreUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(BigDecimal change) {
        softScoreUpdater.accept(change);
        return () -> softScoreUpdater.accept(change.negate());
    }

    public UndoScoreImpacter changeHardScoreBy(BigDecimal change) {
        hardScoreUpdater.accept(change);
        return () -> hardScoreUpdater.accept(change.negate());
    }

    public UndoScoreImpacter changeScoreBy(BigDecimal hardChange, BigDecimal softChange) {
        hardScoreUpdater.accept(hardChange);
        softScoreUpdater.accept(softChange);
        return () -> {
            hardScoreUpdater.accept(hardChange.negate());
            softScoreUpdater.accept(softChange.negate());
        };
    }

}
