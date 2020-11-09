package org.optaplanner.core.impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ConsecutiveData<T> {
    ConsecutiveSetTree<T> sourceTree;

    public ConsecutiveData(ConsecutiveSetTree<T> sourceTree) {
        this.sourceTree = sourceTree;
    }

    public int getNumberOfConsecutive() {
        return sourceTree.getStartItemToList().size();
    }

    public int getNumberOfBreaks() {
        return Math.max(sourceTree.getStartItemToList().size() - 1, 0);
    }

    public List<Integer> getBreakLengths() {
        if (getNumberOfBreaks() == 0) {
            return Collections.emptyList();
        } else {
            List<Integer> out = new ArrayList<>(getNumberOfBreaks());
            T currentElement = sourceTree.getStartItemToList().firstKey();
            for (T nextElement : sourceTree.getStartItemToList().tailMap(currentElement, false).keySet()) {
                int length = sourceTree.getIndex.applyAsInt(nextElement) - sourceTree.getEndIndex(currentElement) - 1;
                out.add(length);
                currentElement = nextElement;
            }
            return out;
        }
    }

    public List<Integer> getConsecutiveLengths() {
        if (getNumberOfConsecutive() == 0) {
            return Collections.emptyList();
        }
        List<Integer> out = new ArrayList<>(getNumberOfConsecutive());
        for (T element : sourceTree.getStartItemToList().keySet()) {
            out.add(sourceTree.getEndIndex(element) - sourceTree.getIndex.applyAsInt(element) + 1);
        }
        return out;
    }

    public List<List<T>> getConsecutiveItems() {
        if (getNumberOfConsecutive() == 0) {
            return Collections.emptyList();
        }
        List<List<T>> out = new ArrayList<>(getNumberOfConsecutive());
        for (T element : sourceTree.getStartItemToList().keySet()) {
            TreeMap<T, Integer> bag = sourceTree.getStartItemToList().get(element);
            out.add(bag.keySet().stream().flatMap(key -> ConsecutiveSetTree.repeatTime(key, bag.get(key)))
                    .collect(Collectors.toList()));
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsecutiveData<?> that = (ConsecutiveData<?>) o;
        return sourceTree.equals(that.sourceTree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTree);
    }

    @Override
    public String toString() {
        return getConsecutiveItems().stream().map(items -> items.stream()
                .map(Objects::toString).collect(Collectors.joining(", ", "[", "]")))
                .collect(Collectors.joining(",\n"));
    }
}
