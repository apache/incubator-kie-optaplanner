package org.optaplanner.constraint.streams.common.inliner;

import java.util.function.IntConsumer;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftScoreContext extends ScoreContext<HardSoftScore> {

    private final IntConsumer softScoreUpdater;
    private final IntConsumer hardScoreUpdater;

    public HardSoftScoreContext(Constraint constraint, HardSoftScore constraintWeight, boolean constraintMatchEnabled,
            IntConsumer hardScoreUpdater, IntConsumer softScoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreUpdater = softScoreUpdater;
        this.hardScoreUpdater = hardScoreUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(int change) {
        softScoreUpdater.accept(change);
        return () -> softScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeHardScoreBy(int change) {
        hardScoreUpdater.accept(change);
        return () -> hardScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeScoreBy(int hardChange, int softChange) {
        hardScoreUpdater.accept(hardChange);
        softScoreUpdater.accept(softChange);
        return () -> {
            hardScoreUpdater.accept(-hardChange);
            softScoreUpdater.accept(-softChange);
        };
    }

}
