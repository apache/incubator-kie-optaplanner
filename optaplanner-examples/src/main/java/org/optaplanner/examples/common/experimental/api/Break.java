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

/**
 * A Break is a gap between two consecutive values. For instance,
 * the list [1,2,4,5,6,10] has a break of length 2 between 2 and 4,
 * as well as a break of length 4 between 6 and 10.
 *
 * @param <ValueType_> The type of value in the sequence
 * @param <DifferenceType_> The type of difference between values in the sequence
 */
public interface Break<ValueType_, DifferenceType_ extends Comparable<DifferenceType_>> {
    /**
     * Return the item this break is directly after. For the
     * break between 6 and 10, this will return 6.
     * @return never null, the item this break is directly after
     */
    ValueType_ getAfterItem();

    /**
     * Return the item this break is directly before. For the
     * break between 6 and 10, this will return 10.
     * @return never null,  the item this break is directly before
     */
    ValueType_ getBeforeItem();

    /**
     * Return the length of the break, which is the difference
     * between {@link #getBeforeItem()} and {@link #getAfterItem()}. For the
     * break between 6 and 10, this will return 4.
     * @return never null, the length of this break.
     */
    DifferenceType_ getLength();
}
