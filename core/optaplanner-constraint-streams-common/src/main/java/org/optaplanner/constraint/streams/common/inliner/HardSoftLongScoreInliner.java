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

package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftLongScoreInliner extends AbstractScoreInliner<HardSoftLongScore> {

    private long hardScore;
    private long softScore;

    HardSoftLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<HardSoftLongScore, HardSoftLongScoreContext> buildWeightedScoreImpacter(Constraint constraint,
            HardSoftLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        HardSoftLongScoreContext context =
                new HardSoftLongScoreContext(this, constraint, constraintWeight,
                        impact -> this.hardScore += impact, impact -> this.softScore += impact);
        if (constraintWeight.softScore() == 0L) {
            return WeightedScoreImpacter.of(context,
                    (HardSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> ctx
                            .changeHardScoreBy(matchWeight, justificationsSupplier));
        } else if (constraintWeight.hardScore() == 0L) {
            return WeightedScoreImpacter.of(context,
                    (HardSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> ctx
                            .changeSoftScoreBy(matchWeight, justificationsSupplier));
        } else {
            return WeightedScoreImpacter.of(context,
                    (HardSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> ctx
                            .changeScoreBy(matchWeight, justificationsSupplier));
        }
    }

    @Override
    public HardSoftLongScore extractScore(int initScore) {
        return HardSoftLongScore.ofUninitialized(initScore, hardScore, softScore);
    }

    @Override
    public String toString() {
        return HardSoftLongScore.class.getSimpleName() + " inliner";
    }

}
