package org.optaplanner.core.impl.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsecutiveSetTree<T, I extends Comparable<I>, D extends Comparable<D>> implements Collection<T> {
    Function<T, I> indexFunction;
    BiFunction<I, I, D> differenceFunction;
    D maxDifference;
    D zeroDifference;
    TreeMap<T, Sequence<T>> startItemToSequence;
    Class<? extends T> typeClass;
    Comparator<T> comparator;

    TreeMapValueList<T, Sequence<T>> sequenceList;
    ConsecutiveData<T, D> consecutiveData;

    public ConsecutiveSetTree(Class<? extends T> typeClass, Function<T, I> indexFunction,
            BiFunction<I, I, D> differenceFunction, D maxDifference, D zeroDifference) {
        this.indexFunction = indexFunction;
        this.differenceFunction = differenceFunction;
        this.maxDifference = maxDifference;
        this.zeroDifference = zeroDifference;
        this.typeClass = typeClass;
        // Hashcode for duplicate protection
        // Ex: two different games on the same time slot
        comparator = Comparator.comparing(indexFunction).thenComparingInt(System::identityHashCode);
        startItemToSequence = new TreeMap<>(comparator);
        consecutiveData = new ConsecutiveData<>(this);
        sequenceList = new TreeMapValueList<>(startItemToSequence);
    }

    public List<Sequence<T>> getConsecutiveSequences() {
        return sequenceList;
    }

    public List<Break<T, D>> getBreaks() {
        return startItemToSequence.keySet().stream().flatMap(startItem -> {
            T nextStartItem = startItemToSequence.higherKey(startItem);
            if (nextStartItem == null) {
                return Stream.empty();
            }
            T endOfCurrentSequence = startItemToSequence.get(startItem).getItems().last();
            return Stream.of(new Break<>(nextStartItem, endOfCurrentSequence,
                    differenceFunction.apply(indexFunction.apply(endOfCurrentSequence),
                            indexFunction.apply(nextStartItem))));
        }).collect(Collectors.toList());
    }

    public List<D> getConsecutiveLengths() {
        return getConsecutiveSequences().stream()
                .map(sequence -> differenceFunction.apply(indexFunction.apply(sequence.getItems().first()),
                        indexFunction.apply(sequence.getItems().last())))
                .collect(Collectors.toList());
    }

    public Optional<D> getBreakBefore(Sequence<T> sequence) {
        T startItem = sequence.getItems().first();
        T prevStartItem = startItemToSequence.lowerKey(startItem);
        if (prevStartItem == null) {
            return Optional.empty();
        }
        return Optional
                .of(differenceFunction.apply(indexFunction.apply(startItemToSequence.get(prevStartItem).getItems().last()),
                        indexFunction.apply(startItem)));
    }

    public Optional<D> getBreakAfter(Sequence<T> sequence) {
        T endItem = sequence.getItems().last();
        T nextStartItem = startItemToSequence.higherKey(endItem);
        if (nextStartItem == null) {
            return Optional.empty();
        }
        return Optional.of(differenceFunction.apply(indexFunction.apply(endItem),
                indexFunction.apply(nextStartItem)));
    }

    public ConsecutiveData<T, D> getConsecutiveData() {
        return consecutiveData;
    }

    public I getEndIndex(T key) {
        return indexFunction.apply(startItemToSequence.get(key).getItems().last());
    }

    private boolean isSecondSuccessorOfFirst(I first, I second) {
        D difference = differenceFunction.apply(second, first);
        return !(difference.compareTo(maxDifference) > 0 || difference.compareTo(zeroDifference) < 0);
    }

    @Override
    public boolean add(T item) {
        T firstBeforeItem = startItemToSequence.floorKey(item);
        I itemIndex = indexFunction.apply(item);
        if (firstBeforeItem != null) {
            I endIndex = getEndIndex(firstBeforeItem);
            if (itemIndex.compareTo(endIndex) <= 0) {
                // Item is already in the bag; increase it count
                startItemToSequence.get(firstBeforeItem).add(item);
            } else {
                // Item is outside the bag
                T firstAfterItem = startItemToSequence.higherKey(item);
                if (firstAfterItem != null) {
                    I afterStartIndex = indexFunction.apply(firstAfterItem);
                    if (isSecondSuccessorOfFirst(itemIndex, endIndex)) {
                        // We need to extend the first bag
                        Sequence<T> prevBag = startItemToSequence.get(firstBeforeItem);
                        if (isSecondSuccessorOfFirst(afterStartIndex, itemIndex)) {
                            // We need to merge the two bags
                            Sequence<T> afterBag = startItemToSequence.remove(firstAfterItem);
                            prevBag.putAll(afterBag);
                        }
                        prevBag.add(item);
                    } else {
                        // Don't need to extend the first bag
                        if (isSecondSuccessorOfFirst(afterStartIndex, itemIndex)) {
                            // We need to move the after bag to use item as key
                            Sequence<T> afterBag = startItemToSequence.remove(firstAfterItem);
                            afterBag.add(item);
                            startItemToSequence.put(item, afterBag);
                        } else {
                            // Start a new bag of consecutive items
                            Sequence<T> newBag = new Sequence<>(this);
                            newBag.add(item);
                            startItemToSequence.put(item, newBag);
                        }
                    }
                } else {
                    if (isSecondSuccessorOfFirst(itemIndex, endIndex)) {
                        // We need to extend the first bag
                        Sequence<T> prevBag = startItemToSequence.get(firstBeforeItem);
                        prevBag.add(item);
                    } else {
                        // Start a new bag of consecutive items
                        Sequence<T> newBag = new Sequence<>(this);
                        newBag.add(item);
                        startItemToSequence.put(item, newBag);
                    }
                }
            }
        } else {
            // No items before it
            T firstAfterItem = startItemToSequence.higherKey(item);
            if (firstAfterItem != null) {
                I afterStartIndex = indexFunction.apply(firstAfterItem);

                if (isSecondSuccessorOfFirst(afterStartIndex, itemIndex)) {
                    // We need to move the after bag to use item as key
                    Sequence<T> afterBag = startItemToSequence.remove(firstAfterItem);
                    afterBag.add(item);
                    startItemToSequence.put(item, afterBag);
                } else {
                    // Start a new bag of consecutive items
                    Sequence<T> newBag = new Sequence<>(this);
                    newBag.add(item);
                    startItemToSequence.put(item, newBag);
                }
            } else {
                // Start a new bag of consecutive items
                Sequence<T> newBag = new Sequence<>(this);
                newBag.add(item);
                startItemToSequence.put(item, newBag);
            }
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!typeClass.isInstance(o)) {
            return false;
        } else {
            T item = typeClass.cast(o);
            T firstBeforeItem = startItemToSequence.floorKey(item);
            I itemIndex = indexFunction.apply(item);
            I endIndex = getEndIndex(firstBeforeItem);

            if (itemIndex.compareTo(endIndex) > 0) {
                // Item not in bag
                return false;
            }

            Sequence<T> bag = startItemToSequence.get(firstBeforeItem);
            T endItem = bag.getItems().last();
            boolean isRemoved = bag.remove(item);
            if (!isRemoved) {
                return true;
            }

            // Count of item in bag is 0
            if (bag.isEmpty()) {
                startItemToSequence.remove(firstBeforeItem);
                return true;
            }

            // Bag is not empty
            if (item.equals(firstBeforeItem)) {
                // Change start key to the item after this one
                startItemToSequence.remove(firstBeforeItem);
                startItemToSequence.put(bag.getItems().first(), bag);
                return true;
            }
            if (item.equals(endItem)) {
                // Item already removed from bag;
                // don't need to do anything
                return true;
            }

            // Need to split bag into two halves
            // Both halves are not empty as the item was not an endpoint
            Sequence<T> splitBag = bag.split(item);
            startItemToSequence.put(splitBag.getItems().first(), splitBag);
            return true;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int oldSize = size();
        c.forEach(this::add);
        return size() > oldSize;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int oldSize = size();
        c.forEach(this::remove);
        return size() < oldSize;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int oldSize = size();
        stream().filter(item -> !c.contains(item)).forEach(this::remove);
        return size() < oldSize;
    }

    @Override
    public void clear() {
        startItemToSequence.clear();
    }

    @Override
    public int size() {
        return startItemToSequence.values().stream().map(Sequence::getCountIncludingDuplicates)
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean isEmpty() {
        return startItemToSequence.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (startItemToSequence.isEmpty()) {
            return false;
        }
        if (typeClass.isInstance(o)) {
            T item = typeClass.cast(o);
            T flooredKey = startItemToSequence.floorKey(item);
            I endIndex = getEndIndex(flooredKey);
            return indexFunction.apply(item).compareTo(endIndex) <= 0;
        } else {
            return false;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return startItemToSequence.values().stream().flatMap(Sequence::getDuplicatedStream).iterator();
    }

    @Override
    public Object[] toArray() {
        return startItemToSequence.values().stream().flatMap(Sequence::getDuplicatedStream)
                .toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return startItemToSequence.values().stream().flatMap(Sequence::getDuplicatedStream)
                .collect(Collectors.toList()).toArray(a);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsecutiveSetTree<?, ?, ?> that = (ConsecutiveSetTree<?, ?, ?>) o;
        return startItemToSequence.equals(that.startItemToSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startItemToSequence);
    }
}
