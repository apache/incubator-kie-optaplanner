/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.tri;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.core.common.InternalFactHandle;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.TriTuple;

final class DroolsTriGroupBy<A, B, C, ResultContainer, NewA, NewB, NewC> implements Serializable {

    private static final long serialVersionUID = 510l;
    private final Map<Long, Runnable> undoMap = new HashMap<>(0);
    private final TriFunction<A, B, C, NewA> groupKeyAMapping;
    private final TriFunction<A, B, C, NewB> groupKeyBMapping;
    private final TriConstraintCollector<A, B, C, ResultContainer, NewC> collector;
    private DroolsTriGroupByAccumulator<A, B, C, ResultContainer, NewA, NewB, NewC> acc;

    public DroolsTriGroupBy(TriFunction<A, B, C, NewA> groupKeyAMapping, TriFunction<A, B, C, NewB> groupKeyBMapping,
            TriConstraintCollector<A, B, C, ResultContainer, NewC> collector) {
        this.groupKeyAMapping = groupKeyAMapping;
        this.groupKeyBMapping = groupKeyBMapping;
        this.collector = collector;
    }

    public void init() {
        acc = new DroolsTriGroupByAccumulator<>(groupKeyAMapping, groupKeyBMapping, collector);
        undoMap.clear();
    }

    public void accumulate(InternalFactHandle handle, A a, B b, C c) {
        Runnable undo = acc.accumulate(a, b, c);
        Runnable oldUndo = this.undoMap.put(handle.getId(), undo);
        if (oldUndo != null) {
            throw new IllegalStateException("Undo for fact handle (" + handle.getId() + ") already exists.");
        }
    }

    public void reverse(InternalFactHandle handle) {
        final Runnable undo = this.undoMap.remove(handle.getId());
        if (undo == null) {
            throw new IllegalStateException("No undo for fact handle (" + handle.getId() + ")");
        }
        undo.run();
    }

    public Set<TriTuple<NewA, NewB, NewC>> getResult() {
        return acc.finish();
    }

}
