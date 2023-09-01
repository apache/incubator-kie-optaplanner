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

package org.optaplanner.core.impl.heuristic.selector.common.nearby;

import java.util.Objects;
import java.util.Random;

/**
 * {@code P(x) = 3(m - x)²/m³}.
 * <p>
 * Cumulative probability: {@code F(x) = 1 - (1 - x/m)³}.
 * <p>
 * Inverse cumulative probability: {@code F(p) = m(1 - (1 - p)^(1/3))}.
 */
public final class ParabolicDistributionNearbyRandom implements NearbyRandom {
    private final int sizeMaximum;

    public ParabolicDistributionNearbyRandom(int sizeMaximum) {
        this.sizeMaximum = sizeMaximum;
        if (sizeMaximum < 1) {
            throw new IllegalArgumentException("The maximum (" + sizeMaximum
                    + ") must be at least 1.");
        }
    }

    @Override
    public int nextInt(Random random, int nearbySize) {
        int m = sizeMaximum <= nearbySize ? sizeMaximum : nearbySize;
        double p = random.nextDouble();
        double x = m * (1.0 - Math.pow(1.0 - p, 1.0 / 3.0));
        int next = (int) x;
        // Due to a rounding error it might return m
        if (next >= m) {
            next = m - 1;
        }
        return next;
    }

    @Override
    public int getOverallSizeMaximum() {
        return sizeMaximum;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        ParabolicDistributionNearbyRandom that = (ParabolicDistributionNearbyRandom) other;
        return sizeMaximum == that.sizeMaximum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sizeMaximum);
    }

}
