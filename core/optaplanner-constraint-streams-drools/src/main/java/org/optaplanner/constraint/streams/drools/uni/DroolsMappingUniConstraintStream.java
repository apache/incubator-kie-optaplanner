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

public final class DroolsMappingUniConstraintStream<Solution_, NewA>
        extends DroolsAbstractUniConstraintStream<Solution_, NewA> {

    private final Supplier<UniLeftHandSide<NewA>> leftHandSide;

    public <A> DroolsMappingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> mapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function1<A, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(mapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andMap(convertedMapping);
    }

    public <A, B> DroolsMappingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> mapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function2<A, B, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(mapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andMap(convertedMapping);
    }

    public <A, B, C> DroolsMappingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> mapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function3<A, B, C, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(mapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andMap(convertedMapping);
    }

    public <A, B, C, D> DroolsMappingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent, QuadFunction<A, B, C, D, NewA> mapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function4<A, B, C, D, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(mapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andMap(convertedMapping);
    }

    @Override
    public boolean guaranteesDistinct() {
        return false; // map() can never guarantee distinct tuples, as we do not see inside of the mapping function.
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
        return "Map() with " + getChildStreams().size() + " children";
    }

}
