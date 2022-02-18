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

package org.optaplanner.constraint.streams.bavet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.optaplanner.constraint.streams.bavet.common.BavetNode;
import org.optaplanner.constraint.streams.bavet.common.BavetNodeBuildPolicy;
import org.optaplanner.constraint.streams.bavet.common.BavetScoringNode;
import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;
import org.optaplanner.constraint.streams.bavet.uni.BavetForEachUniNode;
import org.optaplanner.constraint.streams.bavet.uni.BavetForEachUniTuple;
import org.optaplanner.constraint.streams.common.inliner.AbstractScoreInliner;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

public final class BavetConstraintSession<Solution_, Score_ extends Score<Score_>> {

    private final AbstractScoreInliner<Score_> scoreInliner;
    private final Map<Class<?>, BavetForEachUniNode<Object>> declaredClassToNodeMap;
    private final BavetNode[] nodes; // Indexed by nodeIndex
    private final List<BavetScoringNode> scoringNodeList; // TODO Unused (except by 1 test)

    private final Map<Class<?>, List<BavetForEachUniNode<Object>>> effectiveClassToNodeListMap;

    private final Map<Object, List<BavetForEachUniTuple<Object>>> fromTupleListMap;

    public BavetConstraintSession(boolean constraintMatchEnabled, ScoreDefinition<Score_> scoreDefinition,
            Map<BavetConstraint<Solution_>, Score_> constraintToWeightMap) {
        scoreInliner = AbstractScoreInliner.buildScoreInliner(scoreDefinition, (Map) constraintToWeightMap,
                constraintMatchEnabled);
        declaredClassToNodeMap = new HashMap<>(50);
        BavetNodeBuildPolicy<Solution_> buildPolicy = new BavetNodeBuildPolicy<>(this);
        constraintToWeightMap.forEach((constraint, constraintWeight) -> constraint.createNodes(buildPolicy,
                declaredClassToNodeMap, constraintWeight));
        nodes = buildPolicy.getCreatedNodes().toArray(BavetNode[]::new);
        scoringNodeList = Arrays.stream(nodes)
                .filter(node -> node instanceof BavetScoringNode)
                .map(node -> (BavetScoringNode) node)
                .collect(Collectors.toList());
        effectiveClassToNodeListMap = new HashMap<>(declaredClassToNodeMap.size());
        fromTupleListMap = new IdentityHashMap<>(1000);
    }

    public List<BavetForEachUniNode<Object>> findFromNodeList(Class<?> factClass) {
        return effectiveClassToNodeListMap.computeIfAbsent(factClass, key -> {
            List<BavetForEachUniNode<Object>> nodeList = new ArrayList<>();
            declaredClassToNodeMap.forEach((declaredClass, declaredNode) -> {
                if (declaredClass.isAssignableFrom(factClass)) {
                    nodeList.add(declaredNode);
                }
            });
            return nodeList;
        });
    }

    public void insert(Object fact) {
        Class<?> factClass = fact.getClass();
        List<BavetForEachUniNode<Object>> fromNodeList = findFromNodeList(factClass);
        List<BavetForEachUniTuple<Object>> tupleList = new ArrayList<>(fromNodeList.size());
        List<BavetForEachUniTuple<Object>> old = fromTupleListMap.put(fact, tupleList);
        if (old != null) {
            throw new IllegalStateException("The fact (" + fact + ") was already inserted, so it cannot insert again.");
        }
        for (BavetForEachUniNode<Object> node : fromNodeList) {
            BavetForEachUniTuple<Object> tuple = node.createTuple(fact);
            tupleList.add(tuple);
            node.transitionTuple(tuple, BavetTupleState.CREATING);
        }
    }

    public void update(Object fact) {
        List<BavetForEachUniTuple<Object>> tupleList = fromTupleListMap.get(fact);
        if (tupleList == null) {
            throw new IllegalStateException("The fact (" + fact + ") was never inserted, so it cannot update.");
        }
        for (BavetForEachUniTuple<Object> tuple : tupleList) {
            tuple.getNode().transitionTuple(tuple, BavetTupleState.UPDATING);
        }
    }

    public void retract(Object fact) {
        List<BavetForEachUniTuple<Object>> tupleList = fromTupleListMap.remove(fact);
        if (tupleList == null) {
            throw new IllegalStateException("The fact (" + fact + ") was never inserted, so it cannot retract.");
        }
        for (BavetForEachUniTuple<Object> tuple : tupleList) {
            tuple.getNode().transitionTuple(tuple, BavetTupleState.DYING);
        }
    }

    public Score_ calculateScore(int initScore) {
        for (BavetNode node : nodes) {
            node.calculateScore();
        }
        return scoreInliner.extractScore(initScore);
    }

    public Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        return scoreInliner.getConstraintMatchTotalMap();
    }

    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        return scoreInliner.getIndictmentMap();
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    public AbstractScoreInliner<Score_> getScoreInliner() {
        return scoreInliner;
    }

    public BavetNode[] getNodes() {
        return nodes;
    }

    public List<BavetScoringNode> getScoringNodeList() {
        return scoringNodeList;
    }

}
