package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class BendableLongScoreContext extends ScoreContext<BendableLongScore> {

    private final IntLongConsumer softScoreLevelUpdater;
    private final IntLongConsumer hardScoreLevelUpdater;

    public BendableLongScoreContext(Constraint constraint, BendableLongScore constraintWeight, boolean constraintMatchEnabled,
            IntLongConsumer hardScoreLevelUpdater, IntLongConsumer softScoreLevelUpdater) {
        super(constraint, constraintWeight, constraintMatchEnabled);
        this.softScoreLevelUpdater = softScoreLevelUpdater;
        this.hardScoreLevelUpdater = hardScoreLevelUpdater;
    }

    public UndoScoreImpacter changeSoftScoreBy(int level, long change) {
        softScoreLevelUpdater.accept(level, change);
        return () -> softScoreLevelUpdater.accept(level, -change);
    }

    public UndoScoreImpacter changeHardScoreBy(int level, long change) {
        hardScoreLevelUpdater.accept(level, change);
        return () -> hardScoreLevelUpdater.accept(level, -change);
    }

    public UndoScoreImpacter changeScoreBy(long[] hardChanges, long[] softChanges) {
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

    public interface IntLongConsumer {

        void accept(int value1, long value2);

    }

}
