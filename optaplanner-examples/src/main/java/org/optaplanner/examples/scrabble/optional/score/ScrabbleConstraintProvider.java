package org.optaplanner.examples.scrabble.optional.score;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.score.stream.bi.FilteringBiJoiner;
import org.optaplanner.examples.scrabble.domain.ScrabbleWordDirection;
import org.optaplanner.examples.scrabble.domain.ScrabbleCell;
import org.optaplanner.examples.scrabble.domain.ScrabbleWordAssignment;

public class ScrabbleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                                characterConflict(constraintFactory),
                                noParallelHorizontalNeighbours(constraintFactory),
                                noParallelVerticalNeighbours(constraintFactory),
                                outOfGrid(constraintFactory),
                                maximizeMergesPerWord(constraintFactory),
                                pullToCenter(constraintFactory)
        };
    }

    private Constraint characterConflict(ConstraintFactory cf) {
        return cf.from(ScrabbleCell.class)
                 .filter(sc -> sc.getCharacterSet().size() >= 2)
                 .penalize("Character confict", HardMediumSoftScore.ONE_HARD, sc -> 1 - sc.getCharacterSet().size());
    }

    private Constraint noParallelHorizontalNeighbours(ConstraintFactory cf) {
        return cf.fromUniquePair(ScrabbleCell.class)
                 .filter((c1, c2) -> c1.getX() == c2.getX() &&
                                     Math.abs(c1.getY() - c2.getY()) == 1 &&
                                     c1.hasWordSet(ScrabbleWordDirection.HORIZONTAL) &&
                                     c2.hasWordSet(ScrabbleWordDirection.HORIZONTAL))
                 .penalize("No parallel horizontal neighbours", HardMediumSoftScore.ONE_HARD);
    }

    private Constraint noParallelVerticalNeighbours(ConstraintFactory cf) {
        return cf.fromUniquePair(ScrabbleCell.class)
                 .filter((c1, c2) -> c1.getY() == c2.getY() &&
                                     Math.abs(c1.getX() - c2.getX()) == 1 &&
                                     c1.hasWordSet(ScrabbleWordDirection.VERTICAL) &&
                                     c2.hasWordSet(ScrabbleWordDirection.VERTICAL))
                 .penalize("No parallel vertical neighbours", HardMediumSoftScore.ONE_HARD);
    }

    private Constraint outOfGrid(ConstraintFactory cf) {
        return cf.from(ScrabbleWordAssignment.class)
                 .filter(ScrabbleWordAssignment::isOutOfGrid)
                 .penalize("Out of grid", HardMediumSoftScore.ONE_HARD, swa -> swa.getWord().length());
    }

    private Constraint maximizeMergesPerWord(ConstraintFactory cf) {
        return cf.from(ScrabbleWordAssignment.class)
                 .join(ScrabbleCell.class, new FilteringBiJoiner<>((swa, sc) -> sc.getWordSet().contains(swa) && sc.hasMerge()))
                 .groupBy((swa, sc) -> swa.getId(), ConstraintCollectors.countBi())
                 .penalize("Maximize merges per word", HardMediumSoftScore.ONE_MEDIUM, (id, count) -> count * count);
    }

    private Constraint pullToCenter(ConstraintFactory cf) {
        return cf.from(ScrabbleWordAssignment.class)
                 .penalize("Pull to the center", HardMediumSoftScore.ONE_SOFT, (swa) -> swa.getDistanceToCenter());
    }

}
