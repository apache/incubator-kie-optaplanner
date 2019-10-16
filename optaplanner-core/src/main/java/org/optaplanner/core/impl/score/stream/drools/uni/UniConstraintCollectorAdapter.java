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

package org.optaplanner.core.impl.score.stream.drools.uni;

import java.io.Serializable;
import java.util.function.Supplier;

import org.kie.api.runtime.rule.AccumulateFunction;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsUniAccumulateFunctionAdapter;

/**
 * Exists so that {@link DroolsUniAccumulateFunctionAdapter} can fulfill the contract of {@link AccumulateFunction} while
 * still accepting {@link UniConstraintCollector} which is not {@link Serializable}.
 * @param <A> input to accumulate
 * @param <ResultContainer_> implementation detail
 * @param <NewA> result of accumulation
 */
public final class UniConstraintCollectorAdapter<A, ResultContainer_, NewA> implements Serializable,
        Supplier<UniConstraintCollector<A, ResultContainer_, NewA>> {

    private final UniConstraintCollector<A, ResultContainer_, NewA> collector;

    public UniConstraintCollectorAdapter(UniConstraintCollector<A, ResultContainer_, NewA> collector) {
        this.collector = collector;
    }

    @Override
    public UniConstraintCollector<A, ResultContainer_, NewA> get() {
        return collector;
    }
}
