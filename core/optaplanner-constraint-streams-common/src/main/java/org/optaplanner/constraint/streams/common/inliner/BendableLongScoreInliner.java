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
        if (singleLevel != null) {
            long levelWeight = constraintWeight.getHardOrSoftScore(singleLevel);
            if (singleLevel < constraintWeight.getHardLevelsSize()) {
                BendableLongScoreContext context = new BendableLongScoreContext(this, constraint, constraintWeight,
                        hardScores.length, softScores.length, singleLevel, levelWeight,
                        (scoreLevel, impact) -> this.hardScores[scoreLevel] += impact,
                        (scoreLevel, impact) -> this.softScores[scoreLevel] += impact);
                return WeightedScoreImpacter.of(context,
                        (BendableLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> ctx
                                .changeHardScoreBy(matchWeight, justificationsSupplier));
            } else {
                int level = singleLevel - constraintWeight.getHardLevelsSize();
                BendableLongScoreContext context = new BendableLongScoreContext(this, constraint, constraintWeight,
                        hardScores.length, softScores.length, level, levelWeight,
                        (scoreLevel, impact) -> this.hardScores[scoreLevel] += impact,
                        (scoreLevel, impact) -> this.softScores[scoreLevel] += impact);
                return WeightedScoreImpacter.of(context,
                        (BendableLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> ctx
                                .changeSoftScoreBy(matchWeight, justificationsSupplier));
            }
        } else {
            BendableLongScoreContext context = new BendableLongScoreContext(this, constraint, constraintWeight,
                    hardScores.length, softScores.length,
                    (scoreLevel, impact) -> this.hardScores[scoreLevel] += impact,
                    (scoreLevel, impact) -> this.softScores[scoreLevel] += impact);
            return WeightedScoreImpacter.of(context,
                    (BendableLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> ctx
                            .changeScoreBy(matchWeight, justificationsSupplier));
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
