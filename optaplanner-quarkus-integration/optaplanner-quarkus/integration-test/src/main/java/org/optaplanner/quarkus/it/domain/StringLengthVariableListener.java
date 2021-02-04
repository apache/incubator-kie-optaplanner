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

package org.optaplanner.quarkus.it.domain;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public class StringLengthVariableListener implements VariableListener<ITestdataPlanningSolution, ITestdataPlanningEntity> {

    @Override
    public void beforeEntityAdded(ScoreDirector<ITestdataPlanningSolution> scoreDirector,
            ITestdataPlanningEntity iTestdataPlanningEntity) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector<ITestdataPlanningSolution> scoreDirector,
            ITestdataPlanningEntity iTestdataPlanningEntity) {

    }

    @Override
    public void beforeVariableChanged(ScoreDirector<ITestdataPlanningSolution> scoreDirector,
            ITestdataPlanningEntity iTestdataPlanningEntity) {
    }

    @Override
    public void afterVariableChanged(ScoreDirector<ITestdataPlanningSolution> scoreDirector,
            ITestdataPlanningEntity iTestdataPlanningEntity) {
        int oldLength = (iTestdataPlanningEntity.getLength() != null) ? iTestdataPlanningEntity.getLength() : 0;
        int newLength = getLength(iTestdataPlanningEntity.getValue());
        if (oldLength != newLength) {
            scoreDirector.beforeVariableChanged(iTestdataPlanningEntity, "length");
            iTestdataPlanningEntity.setLength(getLength(iTestdataPlanningEntity.getValue()));
            scoreDirector.afterVariableChanged(iTestdataPlanningEntity, "length");
        }
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<ITestdataPlanningSolution> scoreDirector,
            ITestdataPlanningEntity iTestdataPlanningEntity) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector<ITestdataPlanningSolution> scoreDirector,
            ITestdataPlanningEntity iTestdataPlanningEntity) {

    }

    private static int getLength(String value) {
        if (value != null) {
            return value.length();
        } else {
            return 0;
        }
    }
}
