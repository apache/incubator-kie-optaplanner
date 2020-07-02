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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ChildNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType;

public final class ConstraintTree<Node_ extends ConstraintGraphNode, Consequence_ extends ConstraintConsequence<Node_>> {

    private final ConstraintGraph graph;
    private final Consequence_ consequence;
    private final List<ConstraintGraphNode> orderedNodeList;

    ConstraintTree(ConstraintGraph graph, Consequence_ consequence) {
        this.graph = graph;
        this.consequence = consequence;
        this.orderedNodeList = orderNodes(consequence);
    }

    private List<ConstraintGraphNode> orderNodes(Consequence_ consequence) {
        // Depth-first search, right parents of join nodes get precedence.
        List<ConstraintGraphNode> nodeList = new ArrayList<>(0);
        Stack<ConstraintGraphNode> unprocessedNodes = new Stack<>();
        unprocessedNodes.add(consequence.getTerminalNode());
        while (!unprocessedNodes.isEmpty()) {
            ConstraintGraphNode node = unprocessedNodes.pop();
            nodeList.add(node);
            if (node instanceof ChildNode) {
                List<ConstraintGraphNode> parentNodes = ((ChildNode) node).getParentNodes();
                int parentNodeCount = parentNodes.size();
                if (parentNodeCount == 2) { // Join node.
                    ConstraintGraphNode supposedLeftParent = parentNodes.get(0);
                    ConstraintGraphNode supposedRightParent = parentNodes.get(1);
                    if (supposedLeftParent.getCardinality() < supposedRightParent.getCardinality()) {
                        throw new IllegalStateException("Left join parent (" + supposedLeftParent +
                                ") has lower cardinality (" + supposedLeftParent.getCardinality() + ") than right (" +
                                supposedRightParent + ", " + supposedRightParent.getCardinality() + ")");
                    }
                    unprocessedNodes.add(supposedLeftParent);
                    unprocessedNodes.add(supposedRightParent);
                } else if (parentNodeCount == 1) {
                    unprocessedNodes.add(parentNodes.get(0));
                } else {
                    throw new IllegalStateException("Node (" + node + ") had unexpected number of parents (" +
                            parentNodeCount + ").");
                }
            }
        }
        // Reverse order. (Start with left-most From node.)
        Collections.reverse(nodeList);
        ConstraintGraphNode firstNode = nodeList.get(0);
        if (firstNode.getType() != ConstraintGraphNodeType.FROM) {
            throw new IllegalStateException("First node (" + firstNode + ") is not a " + ConstraintGraphNodeType.FROM +
                    " (" + firstNode.getType() + ").");
        }
        ConstraintGraphNode lastNode = nodeList.get(nodeList.size() - 1);
        if (lastNode != consequence.getTerminalNode()) {
            throw new IllegalStateException("Last node (" + lastNode + ") is not the terminal node (" +
                    consequence.getTerminalNode() + ").");
        }
        return Collections.unmodifiableList(nodeList);
    }

}
