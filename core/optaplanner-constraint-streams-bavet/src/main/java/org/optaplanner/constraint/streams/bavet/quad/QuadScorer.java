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

package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.AbstractScorer;
import org.optaplanner.constraint.streams.common.inliner.UndoScoreImpacter;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.Score;

final class QuadScorer<A, B, C, D> extends AbstractScorer<QuadTuple<A, B, C, D>> {

    private final QuadFunction<A, B, C, D, UndoScoreImpacter> scoreImpacter;

    public QuadScorer(String constraintPackage, String constraintName, Score<?> constraintWeight,
            QuadFunction<A, B, C, D, UndoScoreImpacter> scoreImpacter, int inputStoreIndex) {
        super(constraintPackage, constraintName, constraintWeight, inputStoreIndex);
        this.scoreImpacter = scoreImpacter;
    }

    @Override
    protected UndoScoreImpacter impact(QuadTuple<A, B, C, D> tuple) {
        try {
            return scoreImpacter.apply(tuple.getFactA(), tuple.getFactB(), tuple.getFactC(), tuple.getFactD());
        } catch (Exception e) {
            throw createExceptionOnImpact(tuple, e);
        }
    }
}
