package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftBigDecimalScoreInliner extends AbstractScoreInliner<HardSoftBigDecimalScore> {

    private BigDecimal hardScore = BigDecimal.ZERO;
    private BigDecimal softScore = BigDecimal.ZERO;

    HardSoftBigDecimalScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<HardSoftBigDecimalScore> buildWeightedScoreImpacter(Constraint constraint,
            HardSoftBigDecimalScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        ScoreImpacterContext<HardSoftBigDecimalScore> context =
                new ScoreImpacterContext<>(constraint, constraintWeight, constraintMatchEnabled);
        if (constraintWeight.getSoftScore().equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftBigDecimalScore> ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal hardImpact = ctx.getConstraintWeight().getHardScore().multiply(matchWeight);
                        this.hardScore = this.hardScore.add(hardImpact);
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore = this.hardScore.subtract(hardImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftBigDecimalScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (constraintWeight.getHardScore().equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftBigDecimalScore> ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal softImpact = ctx.getConstraintWeight().getSoftScore().multiply(matchWeight);
                        this.softScore = this.softScore.add(softImpact);
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore = this.softScore.subtract(softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftBigDecimalScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftBigDecimalScore> ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal hardImpact = ctx.getConstraintWeight().getHardScore().multiply(matchWeight);
                        BigDecimal softImpact = ctx.getConstraintWeight().getSoftScore().multiply(matchWeight);
                        this.hardScore = this.hardScore.add(hardImpact);
                        this.softScore = this.softScore.add(softImpact);
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore = this.hardScore.subtract(hardImpact);
                            this.softScore = this.softScore.subtract(softImpact);
                        };
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                HardSoftBigDecimalScore.of(hardImpact, softImpact), justificationsSupplier);
                    });
        }
    }

    @Override
    public HardSoftBigDecimalScore extractScore(int initScore) {
        return HardSoftBigDecimalScore.ofUninitialized(initScore, hardScore, softScore);
    }

    @Override
    public String toString() {
        return HardSoftBigDecimalScore.class.getSimpleName() + " inliner";
    }

}
