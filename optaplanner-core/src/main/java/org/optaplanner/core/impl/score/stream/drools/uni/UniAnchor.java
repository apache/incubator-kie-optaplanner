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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.drools.model.Global;
import org.drools.model.RuleItemBuilder;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

public interface UniAnchor<T extends UniAnchor<T>> {

    static String createContextId() {
        return UUID.randomUUID().toString();
    }

    String getContextId();

    public abstract T filter(Predicate predicate);

    <A, GroupKey_> List<RuleItemBuilder<?>> terminateWithLogical(String currentContextId,
            Function<A, GroupKey_> groupKeyMapping);

    List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal);

    List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntFunction matchWeighter);

    List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongFunction matchWeighter);

    List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            Function<Object, BigDecimal> matchWeighter);
}
