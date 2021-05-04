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

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.List;

import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;

public class DefaultListVariableDescriptor<Solution_> extends VariableDescriptor<Solution_> {

    public DefaultListVariableDescriptor(EntityDescriptor<Solution_> entityDescriptor, MemberAccessor variableMemberAccessor) {
        super(entityDescriptor, variableMemberAccessor);
    }

    @Override
    public void linkVariableDescriptors(DescriptorPolicy descriptorPolicy) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean isGenuineAndUninitialized(Object entity) {
        throw new UnsupportedOperationException("Not yet implemented.");
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
