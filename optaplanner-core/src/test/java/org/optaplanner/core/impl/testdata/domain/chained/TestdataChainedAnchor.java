/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.testdata.domain.chained;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataChainedAnchor extends TestdataObject implements TestdataChainedObject {
    private TestdataChainedEntity nextChainedEntity;

    public TestdataChainedAnchor() {
    }

    public TestdataChainedAnchor(String code) {
        super(code);
    }

    @Override
    @InverseRelationShadowVariable(sourceVariableName = "chainedObject")
    public TestdataChainedEntity getNextChainedEntity() {
        return nextChainedEntity;
    }

    public void setNextChainedEntity(TestdataChainedEntity nextChainedEntity) {
        this.nextChainedEntity = nextChainedEntity;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
}
