package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractFlattenLastNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class FlattenLastUniNode<A, NewA> extends AbstractFlattenLastNode<UniTupleImpl<A>, UniTupleImpl<NewA>, A, NewA> {

    private final int outputStoreSize;

    FlattenLastUniNode(int flattenLastStoreIndex, Function<A, Iterable<NewA>> mappingFunction,
            TupleLifecycle<UniTupleImpl<NewA>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(flattenLastStoreIndex, mappingFunction, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected UniTupleImpl<NewA> createTuple(UniTupleImpl<A> originalTuple, NewA item) {
        return new UniTupleImpl<>(item, outputStoreSize);
    }

    @Override
    protected A getEffectiveFactIn(UniTupleImpl<A> tuple) {
        return tuple.factA;
    }

    @Override
    protected NewA getEffectiveFactOut(UniTupleImpl<NewA> outTuple) {
        return outTuple.factA;
    }

}
