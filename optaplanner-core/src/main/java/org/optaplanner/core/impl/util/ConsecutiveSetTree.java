package org.optaplanner.core.impl.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsecutiveSetTree<T> implements Collection<T> {
    ToIntFunction<T> getIndex;
    TreeMap<T, TreeMap<T, Integer>> startItemToList;
    Class<? extends T> typeClass;
    Comparator<T> comparator;

    public ConsecutiveSetTree(Class<? extends T> typeClass, ToIntFunction<T> getIndex) {
        this.getIndex = getIndex;
        this.typeClass = typeClass;
        // Hashcode for duplicate protection
        // Ex: two different games on the same time slot
        comparator = Comparator.comparingInt(getIndex).thenComparingInt(Objects::hashCode);
        startItemToList = new TreeMap<>(comparator);
    }

    public ConsecutiveData<T> getConsecutiveData() {
        return new ConsecutiveData<>(this);
    }

    protected TreeMap<T, TreeMap<T, Integer>> getStartItemToList() {
        return startItemToList;
    }

    public int getEndIndex(T key) {
        return getIndex.applyAsInt(startItemToList.get(key).lastKey());
    }

    @Override
    public boolean add(T item) {
        T firstBeforeItem = startItemToList.floorKey(item);
        int itemIndex = getIndex.applyAsInt(item);
        if (firstBeforeItem != null) {
            int endIndex = getEndIndex(firstBeforeItem);
            if (itemIndex <= endIndex) {
                // Item is already in the bag; increase it count
                startItemToList.get(firstBeforeItem).merge(item, 1, (oldVal, newVal) -> oldVal + 1);
            } else {
                // Item is outside the bag
                T firstAfterItem = startItemToList.higherKey(item);
                if (firstAfterItem != null) {
                    int afterStartIndex = getIndex.applyAsInt(firstAfterItem);
                    if (itemIndex == endIndex + 1) {
                        // We need to extend the first bag
                        TreeMap<T, Integer> prevBag = startItemToList.get(firstBeforeItem);
                        if (itemIndex == afterStartIndex - 1) {
                            // We need to merge the two bags
                            TreeMap<T, Integer> afterBag = startItemToList.remove(firstAfterItem);
                            prevBag.putAll(afterBag);
                        }
                        prevBag.put(item, 1);
                    } else {
                        // Don't need to extend the first bag
                        if (itemIndex == afterStartIndex - 1) {
                            // We need to move the after bag to use item as key
                            TreeMap<T, Integer> afterBag = startItemToList.remove(firstAfterItem);
                            afterBag.put(item, 1);
                            startItemToList.put(item, afterBag);
                        } else {
                            // Start a new bag of consecutive items
                            TreeMap<T, Integer> newBag = new TreeMap<>(comparator);
                            newBag.put(item, 1);
                            startItemToList.put(item, newBag);
                        }
                    }
                } else {
                    if (itemIndex == endIndex + 1) {
                        // We need to extend the first bag
                        TreeMap<T, Integer> prevBag = startItemToList.get(firstBeforeItem);
                        prevBag.put(item, 1);
                    } else {
                        // Start a new bag of consecutive items
                        TreeMap<T, Integer> newBag = new TreeMap<>(comparator);
                        newBag.put(item, 1);
                        startItemToList.put(item, newBag);
                    }
                }
            }
        } else {
            // No items before it
            T firstAfterItem = startItemToList.higherKey(item);
            if (firstAfterItem != null) {
                int afterStartIndex = getIndex.applyAsInt(firstAfterItem);

                if (itemIndex == afterStartIndex - 1) {
                    // We need to move the after bag to use item as key
                    TreeMap<T, Integer> afterBag = startItemToList.remove(firstAfterItem);
                    afterBag.put(item, 1);
                    startItemToList.put(item, afterBag);
                } else {
                    // Start a new bag of consecutive items
                    TreeMap<T, Integer> newBag = new TreeMap<>(comparator);
                    newBag.put(item, 1);
                    startItemToList.put(item, newBag);
                }
            } else {
                // Start a new bag of consecutive items
                TreeMap<T, Integer> newBag = new TreeMap<>(comparator);
                newBag.put(item, 1);
                startItemToList.put(item, newBag);
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
            T firstBeforeItem = startItemToList.floorKey(item);
            int itemIndex = getIndex.applyAsInt(item);
            int endIndex = getEndIndex(firstBeforeItem);

            if (itemIndex > endIndex) {
                // Item not in bag
                return false;
            }

            TreeMap<T, Integer> bag = startItemToList.get(firstBeforeItem);
            bag.merge(item, 0, (oldVal, newVal) -> oldVal - 1);
            if (!bag.get(item).equals(0)) {
                return true;
            }

            // Count of item in bag is 0
            T endItem = bag.lastKey();
            bag.remove(item);
            if (bag.isEmpty()) {
                startItemToList.remove(firstBeforeItem);
                return true;
            }

            // Bag is not empty
            if (item.equals(firstBeforeItem)) {
                // Change start key to the item after this one
                startItemToList.remove(firstBeforeItem);
                startItemToList.put(bag.firstKey(), bag);
                return true;
            }
            if (item.equals(endItem)) {
                // Item already removed from bag;
                // don't need to do anything
                return true;
            }

            // Need to split bag into two halves
            // Both halves are not empty as the item was not an endpoint
            TreeMap<T, Integer> firstBag = new TreeMap<>(bag.subMap(firstBeforeItem, true,
                    item, false));

            TreeMap<T, Integer> secondBag = new TreeMap<>(bag.subMap(item, true,
                    endItem, true));

            startItemToList.put(firstBeforeItem, firstBag);
            startItemToList.put(secondBag.firstKey(), secondBag);
            return true;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return c.stream().map(this::add).count() > 0;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().map(this::remove).count() > 0;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return stream().filter(item -> !c.contains(item)).map(this::remove)
                .count() > 0;
    }

    @Override
    public void clear() {
        startItemToList.clear();
    }

    @Override
    public int size() {
        return startItemToList.values().stream().map(bag -> bag.values().stream()
                .reduce(0, Integer::sum))
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean isEmpty() {
        return startItemToList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (startItemToList.isEmpty()) {
            return false;
        }
        if (typeClass.isInstance(o)) {
            T item = typeClass.cast(o);
            T flooredKey = startItemToList.floorKey(item);
            int endIndex = getEndIndex(flooredKey);
            return getIndex.applyAsInt(item) <= endIndex;
        } else {
            return false;
        }
    }

    public static <T> Stream<T> repeatTime(T item, int times) {
        Stream<T> out = Stream.empty();
        for (int i = 0; i < times; i++) {
            out = Stream.concat(Stream.of(item), out);
        }
        return out;
    }

    @Override
    public Iterator<T> iterator() {
        return startItemToList.values().stream().flatMap(bag -> bag.keySet().stream()
                .flatMap(key -> repeatTime(key, bag.get(key)))).iterator();
    }

    @Override
    public Object[] toArray() {
        return startItemToList.values().stream().flatMap(bag -> bag.keySet().stream()
                .flatMap(key -> repeatTime(key, bag.get(key))))
                .toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return startItemToList.values().stream().flatMap(bag -> bag.keySet().stream()
                .flatMap(key -> repeatTime(key, bag.get(key))))
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
        ConsecutiveSetTree<?> that = (ConsecutiveSetTree<?>) o;
        return startItemToList.equals(that.startItemToList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startItemToList);
    }
}
