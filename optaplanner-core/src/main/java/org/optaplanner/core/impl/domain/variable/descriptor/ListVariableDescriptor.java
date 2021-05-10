/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.domain.variable.descriptor;

import java.util.List;

import org.optaplanner.core.api.domain.variable.PlanningCollectionVariable;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;

public class ListVariableDescriptor<Solution_> extends GenuineVariableDescriptor<Solution_> {

    public ListVariableDescriptor(EntityDescriptor<Solution_> entityDescriptor, MemberAccessor variableMemberAccessor) {
        super(entityDescriptor, variableMemberAccessor);
    }

    @Override
    protected void processPropertyAnnotations(DescriptorPolicy descriptorPolicy) {
        PlanningCollectionVariable planningVariableAnnotation =
                variableMemberAccessor.getAnnotation(PlanningCollectionVariable.class);
        processValueRangeRefs(descriptorPolicy, planningVariableAnnotation::valueRangeProviderRefs);
        // TODO process strength
        //processStrength(descriptorPolicy, planningVariableAnnotation);
    }

    @Override
    public boolean isListVariable() {
        return true;
    }

    @Override
    public boolean isChained() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public boolean isInitialized(Object entity) {
        return true;
    }

    public Object removeElement(Object entity, int index) {
        List<?> listVariable = (List<?>) getValue(entity);
        return listVariable.remove(index);
    }

    public void addElement(Object entity, int index, Object element) {
        List<Object> listVariable = (List<Object>) getValue(entity);
        listVariable.add(index, element);
    }

    public int getListSize(Object entity) {
        return ((List<?>) getValue(entity)).size();
    }
}
