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

package org.optaplanner.core.impl.testdata.domain.collection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningEntity
public class TestdataEntityCollectionPropertyEntity extends TestdataObject {

    public static EntityDescriptor buildEntityDescriptor() {
        SolutionDescriptor solutionDescriptor = TestdataEntityCollectionPropertySolution.buildSolutionDescriptor();
        return solutionDescriptor.findEntityDescriptorOrFail(TestdataEntityCollectionPropertyEntity.class);
    }

    public List<TestdataEntityCollectionPropertyEntity> entityList;
    public Set<TestdataEntityCollectionPropertyEntity> entitySet;
    public Map<String, TestdataEntityCollectionPropertyEntity> stringToEntityMap;
    public Map<TestdataEntityCollectionPropertyEntity, String> entityToStringMap;
    public Map<String, List<TestdataEntityCollectionPropertyEntity>> stringToEntityListMap;

    public TestdataValue value;

    public TestdataEntityCollectionPropertyEntity() {
    }

    public TestdataEntityCollectionPropertyEntity(String code) {
        super(code);
    }

    public TestdataEntityCollectionPropertyEntity(String code, TestdataValue value) {
        this(code);
        this.value = value;
    }

    public List<TestdataEntityCollectionPropertyEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataEntityCollectionPropertyEntity> entityList) {
        this.entityList = entityList;
    }

    public Set<TestdataEntityCollectionPropertyEntity> getEntitySet() {
        return entitySet;
    }

    public void setEntitySet(Set<TestdataEntityCollectionPropertyEntity> entitySet) {
        this.entitySet = entitySet;
    }

    public Map<String, TestdataEntityCollectionPropertyEntity> getStringToEntityMap() {
        return stringToEntityMap;
    }

    public void setStringToEntityMap(Map<String, TestdataEntityCollectionPropertyEntity> stringToEntityMap) {
        this.stringToEntityMap = stringToEntityMap;
    }

    public Map<TestdataEntityCollectionPropertyEntity, String> getEntityToStringMap() {
        return entityToStringMap;
    }

    public void setEntityToStringMap(Map<TestdataEntityCollectionPropertyEntity, String> entityToStringMap) {
        this.entityToStringMap = entityToStringMap;
    }

    public Map<String, List<TestdataEntityCollectionPropertyEntity>> getStringToEntityListMap() {
        return stringToEntityListMap;
    }

    public void setStringToEntityListMap(Map<String, List<TestdataEntityCollectionPropertyEntity>> stringToEntityListMap) {
        this.stringToEntityListMap = stringToEntityListMap;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getValue() {
        return value;
    }

    public void setValue(TestdataValue value) {
        this.value = value;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
