package org.optaplanner.constraint.streams.common.inliner;

import java.util.function.IntConsumer;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftScoreContext extends ScoreContext<HardMediumSoftScore> {

    private final IntConsumer softScoreUpdater;
    private final IntConsumer mediumScoreUpdater;
    private final IntConsumer hardScoreUpdater;

    public HardMediumSoftScoreContext(Constraint constraint, HardMediumSoftScore constraintWeight,
            boolean constraintMatchEnabled,
            IntConsumer hardScoreUpdater, IntConsumer mediumScoreUpdater, IntConsumer softScoreUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreUpdater = softScoreUpdater;
        this.mediumScoreUpdater = mediumScoreUpdater;
        this.hardScoreUpdater = hardScoreUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(int change) {
        softScoreUpdater.accept(change);
        return () -> softScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeMediumScoreBy(int change) {
        mediumScoreUpdater.accept(change);
        return () -> mediumScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeHardScoreBy(int change) {
        hardScoreUpdater.accept(change);
        return () -> hardScoreUpdater.accept(-change);
    }

    public UndoScoreImpacter changeScoreBy(int hardChange, int mediumChange, int softChange) {
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
