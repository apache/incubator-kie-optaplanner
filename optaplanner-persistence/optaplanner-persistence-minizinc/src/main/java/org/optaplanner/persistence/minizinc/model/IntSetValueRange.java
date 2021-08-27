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

package org.optaplanner.persistence.minizinc.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.optaplanner.core.impl.domain.valuerange.AbstractCountableValueRange;

public class IntSetValueRange extends AbstractCountableValueRange<List<Integer>> {

    private final List<Integer> possibleItemsOfSet;

    public IntSetValueRange(List<Integer> possibleItemsOfSet) {
        this.possibleItemsOfSet = possibleItemsOfSet;
    }

    @Override
    public long getSize() {
        return 2L << possibleItemsOfSet.size();
    }

    @Override
    public List<Integer> get(long index) {
        int size = Long.bitCount(index);
        List<Integer> out = new ArrayList<>(size);
        while (index != 0) {
            long item = Long.highestOneBit(index);
            int itemIndex = Long.numberOfTrailingZeros(item);
            out.add(possibleItemsOfSet.get(itemIndex));
            index -= item;
        }
        ;
        return out;
    }

    @Override
    public Iterator<List<Integer>> createOriginalIterator() {
        return new Iterator<List<Integer>>() {
            long index = 0;

            @Override
            public boolean hasNext() {
                return index < getSize();
            }

            @Override
            public List<Integer> next() {
                List<Integer> out = get(index);
                index++;
                return out;
            }
        };
    }

    @Override
    public boolean contains(List<Integer> value) {
        return possibleItemsOfSet.containsAll(value);
    }

    @Override
    public Iterator<List<Integer>> createRandomIterator(Random workingRandom) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public List<Integer> next() {
                return get(workingRandom.nextLong());
            }
        };
    }

    @Override
    public String toString() {
        return "IntSetValueRange{" +
                "possibleItemsOfSet=" + possibleItemsOfSet +
                '}';
    }
}
