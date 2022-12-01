package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftBigDecimalScoreContext extends ScoreContext<HardMediumSoftBigDecimalScore> {

    private final Consumer<BigDecimal> softScoreUpdater;
    private final Consumer<BigDecimal> mediumScoreUpdater;
    private final Consumer<BigDecimal> hardScoreUpdater;

    public HardMediumSoftBigDecimalScoreContext(Constraint constraint, HardMediumSoftBigDecimalScore constraintWeight,
            boolean constraintMatchEnabled, Consumer<BigDecimal> hardScoreUpdater, Consumer<BigDecimal> mediumScoreUpdater,
            Consumer<BigDecimal> softScoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreUpdater = softScoreUpdater;
        this.mediumScoreUpdater = mediumScoreUpdater;
        this.hardScoreUpdater = hardScoreUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(BigDecimal change) {
        softScoreUpdater.accept(change);
        return () -> softScoreUpdater.accept(change.negate());
    }

    public UndoScoreImpacter changeMediumScoreBy(BigDecimal change) {
        mediumScoreUpdater.accept(change);
        return () -> mediumScoreUpdater.accept(change.negate());
    }

    public UndoScoreImpacter changeHardScoreBy(BigDecimal change) {
        hardScoreUpdater.accept(change);
        return () -> hardScoreUpdater.accept(change.negate());
    }

    public UndoScoreImpacter changeScoreBy(BigDecimal hardChange, BigDecimal mediumChange, BigDecimal softChange) {
        hardScoreUpdater.accept(hardChange);
        mediumScoreUpdater.accept(mediumChange);
        softScoreUpdater.accept(softChange);
        return () -> {
            hardScoreUpdater.accept(hardChange.negate());
            mediumScoreUpdater.accept(mediumChange.negate());
            softScoreUpdater.accept(softChange.negate());
        };
    }

}
