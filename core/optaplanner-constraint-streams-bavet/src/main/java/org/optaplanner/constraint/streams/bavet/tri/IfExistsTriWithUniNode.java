package org.optaplanner.constraint.streams.bavet.tri;

import java.util.Set;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.TriFunction;

final class IfExistsTriWithUniNode<A, B, C, D> extends AbstractIfExistsNode<TriTupleImpl<A, B, C>, D> {

    private final TriFunction<A, B, C, IndexProperties> mappingABC;
    private final QuadPredicate<A, B, C, D> filtering;

    public IfExistsTriWithUniNode(boolean shouldExist,
            TriFunction<A, B, C, IndexProperties> mappingABC, Function<D, IndexProperties> mappingD,
            int inputStoreIndexABC, int inputStoreIndexD,
            TupleLifecycle<TriTupleImpl<A, B, C>> nextNodesTupleLifecycle,
            Indexer<TriTupleImpl<A, B, C>, Counter<TriTupleImpl<A, B, C>>> indexerABC,
            Indexer<UniTupleImpl<D>, Set<Counter<TriTupleImpl<A, B, C>>>> indexerD,
            QuadPredicate<A, B, C, D> filtering) {
        super(shouldExist, mappingD, inputStoreIndexABC, inputStoreIndexD, nextNodesTupleLifecycle, indexerABC,
                indexerD);
        this.mappingABC = mappingABC;
        this.filtering = filtering;
    }

    @Override
    protected IndexProperties createIndexProperties(TriTupleImpl<A, B, C> abcTriTuple) {
        return mappingABC.apply(abcTriTuple.factA, abcTriTuple.factB, abcTriTuple.factC);
    }

    @Override
    protected boolean isFiltering() {
        return filtering != null;
    }

    @Override
    protected boolean isFiltered(TriTupleImpl<A, B, C> abcTriTuple, UniTupleImpl<D> rightTuple) {
        return filtering.test(abcTriTuple.factA, abcTriTuple.factB, abcTriTuple.factC, rightTuple.factA);
    }

    @Override
    public String toString() {
        return "IfExistsTriWithUniNode";
    }

}
