/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.score.director.drools.reproducer.operation;

import java.lang.reflect.Method;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.core.impl.domain.common.ReflectionHelper;
import org.optaplanner.core.impl.domain.common.accessor.BeanPropertyMemberAccessor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.Fact;

public class KieSessionUpdate implements KieSessionOperation {

    private final int id;
    private final Fact entity;
    private final BeanPropertyMemberAccessor accessor;
    private final String setterName;
    private final Fact value;

    public KieSessionUpdate(int id, Fact entity, VariableDescriptor<?> variableDescriptor, Fact value) {
        this.id = id;
        this.entity = entity;
        this.value = value;
        Method getter = ReflectionHelper.getGetterMethod(
                variableDescriptor.getEntityDescriptor().getEntityClass(),
                variableDescriptor.getVariableName());
        setterName = ReflectionHelper.getSetterMethod(
                getter.getDeclaringClass(), getter.getReturnType(), variableDescriptor.getVariableName()).getName();
        accessor = new BeanPropertyMemberAccessor(getter);
    }

    public Fact getValue() {
        return value;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void invoke(KieSession kieSession) {
        accessor.executeSetter(entity.getInstance(), value.getInstance());
        FactHandle fh = kieSession.getFactHandle(entity.getInstance());
        if (fh == null) {
            throw new IllegalStateException("No fact handle for " + entity);
        }
        kieSession.update(fh, entity.getInstance());
    }

    @Override
    public String toString() {
        return "        " + entity + "." + setterName + "(" + value + ");\n" +
                "        kieSession.update(kieSession.getFactHandle(" + entity + "), " + entity + ");";
    }

}
