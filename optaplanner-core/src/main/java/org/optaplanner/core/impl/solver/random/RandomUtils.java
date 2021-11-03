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

package org.optaplanner.core.impl.solver.random;

import java.util.Random;

public class RandomUtils {

    /**
     * Mimics {@link Random#nextInt(int)} for longs.
     *
     * @param random never null
     * @param n {@code > 0L}
     * @return like {@link Random#nextInt(int)} but for a long
     * @see Random#nextInt(int)
     */
    public static long nextLong(Random random, long n) {
        return random.longs(0, n)
                .findAny()
                .orElseThrow();
    }

    /**
     * Mimics {@link Random#nextInt(int)} for doubles.
     *
     * @param random never null
     * @param n {@code > 0.0}
     * @return like {@link Random#nextInt(int)} but for a double
     * @see Random#nextInt(int)
     */
    public static double nextDouble(Random random, double n) {
        return random.doubles()
                .map(d -> d * n)
                .findAny()
                .orElseThrow();
    }

    private RandomUtils() {
    }

}
