package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractGroupNode;
import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;

abstract class AbstractGroupBiNode<OldA, OldB, OutTuple_ extends Tuple, MutableOutTuple_ extends OutTuple_, GroupKey_, ResultContainer_, Result_>
        extends AbstractGroupNode<BiTuple<OldA, OldB>, OutTuple_, MutableOutTuple_, GroupKey_, ResultContainer_, Result_> {

    private final TriFunction<ResultContainer_, OldA, OldB, Runnable> accumulator;

    protected AbstractGroupBiNode(int groupStoreIndex, int undoStoreIndex,
            Function<BiTuple<OldA, OldB>, GroupKey_> groupKeyFunction,
            BiConstraintCollector<OldA, OldB, ResultContainer_, Result_> collector,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle) {
        super(groupStoreIndex, undoStoreIndex, groupKeyFunction,
                collector == null ? null : collector.supplier(),
                collector == null ? null : collector.finisher(),
                nextNodesTupleLifecycle);
        accumulator = collector == null ? null : collector.accumulator();
    }

    protected AbstractGroupBiNode(int groupStoreIndex, Function<BiTuple<OldA, OldB>, GroupKey_> groupKeyFunction,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle) {
        super(groupStoreIndex, groupKeyFunction, nextNodesTupleLifecycle);
        accumulator = null;
    }

    @Override
    protected final Runnable accumulate(ResultContainer_ resultContainer, BiTuple<OldA, OldB> tuple) {
        return accumulator.apply(resultContainer, tuple.getFactA(), tuple.getFactB());
    }

}
