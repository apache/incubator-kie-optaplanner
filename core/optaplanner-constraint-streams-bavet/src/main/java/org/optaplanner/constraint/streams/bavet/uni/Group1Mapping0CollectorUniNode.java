package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.core.config.solver.EnvironmentMode;

final class Group1Mapping0CollectorUniNode<OldA, A>
        extends AbstractGroupUniNode<OldA, UniTuple<A>, A, Void, Void> {

    private final int outputStoreSize;

    public Group1Mapping0CollectorUniNode(Function<OldA, A> groupKeyMapping, int groupStoreIndex,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple), nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, OldA> A createGroupKey(Function<OldA, A> groupKeyMapping, UniTuple<OldA> tuple) {
        return groupKeyMapping.apply(tuple.getA());
    }

    @Override
    protected UniTuple<A> createOutTuple(A a) {
        return UniTuple.of(a, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(UniTuple<A> aUniTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
