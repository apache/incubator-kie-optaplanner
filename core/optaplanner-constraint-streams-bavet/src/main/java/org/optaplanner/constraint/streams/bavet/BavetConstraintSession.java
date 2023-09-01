/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.constraint.streams.bavet;

import java.util.IdentityHashMap;
import java.util.Map;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.uni.ForEachUniNode;
import org.optaplanner.constraint.streams.common.inliner.AbstractScoreInliner;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;

final class BavetConstraintSession<Score_ extends Score<Score_>> {

    private final AbstractScoreInliner<Score_> scoreInliner;
    private final Map<Class<?>, ForEachUniNode<Object>> declaredClassToNodeMap;
    private final AbstractNode[] nodes; // Indexed by nodeIndex
    private final Map<Class<?>, ForEachUniNode<Object>[]> effectiveClassToNodeArrayMap;

    public BavetConstraintSession(AbstractScoreInliner<Score_> scoreInliner,
            Map<Class<?>, ForEachUniNode<Object>> declaredClassToNodeMap,
            AbstractNode[] nodes) {
        this.scoreInliner = scoreInliner;
        this.declaredClassToNodeMap = declaredClassToNodeMap;
        this.nodes = nodes;
        this.effectiveClassToNodeArrayMap = new IdentityHashMap<>(declaredClassToNodeMap.size());
    }

    public void insert(Object fact) {
        Class<?> factClass = fact.getClass();
        for (ForEachUniNode<Object> node : findNodes(factClass)) {
            node.insert(fact);
        }
    }

    private ForEachUniNode<Object>[] findNodes(Class<?> factClass) {
        // Map.computeIfAbsent() would have created lambdas on the hot path, this will not.
        ForEachUniNode<Object>[] nodeArray = effectiveClassToNodeArrayMap.get(factClass);
        if (nodeArray == null) {
            nodeArray = declaredClassToNodeMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().isAssignableFrom(factClass))
                    .map(Map.Entry::getValue)
                    .toArray(ForEachUniNode[]::new);
            effectiveClassToNodeArrayMap.put(factClass, nodeArray);
        }
        return nodeArray;
    }

    public void update(Object fact) {
        Class<?> factClass = fact.getClass();
        for (ForEachUniNode<Object> node : findNodes(factClass)) {
            node.update(fact);
        }
    }

    public void retract(Object fact) {
        Class<?> factClass = fact.getClass();
        for (ForEachUniNode<Object> node : findNodes(factClass)) {
            node.retract(fact);
        }
    }

    public Score_ calculateScore(int initScore) {
        for (AbstractNode node : nodes) {
            node.calculateScore();
        }
        return scoreInliner.extractScore(initScore);
    }

    public AbstractScoreInliner<Score_> getScoreInliner() {
        return scoreInliner;
    }

    public Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        return scoreInliner.getConstraintMatchTotalMap();
    }

    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        return scoreInliner.getIndictmentMap();
    }

}
