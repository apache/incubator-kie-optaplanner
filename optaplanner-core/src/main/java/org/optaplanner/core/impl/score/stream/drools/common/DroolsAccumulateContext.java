/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

/**
 * Each context is uniquely identified by its ID.
 * This is necessary so that the Drools accumulate function can properly undo in
 * {@link DroolsUniAccumulateFunctionBridge} and its Bi, Tri, ... alternatives.
 * @param <ResultContainer_> The same type from {@link UniConstraintCollector} and its Bi, Tri, ... alternatives.
 */
public final class DroolsAccumulateContext<ResultContainer_> implements Serializable {

    private static final AtomicLong CONTEXT_COUNTER = new AtomicLong();
    private final long id = CONTEXT_COUNTER.getAndIncrement();
    private final ResultContainer_ container;

    public DroolsAccumulateContext(ResultContainer_ container) {
        this.container = container;
    }

    public ResultContainer_ getContainer() {
        return container;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !Objects.equals(getClass(), o.getClass())) {
            return false;
        }
        final DroolsAccumulateContext context = (DroolsAccumulateContext) o;
        return id == context.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
