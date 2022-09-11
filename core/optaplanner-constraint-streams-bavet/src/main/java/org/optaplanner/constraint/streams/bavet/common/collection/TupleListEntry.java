package org.optaplanner.constraint.streams.bavet.common.collection;

import org.optaplanner.constraint.streams.bavet.common.Tuple;

/**
 * An entry of {@link TupleList}
 *
 * @param <Tuple_> the tuple type
 */
public final class TupleListEntry<Tuple_ extends Tuple> {

    private final TupleList<Tuple_> tupleList;
    private final Tuple_ tuple;
    TupleListEntry<Tuple_> previous;
    TupleListEntry<Tuple_> next;

    TupleListEntry(TupleList<Tuple_> tupleList, Tuple_ tuple, TupleListEntry<Tuple_> previous) {
        this.tupleList = tupleList;
        this.tuple = tuple;
        this.previous = previous;
        this.next = null;
    }

    public TupleListEntry<Tuple_> next() {
        return next;
    }

    public void remove() {
        tupleList.remove(this);
    }

    public Tuple_ getTuple() {
        return tuple;
    }

    @Override
    public String toString() {
        return tuple.toString();
    }

}
