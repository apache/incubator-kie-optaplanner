package org.optaplanner.constraint.streams.bavet.uni;

import org.optaplanner.constraint.streams.bavet.common.AbstractInserter;

import java.util.function.Consumer;
import java.util.function.Predicate;

final class ConditionalUniInserter<A> extends AbstractInserter<UniTuple<A>> {
    private final Predicate<A> predicate;

    public ConditionalUniInserter(Predicate<A> predicate, Consumer<UniTuple<A>> insert) {
        super(insert);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(UniTuple<A> tuple) {
        return predicate.test(tuple.factA);
    }
}
