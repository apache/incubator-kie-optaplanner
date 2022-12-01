package org.optaplanner.constraint.streams.common.inliner;

import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class BendableScoreInliner extends AbstractScoreInliner<BendableScore> {

    private final int[] hardScores;
    private final int[] softScores;

    BendableScoreInliner(boolean constraintMatchEnabled,
            int hardLevelsSize, int softLevelsSize) {
        super(constraintMatchEnabled);
        hardScores = new int[hardLevelsSize];
        softScores = new int[softLevelsSize];
    }

    @Override
    public WeightedScoreImpacter<BendableScore, BendableScoreContext> buildWeightedScoreImpacter(Constraint constraint,
            BendableScore constraintWeight) {
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
        BendableScoreContext context = new BendableScoreContext(constraint, constraintWeight, constraintMatchEnabled,
                (level, impact) -> this.hardScores[level] += impact, (level, impact) -> this.softScores[level] += impact);
        if (singleLevel != null) {
            int levelWeight = constraintWeight.getHardOrSoftScore(singleLevel);
            if (singleLevel < constraintWeight.getHardLevelsSize()) {
                int level = singleLevel;
                return WeightedScoreImpacter.of(context,
                        (BendableScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                            int hardImpact = levelWeight * matchWeight;
                            UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(level, hardImpact);
                            if (!ctx.isConstraintMatchEnabled()) {
                                return undoScoreImpact;
                            }
                            return impactWithConstraintMatch(ctx, undoScoreImpact,
                                    BendableScore.ofHard(hardScores.length, softScores.length, level, hardImpact),
                                    justificationsSupplier);
                        });
            } else {
                int level = singleLevel - constraintWeight.getHardLevelsSize();
                return WeightedScoreImpacter.of(context,
                        (BendableScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                            int softImpact = levelWeight * matchWeight;
                            UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(level, softImpact);
                            if (!ctx.isConstraintMatchEnabled()) {
                                return undoScoreImpact;
                            }
                            return impactWithConstraintMatch(ctx, undoScoreImpact,
                                    BendableScore.ofSoft(hardScores.length, softScores.length, level, softImpact),
                                    justificationsSupplier);
                        });
            }
        } else {
            return WeightedScoreImpacter.of(context,
                    (BendableScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int[] hardImpacts = new int[hardScores.length];
                        int[] softImpacts = new int[softScores.length];
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
                        return impactWithConstraintMatch(ctx, undoScoreImpact, BendableScore.of(hardImpacts, softImpacts),
                                justificationsSupplier);
                    });
        }
    }

    @Override
    public BendableScore extractScore(int initScore) {
        return BendableScore.ofUninitialized(initScore,
                Arrays.copyOf(hardScores, hardScores.length),
                Arrays.copyOf(softScores, softScores.length));
    }

    @Override
    public String toString() {
        return BendableScore.class.getSimpleName() + " inliner";
    }

}
