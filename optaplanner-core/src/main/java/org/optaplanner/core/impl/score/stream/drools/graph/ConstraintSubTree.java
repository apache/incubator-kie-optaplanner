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

package org.optaplanner.core.impl.score.stream.drools.graph;

import static org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType.FROM;
import static org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType.GROUPBY_COLLECTING_ONLY;
import static org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType.GROUPBY_MAPPING_AND_COLLECTING;
import static org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType.GROUPBY_MAPPING_ONLY;
import static org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType.JOIN;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.rules.RuleAssembler;

public final class ConstraintSubTree {

    private final boolean isJoin;
    private final ConstraintSubTree leftSubTree;
    private final ConstraintSubTree rightSubTree;
    private final List<ConstraintGraphNode> nodes;

    public ConstraintSubTree(List<ConstraintGraphNode> nodesWithoutJoin) {
        this.isJoin = false;
        this.leftSubTree = null;
        this.rightSubTree = null;
        this.nodes = Collections.unmodifiableList(nodesWithoutJoin);
        if (nodes.isEmpty()) {
            throw new IllegalStateException("Node list may not be empty.");
        }
        if (nodes.get(0).getType() != FROM) {
            throw new IllegalStateException("First node is not a From (" + nodes.get(0) + ").");
        }
    }

    public ConstraintSubTree(ConstraintSubTree leftSubTree, ConstraintSubTree rightSubTree,
            List<ConstraintGraphNode> joinAndOtherNodes) {
        this.isJoin = true;
        this.leftSubTree = Objects.requireNonNull(leftSubTree);
        this.rightSubTree = Objects.requireNonNull(rightSubTree);
        this.nodes = Collections.unmodifiableList(joinAndOtherNodes);
        if (nodes.isEmpty()) {
            throw new IllegalStateException("Node list may not be empty.");
        }
        if (nodes.get(0).getType() != JOIN) {
            throw new IllegalStateException("First node is not a Join (" + nodes.get(0) + ").");
        }
    }

    public int getGroupByCount() {
        long groupByCount = nodes.stream()
                .filter(n -> n.getType() == GROUPBY_COLLECTING_ONLY || n.getType() == GROUPBY_MAPPING_ONLY ||
                        n.getType() == GROUPBY_MAPPING_AND_COLLECTING)
                .count();
        if (isJoin) {
            groupByCount = groupByCount + leftSubTree.getGroupByCount();
            groupByCount = groupByCount + rightSubTree.getGroupByCount();
        }
        return (int) groupByCount;
    }

    public RuleAssembler getRuleAssembler() {
        RuleAssembler builder = isJoin ? leftSubTree.getRuleAssembler()
                .join(rightSubTree.getRuleAssembler(), nodes.get(0)) : RuleAssembler.from(nodes.get(0), getGroupByCount());
        for (int i = 1; i < nodes.size(); i++) {
            builder = builder.andThen(nodes.get(i));
        }
        return builder;
    }
}
