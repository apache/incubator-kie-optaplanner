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

package org.optaplanner.core.impl.score.stream.drools.bi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.drools.core.common.InternalFactHandle;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;
import org.optaplanner.core.impl.score.stream.drools.common.TriTuple;

final class DroolsBiToTriGroupBy<A, B, ResultContainer, NewA, NewB, NewC> implements Serializable {

    private static final long serialVersionUID = 510l;
    private final Map<Long, Runnable> undoMap = new HashMap<>(0);
    private final BiFunction<A, B, NewA> groupKeyAMapping;
    private final BiFunction<A, B, NewB> groupKeyBMapping;
    private final BiConstraintCollector<A, B, ResultContainer, NewC> collector;
    private DroolsBiToTriGroupByAccumulator<A, B, ResultContainer, NewA, NewB, NewC> acc;

    public DroolsBiToTriGroupBy(BiFunction<A, B, NewA> groupKeyAMapping, BiFunction<A, B, NewB> groupKeyBMapping,
            BiConstraintCollector<A, B, ResultContainer, NewC> collector) {
        this.groupKeyAMapping = groupKeyAMapping;
        this.groupKeyBMapping = groupKeyBMapping;
        this.collector = collector;
    }

    public void init() {
        acc = new DroolsBiToTriGroupByAccumulator<>(groupKeyAMapping, groupKeyBMapping, collector);
        undoMap.clear();
    }

    public void accumulate(InternalFactHandle handle, A a, B b) {
        Runnable undo = acc.accumulate(new BiTuple<>(a, b));
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
