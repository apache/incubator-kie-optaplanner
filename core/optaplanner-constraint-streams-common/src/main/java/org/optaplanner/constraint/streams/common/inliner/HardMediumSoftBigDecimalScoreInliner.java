package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftBigDecimalScoreInliner extends AbstractScoreInliner<HardMediumSoftBigDecimalScore> {

    private BigDecimal hardScore = BigDecimal.ZERO;
    private BigDecimal mediumScore = BigDecimal.ZERO;
    private BigDecimal softScore = BigDecimal.ZERO;

    HardMediumSoftBigDecimalScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<HardMediumSoftBigDecimalScore, HardMediumSoftBigDecimalScoreContext>
            buildWeightedScoreImpacter(Constraint constraint,
                    HardMediumSoftBigDecimalScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        BigDecimal hardConstraintWeight = constraintWeight.getHardScore();
        BigDecimal mediumConstraintWeight = constraintWeight.getMediumScore();
        BigDecimal softConstraintWeight = constraintWeight.getSoftScore();
        HardMediumSoftBigDecimalScoreContext context =
                new HardMediumSoftBigDecimalScoreContext(constraint, constraintWeight, constraintMatchEnabled,
                        impact -> this.hardScore = this.hardScore.add(impact),
                        impact -> this.mediumScore = this.mediumScore.add(impact),
                        impact -> this.softScore = this.softScore.add(impact));
        if (mediumConstraintWeight.equals(BigDecimal.ZERO) && softConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftBigDecimalScoreContext ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal hardImpact = ctx.getConstraintWeight().getHardScore().multiply(matchWeight);
                        UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(hardImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftBigDecimalScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO) && softConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftBigDecimalScoreContext ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal mediumImpact = ctx.getConstraintWeight().getMediumScore().multiply(matchWeight);
                        UndoScoreImpacter undoScoreImpact = ctx.changeMediumScoreBy(mediumImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                HardMediumSoftBigDecimalScore.ofMedium(mediumImpact), justificationsSupplier);
                    });
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO) && mediumConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftBigDecimalScoreContext ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal softImpact = ctx.getConstraintWeight().getSoftScore().multiply(matchWeight);
                        UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftBigDecimalScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftBigDecimalScoreContext ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal hardImpact = ctx.getConstraintWeight().getHardScore().multiply(matchWeight);
                        BigDecimal mediumImpact = ctx.getConstraintWeight().getMediumScore().multiply(matchWeight);
                        BigDecimal softImpact = ctx.getConstraintWeight().getSoftScore().multiply(matchWeight);
                        UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(hardImpact, mediumImpact, softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                HardMediumSoftBigDecimalScore.of(hardImpact, mediumImpact, softImpact), justificationsSupplier);
                    });
        }
    }

    @Override
    public HardMediumSoftBigDecimalScore extractScore(int initScore) {
        return HardMediumSoftBigDecimalScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

    @Override
    public String toString() {
        return HardMediumSoftBigDecimalScore.class.getSimpleName() + " inliner";
    }

}
