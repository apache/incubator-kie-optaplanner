/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.score.director.drools.reproducer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class RemoveRandomItemMutator<T> {

    private final List<T> list;
    private final Random random = new Random(0);
    private final List<Integer> indexBlacklist = new ArrayList<>();
    private int removedIndex = -1;
    private T removedItem;

    public RemoveRandomItemMutator(List<T> list) {
        this.list = new ArrayList<>(list);
    }

    public boolean canMutate() {
        return !list.isEmpty() && list.size() != indexBlacklist.size();
    }

    public List<T> mutate() {
        if (removedIndex >= 0) {
            // last mutation was succesful => clear the blacklist
            indexBlacklist.clear();
        }

        do {
            removedIndex = random.nextInt(list.size());
        } while (indexBlacklist.contains(removedIndex));

        removedItem = list.get(removedIndex);
        list.remove(removedIndex);
        return list;
    }

    public void revert() {
        // return the item
        list.add(removedIndex, removedItem);
        // don't try this index on next mutation
        indexBlacklist.add(removedIndex);
        // last mutation wasn't successful
        removedIndex = -1;
    }

    public List<T> getResult() {
        return list;
    }

    public T getRemovedItem() {
        return removedItem;
    }

}
