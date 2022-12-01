package org.optaplanner.constraint.streams.common.inliner;

import java.util.function.LongConsumer;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftLongScoreContext extends ScoreContext<HardMediumSoftLongScore> {

    private final LongConsumer softScoreUpdater;
    private final LongConsumer mediumScoreUpdater;
    private final LongConsumer hardScoreUpdater;

    public HardMediumSoftLongScoreContext(Constraint constraint, HardMediumSoftLongScore constraintWeight,
            boolean constraintMatchEnabled, LongConsumer hardScoreUpdater, LongConsumer mediumScoreUpdater,
            LongConsumer softScoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreUpdater = softScoreUpdater;
        this.mediumScoreUpdater = mediumScoreUpdater;
        this.hardScoreUpdater = hardScoreUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(long change) {
        softScoreUpdater.accept(change);
        return () -> softScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeMediumScoreBy(long change) {
        mediumScoreUpdater.accept(change);
        return () -> mediumScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeHardScoreBy(long change) {
        hardScoreUpdater.accept(change);
        return () -> hardScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeScoreBy(long hardChange, long mediumChange, long softChange) {
        hardScoreUpdater.accept(hardChange);
        mediumScoreUpdater.accept(mediumChange);
        softScoreUpdater.accept(softChange);
        return () -> {
            hardScoreUpdater.accept(-hardChange);
            mediumScoreUpdater.accept(-mediumChange);
            softScoreUpdater.accept(-softChange);
        };
    }

}
