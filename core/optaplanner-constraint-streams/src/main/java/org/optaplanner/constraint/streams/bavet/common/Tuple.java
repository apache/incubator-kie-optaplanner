/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet.common;

public abstract class Tuple {

    private volatile int hashCode = 0;

    @Override
    public final int hashCode() {
        if (hashCode == 0) {
            // This marginally increases performance on the hot path, as the hashes need not be recomputed every time.
            hashCode = System.identityHashCode(this);
        }
        return hashCode;
    }

    @Override
    public final boolean equals(Object obj) {
        // No two tuples are ever the same, as we need to allow for duplicate tuples with the same components.
        return this == obj;
    }
}
