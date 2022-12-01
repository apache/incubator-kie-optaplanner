package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class BendableBigDecimalScoreContext extends ScoreContext<BendableBigDecimalScore> {

    private final IntBigDecimalConsumer softScoreLevelUpdater;
    private final IntBigDecimalConsumer hardScoreLevelUpdater;

    public BendableBigDecimalScoreContext(Constraint constraint, BendableBigDecimalScore constraintWeight,
            boolean constraintMatchEnabled, IntBigDecimalConsumer hardScoreLevelUpdater,
            IntBigDecimalConsumer softScoreLevelUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreLevelUpdater = softScoreLevelUpdater;
        this.hardScoreLevelUpdater = hardScoreLevelUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(int level, BigDecimal change) {
        softScoreLevelUpdater.accept(level, change);
        return () -> softScoreLevelUpdater.accept(level, change.negate());
    }

    public UndoScoreImpacter changeHardScoreBy(int level, BigDecimal change) {
        hardScoreLevelUpdater.accept(level, change);
        return () -> hardScoreLevelUpdater.accept(level, change.negate());
    }

    public UndoScoreImpacter changeScoreBy(BigDecimal[] hardChanges, BigDecimal[] softChanges) {
        for (int i = 0; i < hardChanges.length; i++) {
            hardScoreLevelUpdater.accept(i, hardChanges[i]);
        }
        for (int i = 0; i < softChanges.length; i++) {
            softScoreLevelUpdater.accept(i, softChanges[i]);
        }
        return () -> {
            for (int i = 0; i < hardChanges.length; i++) {
                hardScoreLevelUpdater.accept(i, hardChanges[i].negate());
            }
            for (int i = 0; i < softChanges.length; i++) {
                softScoreLevelUpdater.accept(i, softChanges[i].negate());
            }
        };
    }

    public interface IntBigDecimalConsumer {

        void accept(int value1, BigDecimal value2);

    }

}
