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

import org.optaplanner.core.impl.score.stream.drools.common.consequences.ConstraintConsequence;

import java.util.List;

public interface ConstraintGraphNode {

    int getCardinality();

    ConstraintGraphNodeType getType();

    /**
     * Return {@link FromNode#getFactType()} of the ultimate parent node.
     *
     * @param <A> Generic return type, choose the one you need at the call site.
     * @return null when the ultimate parent is a join/groupBy node
     */
    <A> Class<A> getFactType();

    /**
     * Retrieves an unmodifiable collection of unique nodes which follow this one.
     *
     * @return never null, may be empty when {@link #getConsequences()} is not or when the model is not yet fully built.
     */
    List<ConstraintGraphNode> getChildNodes();

    /**
     * Retrieves an unmodifiable collection of unique consequences having this node as a terminal node.
     *
     * @return never null, may be empty when {@link #getChildNodes()} is not or when the model is not yet fully built.
     */
    List<ConstraintConsequence> getConsequences();

}
