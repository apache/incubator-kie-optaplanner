package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.AbstractInserter;
import org.optaplanner.core.api.function.TriPredicate;

import java.util.function.Consumer;

final class ConditionalTriInserter<A, B, C> extends AbstractInserter<TriTuple<A, B, C>> {
    private final TriPredicate<A, B, C> predicate;

    public ConditionalTriInserter(TriPredicate<A, B, C> predicate, Consumer<TriTuple<A, B, C>> insert) {
        super(insert);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(TriTuple<A, B, C> tuple) {
        return predicate.test(tuple.factA, tuple.factB, tuple.factC);
    }

}
