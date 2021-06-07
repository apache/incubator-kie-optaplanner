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

package org.optaplanner.examples.common.experimental.api;

public interface Sequence<ValueType_, DifferenceType_ extends Comparable<DifferenceType_>> {
    /**
     * Returns the first item in the sequence
     * 
     * @return Never null, the first item in the sequence
     */
    ValueType_ getFirstItem();

    /**
     * Returns the last item in the sequence
     * 
     * @return Never null, the last item in the sequence
     */
    ValueType_ getLastItem();

    /**
     * Returns an iterable that iterates through
     * the items in this sequence in ascending order
     * 
     * @return Never null, an iterable that can iterate through this sequence
     */
    Iterable<ValueType_> getItems();

    /**
     * Return the number of items in this sequence
     * 
     * @return The number of items in this sequence
     */
    int getCount();

    /**
     * Return the difference between the last item and the first item
     * in the sequence
     * 
     * @return Never null, the difference between the last item and
     *         first item in this sequence
     */
    DifferenceType_ getLength();
}
