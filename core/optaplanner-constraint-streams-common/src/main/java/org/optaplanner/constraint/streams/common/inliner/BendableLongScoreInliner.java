package org.optaplanner.constraint.streams.common.inliner;

import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

public final class BendableLongScoreInliner extends AbstractScoreInliner<BendableLongScore> {

    private final long[] hardScores;
    private final long[] softScores;

    BendableLongScoreInliner(boolean constraintMatchEnabled, int hardLevelsSize, int softLevelsSize) {
        super(constraintMatchEnabled);
        hardScores = new long[hardLevelsSize];
        softScores = new long[softLevelsSize];
    }

    @Override
    public WeightedScoreImpacter<BendableLongScore, BendableLongScoreContext> buildWeightedScoreImpacter(Constraint constraint,
            BendableLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        Integer singleLevel = null;
        for (int i = 0; i < constraintWeight.getLevelsSize(); i++) {
            if (constraintWeight.getHardOrSoftScore(i) != 0L) {
                if (singleLevel != null) {
                    singleLevel = null;
                    break;
                }
                singleLevel = i;
            }
        }
        BendableLongScoreContext context = new BendableLongScoreContext(constraint, constraintWeight,
                constraintMatchEnabled, (level, impact) -> this.hardScores[level] += impact,
                (level, impact) -> this.softScores[level] += impact);
        if (singleLevel != null) {
            long levelWeight = constraintWeight.getHardOrSoftScore(singleLevel);
            if (singleLevel < constraintWeight.getHardLevelsSize()) {
                int level = singleLevel;
                return WeightedScoreImpacter.of(context,
                        (BendableLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                            long hardImpact = levelWeight * matchWeight;
                            UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(level, hardImpact);
                            if (!ctx.isConstraintMatchEnabled()) {
                                return undoScoreImpact;
                            }
                            return impactWithConstraintMatch(ctx, undoScoreImpact,
                                    BendableLongScore.ofHard(hardScores.length, softScores.length, level, hardImpact),
                                    justificationsSupplier);
                        });
            } else {
                int level = singleLevel - constraintWeight.getHardLevelsSize();
                return WeightedScoreImpacter.of(context,
                        (BendableLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                            long softImpact = levelWeight * matchWeight;
                            UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(level, softImpact);
                            if (!ctx.isConstraintMatchEnabled()) {
                                return undoScoreImpact;
                            }
                            return impactWithConstraintMatch(ctx, undoScoreImpact,
                                    BendableLongScore.ofSoft(hardScores.length, softScores.length, level, softImpact),
                                    justificationsSupplier);
                        });
            }
        } else {
            return WeightedScoreImpacter.of(context,
                    (BendableLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long[] hardImpacts = new long[hardScores.length];
                        long[] softImpacts = new long[softScores.length];
                        for (int i = 0; i < hardImpacts.length; i++) {
                            hardImpacts[i] = ctx.getConstraintWeight().getHardScore(i) * matchWeight;
                        }
                        for (int i = 0; i < softImpacts.length; i++) {
                            softImpacts[i] = ctx.getConstraintWeight().getSoftScore(i) * matchWeight;
                        }
                        UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(hardImpacts, softImpacts);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, BendableLongScore.of(hardImpacts, softImpacts),
                                justificationsSupplier);
                    });
        }
    }

    @Override
    public BendableLongScore extractScore(int initScore) {
        return BendableLongScore.ofUninitialized(initScore,
                Arrays.copyOf(hardScores, hardScores.length),
                Arrays.copyOf(softScores, softScores.length));
    }

    @Override
    public String toString() {
        return BendableLongScore.class.getSimpleName() + " inliner";
    }

}
