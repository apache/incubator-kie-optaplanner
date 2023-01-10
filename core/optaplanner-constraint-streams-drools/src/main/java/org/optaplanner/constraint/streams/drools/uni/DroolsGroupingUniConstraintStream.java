package org.optaplanner.constraint.streams.drools.uni;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.drools.model.functions.Function1;
import org.drools.model.functions.Function2;
import org.drools.model.functions.Function3;
import org.drools.model.functions.Function4;
import org.optaplanner.constraint.streams.drools.DroolsConstraintFactory;
import org.optaplanner.constraint.streams.drools.bi.DroolsAbstractBiConstraintStream;
import org.optaplanner.constraint.streams.drools.common.UniLeftHandSide;
import org.optaplanner.constraint.streams.drools.quad.DroolsAbstractQuadConstraintStream;
import org.optaplanner.constraint.streams.drools.tri.DroolsAbstractTriConstraintStream;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

public final class DroolsGroupingUniConstraintStream<Solution_, NewA>
        extends DroolsAbstractUniConstraintStream<Solution_, NewA> {

    private final Supplier<UniLeftHandSide<NewA>> leftHandSide;

    public <A> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function1<A, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(groupKeyMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedMapping);
    }

    public <A> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, UniConstraintCollector<A, ?, NewA> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collector);
    }

    public <A, B> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function2<A, B, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(groupKeyMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedMapping);
    }

    public <A, B> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiConstraintCollector<A, B, ?, NewA> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collector);
    }

    public <A, B, C> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent,
            TriConstraintCollector<A, B, C, ?, NewA> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collector);
    }

    public <A, B, C> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function3<A, B, C, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(groupKeyMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedMapping);
    }

    public <A, B, C, D> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadConstraintCollector<A, B, C, D, ?, NewA> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collector);
    }

    public <A, B, C, D> DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function4<A, B, C, D, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(groupKeyMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedMapping);
    }

    @Override
    public boolean guaranteesDistinct() {
        return true;
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public UniLeftHandSide<NewA> createLeftHandSide() {
        return leftHandSide.get();
    }

    @Override
    public String toString() {
        return "UniGroup() with " + getChildStreams().size() + " children.";
    }
}
