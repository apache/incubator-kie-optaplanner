package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class BendableScoreContext extends ScoreContext<BendableScore> {

    private final IntBiConsumer softScoreLevelUpdater;
    private final IntBiConsumer hardScoreLevelUpdater;

    public BendableScoreContext(Constraint constraint, BendableScore constraintWeight, boolean constraintMatchEnabled,
            IntBiConsumer hardScoreLevelUpdater, IntBiConsumer softScoreLevelUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreLevelUpdater = softScoreLevelUpdater;
        this.hardScoreLevelUpdater = hardScoreLevelUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(int level, int change) {
        softScoreLevelUpdater.accept(level, change);
        return () -> softScoreLevelUpdater.accept(level, -change);
    }

    public UndoScoreImpacter changeHardScoreBy(int level, int change) {
        hardScoreLevelUpdater.accept(level, change);
        return () -> hardScoreLevelUpdater.accept(level, -change);
    }

    public UndoScoreImpacter changeScoreBy(int[] hardChanges, int[] softChanges) {
        for (int i = 0; i < hardChanges.length; i++) {
            hardScoreLevelUpdater.accept(i, hardChanges[i]);
        }
        for (int i = 0; i < softChanges.length; i++) {
            softScoreLevelUpdater.accept(i, softChanges[i]);
        }
        return () -> {
            for (int i = 0; i < hardChanges.length; i++) {
                hardScoreLevelUpdater.accept(i, -hardChanges[i]);
            }
            for (int i = 0; i < softChanges.length; i++) {
                softScoreLevelUpdater.accept(i, -softChanges[i]);
            }
        };
    }

    public interface IntBiConsumer {

        void accept(int value1, int value2);

    }

}
