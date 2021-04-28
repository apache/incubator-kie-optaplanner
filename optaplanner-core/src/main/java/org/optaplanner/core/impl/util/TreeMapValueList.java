/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class TreeMapValueList<I, T> implements List<T> {
    private final TreeMap<I, T> sourceMap;

    public TreeMapValueList(TreeMap<I, T> sourceMap) {
        this.sourceMap = sourceMap;
    }

    @Override
    public int size() {
        return sourceMap.size();
    }

    @Override
    public boolean isEmpty() {
        return sourceMap.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return sourceMap.values().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return sourceMap.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return sourceMap.values().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return sourceMap.values().toArray(a);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return sourceMap.values().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= sourceMap.size()) {
            throw new IndexOutOfBoundsException();
        }
        I currentKey = sourceMap.firstKey();
        while (index > 0) {
            currentKey = sourceMap.higherKey(currentKey);
            index--;
        }
        return sourceMap.get(currentKey);
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        if (sourceMap.isEmpty()) {
            return -1;
        }
        I currentKey = sourceMap.firstKey();
        int index = 0;
        while (currentKey != null) {
            if (sourceMap.get(currentKey).equals(o)) {
                return index;
            }
            currentKey = sourceMap.higherKey(currentKey);
            index++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (sourceMap.isEmpty()) {
            return -1;
        }
        I currentKey = sourceMap.lastKey();
        int index = sourceMap.size() - 1;
        while (currentKey != null) {
            if (sourceMap.get(currentKey).equals(o)) {
                return index;
            }
            currentKey = sourceMap.lowerKey(currentKey);
            index--;
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new TreeMapValueListIterator<I, T>(this);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new TreeMapValueListIterator<I, T>(this, index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > sourceMap.size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        if (sourceMap.isEmpty()) {
            return Collections.emptyList();
        }

        I fromKey = sourceMap.firstKey();
        I toKey = sourceMap.firstKey();
        for (int i = 0; i < fromIndex; i++) {
            fromKey = sourceMap.higherKey(fromKey);
        }

        for (int i = 0; i < toIndex; i++) {
            toKey = sourceMap.higherKey(toKey);
        }

        return new TreeMapValueList<I, T>(new TreeMap<>(sourceMap.subMap(fromKey, toKey)));
    }

    private static class TreeMapValueListIterator<I, T> implements ListIterator<T> {
        int index;
        I currentKey;
        TreeMap<I, T> sourceMap;

        public TreeMapValueListIterator(TreeMapValueList<I, T> sourceList) {
            index = 0;
            currentKey = null;
            this.sourceMap = sourceList.sourceMap;
        }

        public TreeMapValueListIterator(TreeMapValueList<I, T> sourceList, int index) {
            this(sourceList);
            for (int i = 0; i < index; i++) {
                next();
            }
        }

        @Override
        public boolean hasNext() {
            return index < sourceMap.size();
        }

        @Override
        public T next() {
            if (index == 0) {
                currentKey = sourceMap.firstKey();
            } else if (index == sourceMap.size()) {
                throw new NoSuchElementException();
            } else {
                currentKey = sourceMap.higherKey(currentKey);
            }
            index++;
            return sourceMap.get(currentKey);
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public T previous() {
            if (index == 0) {
                throw new NoSuchElementException();
            } else if (index == sourceMap.size()) {
                currentKey = sourceMap.lastKey();
            } else {
                currentKey = sourceMap.lowerKey(currentKey);
            }
            index--;
            return sourceMap.get(currentKey);
        }

        @Override
        public int nextIndex() {
            return index + 1;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T t) {
            throw new UnsupportedOperationException();
        }
    }
}
