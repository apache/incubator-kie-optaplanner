/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.examples.common.experimental.api;

/**
 * A Sequence is a series of consecutive values. For instance,
 * the list [1,2,4,5,6,10] has three sequences: [1,2], [4,5,6], and [10].
 *
 * @param <Value_> The type of value in the sequence
 * @param <Difference_> The type of difference between values in the sequence
 */
public interface Sequence<Value_, Difference_ extends Comparable<Difference_>> {
    /**
     * @return never null, the first item in the sequence
     */
    Value_ getFirstItem();

    /**
     * @return never null, the last item in the sequence
     */
    Value_ getLastItem();

    /**
     * @return true if and only if this is the first Sequence
     */
    boolean isFirst();

    /**
     * @return true if and only if this is the last Sequence
     */
    boolean isLast();

    /**
     * @return If this is not the first sequence, the break before it. Otherwise, null.
     */
    Break<Value_, Difference_> getPreviousBreak();

    /**
     * @return If this is not the last sequence, the break after it. Otherwise, null.
     */
    Break<Value_, Difference_> getNextBreak();

    /**
     * @return never null, an iterable that can iterate through this sequence
     */
    Iterable<Value_> getItems();

    /**
     * @return the number of items in this sequence
     */
    int getCount();

    /**
     * @return never null, the difference between the last item and
     *         first item in this sequence
     */
    Difference_ getLength();
}
