package org.optaplanner.constraint.streams.common.inliner;

import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class BendableScoreInliner extends AbstractScoreInliner<BendableScore> {

    private final int[] hardScores;
    private final int[] softScores;

    BendableScoreInliner(boolean constraintMatchEnabled, int hardLevelsSize, int softLevelsSize) {
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
        if (singleLevel != null) {
            int levelWeight = constraintWeight.getHardOrSoftScore(singleLevel);
            if (singleLevel < constraintWeight.getHardLevelsSize()) {
                int level = singleLevel;
                BendableScoreContext context = new BendableScoreContext(this, constraint, constraintWeight, hardScores.length,
                        softScores.length, level, levelWeight,
                        (scoreLevel, impact) -> this.hardScores[scoreLevel] += impact,
                        (scoreLevel, impact) -> this.softScores[scoreLevel] += impact);
                return WeightedScoreImpacter.of(context, BendableScoreContext::changeHardScoreBy);
            } else {
                int level = singleLevel - constraintWeight.getHardLevelsSize();
                BendableScoreContext context = new BendableScoreContext(this, constraint, constraintWeight, hardScores.length,
                        softScores.length, level, levelWeight,
                        (scoreLevel, impact) -> this.hardScores[scoreLevel] += impact,
                        (scoreLevel, impact) -> this.softScores[scoreLevel] += impact);
                return WeightedScoreImpacter.of(context, BendableScoreContext::changeSoftScoreBy);
            }
        } else {
            BendableScoreContext context = new BendableScoreContext(this, constraint, constraintWeight, hardScores.length,
                    softScores.length,
                    (scoreLevel, impact) -> this.hardScores[scoreLevel] += impact,
                    (scoreLevel, impact) -> this.softScores[scoreLevel] += impact);
            return WeightedScoreImpacter.of(context, BendableScoreContext::changeScoreBy);
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
