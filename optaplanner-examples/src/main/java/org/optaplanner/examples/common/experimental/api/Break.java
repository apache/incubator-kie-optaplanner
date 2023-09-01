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
 * A Break is a gap between two consecutive values. For instance,
 * the list [1,2,4,5,6,10] has a break of length 2 between 2 and 4,
 * as well as a break of length 4 between 6 and 10.
 *
 * @param <Value_> The type of value in the sequence
 * @param <Difference_> The type of difference between values in the sequence
 */
public interface Break<Value_, Difference_ extends Comparable<Difference_>> {
    /**
     * @return never null, the sequence leading directly into this
     */
    Sequence<Value_, Difference_> getPreviousSequence();

    /**
     * @return never null, the sequence immediately following this
     */
    Sequence<Value_, Difference_> getNextSequence();

    /**
     * @return true if and only if this is the first break
     */
    default boolean isFirst() {
        return getPreviousSequence().isFirst();
    }

    /**
     * @return true if and only if this is the last break
     */
    default boolean isLast() {
        return getNextSequence().isLast();
    }

    /**
     * Return the end of the sequence before this break. For the
     * break between 6 and 10, this will return 6.
     *
     * @return never null, the item this break is directly after
     */
    default Value_ getPreviousSequenceEnd() {
        return getPreviousSequence().getLastItem();
    };

    /**
     * Return the start of the sequence after this break. For the
     * break between 6 and 10, this will return 10.
     *
     * @return never null, the item this break is directly before
     */
    default Value_ getNextSequenceStart() {
        return getNextSequence().getFirstItem();
    }

    /**
     * Return the length of the break, which is the difference
     * between {@link #getNextSequenceStart()} and {@link #getPreviousSequenceEnd()}. For the
     * break between 6 and 10, this will return 4.
     *
     * @return never null, the length of this break
     */
    Difference_ getLength();
}
