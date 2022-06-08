package org.optaplanner.constraint.streams.bavet.bi;

import org.optaplanner.constraint.streams.bavet.common.AbstractInserter;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

final class ConditionalBiInserter<A, B> extends AbstractInserter<BiTuple<A, B>> {
    private final BiPredicate<A, B> predicate;

    public ConditionalBiInserter(BiPredicate<A, B> predicate, Consumer<BiTuple<A, B>> insert) {
        super(insert);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(BiTuple<A, B> tuple) {
        return predicate.test(tuple.factA, tuple.factB);
    }

}
