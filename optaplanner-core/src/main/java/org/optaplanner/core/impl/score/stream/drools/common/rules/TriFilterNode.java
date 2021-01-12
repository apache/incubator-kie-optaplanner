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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import org.optaplanner.core.api.function.TriPredicate;

import java.util.Objects;
import java.util.function.Supplier;

final class TriFilterNode<A, B, C> extends AbstractConstraintModelChildNode<TriLeftHandSide<A, B, C>>
        implements TriConstraintGraphNode, Supplier<TriPredicate<A, B, C>> {

    private final TriPredicate<A, B, C> predicate;

    TriFilterNode(TriLeftHandSide<A, B, C> leftHandSide, TriPredicate<A, B, C> predicate) {
        super(leftHandSide);
        this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public TriPredicate<A, B, C> get() {
        return predicate;
    }
}
