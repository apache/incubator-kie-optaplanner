package org.optaplanner.constraint.streams.bavet.quad;

import static org.optaplanner.constraint.streams.common.inliner.JustificationsSupplier.of;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import org.optaplanner.constraint.streams.bavet.BavetConstraint;
import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.BavetScoringConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.NodeBuildHelper;
import org.optaplanner.constraint.streams.common.inliner.AbstractScoreInliner;
import org.optaplanner.constraint.streams.common.inliner.UndoScoreImpacter;
import org.optaplanner.constraint.streams.common.inliner.WeightedScoreImpacter;
import org.optaplanner.core.api.function.PentaFunction;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.ConstraintStream;

public final class BavetScoringQuadConstraintStream<Solution_, A, B, C, D>
        extends BavetAbstractQuadConstraintStream<Solution_, A, B, C, D>
        implements BavetScoringConstraintStream<Solution_> {

    private final BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent;
    private final boolean noMatchWeigher;
    private final ToIntQuadFunction<A, B, C, D> intMatchWeigher;
    private final ToLongQuadFunction<A, B, C, D> longMatchWeigher;
    private final QuadFunction<A, B, C, D, BigDecimal> bigDecimalMatchWeigher;
    private BavetConstraint<Solution_> constraint;

    public BavetScoringQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent) {
        this(constraintFactory, parent, true, null, null, null);
    }

    public BavetScoringQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            ToIntQuadFunction<A, B, C, D> intMatchWeigher) {
        this(constraintFactory, parent, false, intMatchWeigher, null, null);
        if (intMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    public BavetScoringQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            ToLongQuadFunction<A, B, C, D> longMatchWeigher) {
        this(constraintFactory, parent, false, null, longMatchWeigher, null);
        if (longMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    public BavetScoringQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, BigDecimal> bigDecimalMatchWeigher) {
        this(constraintFactory, parent, false, null, null, bigDecimalMatchWeigher);
        if (bigDecimalMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    private BavetScoringQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            boolean noMatchWeigher,
            ToIntQuadFunction<A, B, C, D> intMatchWeigher, ToLongQuadFunction<A, B, C, D> longMatchWeigher,
            QuadFunction<A, B, C, D, BigDecimal> bigDecimalMatchWeigher) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.parent = parent;
        this.noMatchWeigher = noMatchWeigher;
        this.intMatchWeigher = intMatchWeigher;
        this.longMatchWeigher = longMatchWeigher;
        this.bigDecimalMatchWeigher = bigDecimalMatchWeigher;
    }

    @Override
    public void setConstraint(BavetConstraint<Solution_> constraint) {
        this.constraint = constraint;
    }

    @Override
    public boolean guaranteesDistinct() {
        return parent.guaranteesDistinct();
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public void collectActiveConstraintStreams(Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet) {
        parent.collectActiveConstraintStreams(constraintStreamSet);
        constraintStreamSet.add(this);
    }

    @Override
    public ConstraintStream getTupleSource() {
        return parent.getTupleSource();
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        Score_ constraintWeight = buildHelper.getConstraintWeight(constraint);
        AbstractScoreInliner<Score_> scoreInliner = buildHelper.getScoreInliner();
        WeightedScoreImpacter weightedScoreImpacter = scoreInliner.buildWeightedScoreImpacter(constraint, constraintWeight);
        PentaFunction<A, B, C, D, Score<?>, ConstraintJustification> justificationMapping =
                constraint.getJustificationMapping();
        QuadFunction<A, B, C, D, Collection<Object>> indictedObjectsMapping = constraint.getIndictedObjectsMapping();
        QuadFunction<A, B, C, D, UndoScoreImpacter> scoreImpacter;
        if (intMatchWeigher != null) {
            scoreImpacter = (a, b, c, d) -> {
                int matchWeight = intMatchWeigher.applyAsInt(a, b, c, d);
                constraint.assertCorrectImpact(matchWeight);
                return weightedScoreImpacter.impactScore(matchWeight,
                        of(constraint, justificationMapping, indictedObjectsMapping, a, b, c, d));
            };
        } else if (longMatchWeigher != null) {
            scoreImpacter = (a, b, c, d) -> {
                long matchWeight = longMatchWeigher.applyAsLong(a, b, c, d);
                constraint.assertCorrectImpact(matchWeight);
                return weightedScoreImpacter.impactScore(matchWeight,
                        of(constraint, justificationMapping, indictedObjectsMapping, a, b, c, d));
            };
        } else if (bigDecimalMatchWeigher != null) {
            scoreImpacter = (a, b, c, d) -> {
                BigDecimal matchWeight = bigDecimalMatchWeigher.apply(a, b, c, d);
                constraint.assertCorrectImpact(matchWeight);
                return weightedScoreImpacter.impactScore(matchWeight,
                        of(constraint, justificationMapping, indictedObjectsMapping, a, b, c, d));
            };
        } else if (noMatchWeigher) {
            scoreImpacter = (a, b, c, d) -> weightedScoreImpacter.impactScore(1,
                    of(constraint, justificationMapping, indictedObjectsMapping, a, b, c, d));
        } else {
            throw new IllegalStateException("Impossible state: neither of the supported match weighers provided.");
        }
        QuadScorer<A, B, C, D> scorer = new QuadScorer<>(constraint.getConstraintPackage(), constraint.getConstraintName(),
                constraintWeight, scoreImpacter, buildHelper.reserveTupleStoreIndex(parent.getTupleSource()));
        buildHelper.putInsertUpdateRetract(this, scorer);
    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    // No node sharing

    @Override
    public String toString() {
        return "Scoring(" + constraint.getConstraintName() + ")";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

}
