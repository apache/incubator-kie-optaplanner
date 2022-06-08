package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.AbstractUpdater;
import org.optaplanner.core.api.function.QuadPredicate;

import java.util.function.Consumer;

final class ConditionalQuadUpdater<A, B, C, D> extends AbstractUpdater<QuadTuple<A, B, C, D>> {
    private final QuadPredicate<A, B, C, D> predicate;

    public ConditionalQuadUpdater(QuadPredicate<A, B, C, D> predicate, Consumer<QuadTuple<A, B, C, D>> update,
                                  Consumer<QuadTuple<A, B, C, D>> retract) {
        super(update, retract);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(QuadTuple<A, B, C, D> tuple) {
        return predicate.test(tuple.factA, tuple.factB, tuple.factC, tuple.factD);
    }
}
