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

package org.optaplanner.core.impl.score.stream.drools.common.nodes;

import org.optaplanner.core.api.score.stream.tri.TriJoiner;

final class TriJoinNode<A, B, C> extends AbstractConstraintModelJoiningNode<C, TriJoiner<A, B, C>>
        implements TriConstraintGraphNode {

    TriJoinNode(Class<C> otherFactType, TriJoiner<A, B, C> joiner) {
        super(otherFactType, ConstraintGraphNodeType.JOIN, joiner);
    }

}
