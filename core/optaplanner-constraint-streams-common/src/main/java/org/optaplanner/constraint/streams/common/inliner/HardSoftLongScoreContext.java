
package org.optaplanner.constraint.streams.common.inliner;

import java.util.function.LongConsumer;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftLongScoreContext extends ScoreContext<HardSoftLongScore> {

    private final LongConsumer softScoreUpdater;
    private final LongConsumer hardScoreUpdater;

    public HardSoftLongScoreContext(Constraint constraint, HardSoftLongScore constraintWeight, boolean constraintMatchEnabled,
            LongConsumer hardScoreUpdater, LongConsumer softScoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreUpdater = softScoreUpdater;
        this.hardScoreUpdater = hardScoreUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(long change) {
        softScoreUpdater.accept(change);
        return () -> softScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeHardScoreBy(long change) {
        hardScoreUpdater.accept(change);
        return () -> hardScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeScoreBy(long hardChange, long softChange) {
        hardScoreUpdater.accept(hardChange);
        softScoreUpdater.accept(softChange);
        return () -> {
            hardScoreUpdater.accept(-hardChange);
            softScoreUpdater.accept(-softChange);
        };
    }

}
