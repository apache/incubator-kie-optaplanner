package org.optaplanner.constraint.streams.drools.quad;

import java.util.function.Function;
import java.util.function.Supplier;

import org.drools.model.functions.Function1;
import org.optaplanner.constraint.streams.drools.DroolsConstraintFactory;
import org.optaplanner.constraint.streams.drools.common.QuadLeftHandSide;

public final class DroolsFlatteningQuadConstraintStream<Solution_, A, B, C, NewD>
        extends DroolsAbstractQuadConstraintStream<Solution_, A, B, C, NewD> {

    private final Supplier<QuadLeftHandSide<A, B, C, NewD>> leftHandSide;

    public <D> DroolsFlatteningQuadConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent, Function<D, Iterable<NewD>> quadMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function1<D, Iterable<NewD>> convertedMapping = constraintFactory.getInternalsFactory().convert(quadMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andFlattenLast(convertedMapping);
    }

    @Override
    public boolean guaranteesDistinct() {
        return false; // flattening can never guarantee distinct tuples, as we do not see inside the Iterable.
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public QuadLeftHandSide<A, B, C, NewD> createLeftHandSide() {
        return leftHandSide.get();
    }

    @Override
    public String toString() {
        return "QuadFlatten() with " + getChildStreams().size() + " children";
    }

}
