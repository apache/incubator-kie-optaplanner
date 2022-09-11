package org.optaplanner.constraint.streams.bavet.common.collection;

import java.util.LinkedList;
import java.util.List;

import org.optaplanner.constraint.streams.bavet.common.Tuple;

/**
 * Different from {@link LinkedList} because nodes/indexes are allowed
 * to directly reference {@link TupleListEntry} instances
 * to avoid the lookup by index cost.
 * Also doesn't implement the {@link List} interface.
 *
 * @param <Tuple_> the tuple type
 */
public final class TupleList<Tuple_ extends Tuple> {

    private int size = 0;
    private TupleListEntry<Tuple_> first = null;
    private TupleListEntry<Tuple_> last = null;

    public TupleListEntry<Tuple_> add(Tuple_ tuple) {
        TupleListEntry<Tuple_> entry = new TupleListEntry<>(this, tuple, last);
        if (first == null) {
            first = entry;
        } else {
            last.next = entry;
        }
        last = entry;
        size++;
        return entry;
    }

    public void remove(TupleListEntry<Tuple_> entry) {
        if (first == entry) {
            first = entry.next;
        } else {
            entry.previous.next = entry.next;
        }
        if (last == entry) {
            last = entry.previous;
        } else {
            entry.next.previous = entry.previous;
        }
        entry.previous = null;
        entry.next = null;
        size--;
    }

    public TupleListEntry<Tuple_> first() {
        return first;
    }

    public TupleListEntry<Tuple_> last() {
        return last;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "size = " + size;
    }

}
