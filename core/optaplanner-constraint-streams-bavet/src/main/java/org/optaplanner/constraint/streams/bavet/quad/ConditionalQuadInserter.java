package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.AbstractInserter;
import org.optaplanner.core.api.function.QuadPredicate;

import java.util.function.Consumer;

final class ConditionalQuadInserter<A, B, C, D> extends AbstractInserter<QuadTuple<A, B, C, D>> {
    private final QuadPredicate<A, B, C, D> predicate;

    public ConditionalQuadInserter(QuadPredicate<A, B, C, D> predicate, Consumer<QuadTuple<A, B, C, D>> insert) {
        super(insert);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(QuadTuple<A, B, C, D> tuple) {
        return predicate.test(tuple.factA, tuple.factB, tuple.factC, tuple.factD);
    }
}
