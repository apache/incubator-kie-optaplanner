package org.optaplanner.constraint.streams.bavet.common.collection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Different from {@link LinkedList} because nodes/indexes are allowed
 * to directly reference {@link TupleListEntry} instances
 * to avoid the lookup by index cost.
 * Also doesn't implement the {@link List} interface.
 *
 * <p>
 * This class is not thread-safe.
 *
 * @param <T> The element type. Often a tuple.
 */
public final class TupleList<T> implements Iterable<T> {

    private int size = 0;
    private TupleListEntry<T> first = null;
    private TupleListEntry<T> last = null;

    public TupleListEntry<T> add(T tuple) {
        TupleListEntry<T> entry = new TupleListEntry<>(this, tuple, last);
        if (first == null) {
            first = entry;
        } else {
            last.next = entry;
        }
        last = entry;
        size++;
        return entry;
    }

    public void remove(TupleListEntry<T> entry) {
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

    public TupleListEntry<T> first() {
        return first;
    }

    public TupleListEntry<T> last() {
        return last;
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new TupleListIterator();
    }

    @Override
    public String toString() {
        return "size = " + size;
    }

    private final class TupleListIterator implements Iterator<T> {

        private TupleListEntry<T> entry = first;
        private boolean reachedEnd = false;

        @Override
        public boolean hasNext() {
            if (size == 0 || reachedEnd) {
                return false;
            }
            return entry != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T element = entry.getElement();
            if (entry == last) {
                entry = null;
                reachedEnd = true;
            } else {
                entry = entry.next;
            }
            return element;
        }
    }

}
