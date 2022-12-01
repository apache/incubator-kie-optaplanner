package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class BendableBigDecimalScoreInliner extends AbstractScoreInliner<BendableBigDecimalScore> {

    private final BigDecimal[] hardScores;
    private final BigDecimal[] softScores;

    BendableBigDecimalScoreInliner(boolean constraintMatchEnabled, int hardLevelsSize, int softLevelsSize) {
        super(constraintMatchEnabled);
        hardScores = new BigDecimal[hardLevelsSize];
        Arrays.fill(hardScores, BigDecimal.ZERO);
        softScores = new BigDecimal[softLevelsSize];
        Arrays.fill(softScores, BigDecimal.ZERO);
    }

    @Override
    public WeightedScoreImpacter<BendableBigDecimalScore, BendableBigDecimalScoreContext> buildWeightedScoreImpacter(
            Constraint constraint, BendableBigDecimalScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        Integer singleLevel = null;
        for (int i = 0; i < constraintWeight.getLevelsSize(); i++) {
            if (!constraintWeight.getHardOrSoftScore(i).equals(BigDecimal.ZERO)) {
                if (singleLevel != null) {
                    singleLevel = null;
                    break;
                }
                singleLevel = i;
            }
        }
        BendableBigDecimalScoreContext context = new BendableBigDecimalScoreContext(constraint, constraintWeight,
                constraintMatchEnabled, (level, impact) -> this.hardScores[level] = this.hardScores[level].add(impact),
                (level, impact) -> this.softScores[level] = this.softScores[level].add(impact));
        if (singleLevel != null) {
            BigDecimal levelWeight = constraintWeight.getHardOrSoftScore(singleLevel);
            if (singleLevel < constraintWeight.getHardLevelsSize()) {
                int level = singleLevel;
                return WeightedScoreImpacter.of(context,
                        (BendableBigDecimalScoreContext ctx, BigDecimal matchWeight,
                                JustificationsSupplier justificationsSupplier) -> {
                            BigDecimal hardImpact = levelWeight.multiply(matchWeight);
                            UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(level, hardImpact);
                            if (!ctx.isConstraintMatchEnabled()) {
                                return undoScoreImpact;
                            }
                            return impactWithConstraintMatch(ctx, undoScoreImpact,
                                    BendableBigDecimalScore.ofHard(hardScores.length, softScores.length, level, hardImpact),
                                    justificationsSupplier);
                        });
            } else {
                int level = singleLevel - constraintWeight.getHardLevelsSize();
                return WeightedScoreImpacter.of(context,
                        (BendableBigDecimalScoreContext ctx, BigDecimal matchWeight,
                                JustificationsSupplier justificationsSupplier) -> {
                            BigDecimal softImpact = levelWeight.multiply(matchWeight);
                            UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(level, softImpact);
                            if (!ctx.isConstraintMatchEnabled()) {
                                return undoScoreImpact;
                            }
                            return impactWithConstraintMatch(ctx, undoScoreImpact,
                                    BendableBigDecimalScore.ofSoft(hardScores.length, softScores.length, level, softImpact),
                                    justificationsSupplier);
                        });
            }
        } else {
            return WeightedScoreImpacter.of(context,
                    (BendableBigDecimalScoreContext ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal[] hardImpacts = new BigDecimal[hardScores.length];
                        BigDecimal[] softImpacts = new BigDecimal[softScores.length];
                        for (int i = 0; i < hardImpacts.length; i++) {
                            hardImpacts[i] = ctx.getConstraintWeight().getHardScore(i).multiply(matchWeight);
                        }
                        for (int i = 0; i < softImpacts.length; i++) {
                            softImpacts[i] = ctx.getConstraintWeight().getSoftScore(i).multiply(matchWeight);
                        }
                        UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(hardImpacts, softImpacts);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                BendableBigDecimalScore.of(hardImpacts, softImpacts), justificationsSupplier);
                    });
        }
    }

    @Override
    public BendableBigDecimalScore extractScore(int initScore) {
        return BendableBigDecimalScore.ofUninitialized(initScore,
                Arrays.copyOf(hardScores, hardScores.length),
                Arrays.copyOf(softScores, softScores.length));
    }

    @Override
    public String toString() {
        return BendableBigDecimalScore.class.getSimpleName() + " inliner";
    }

}
