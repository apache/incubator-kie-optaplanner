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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.reproducer.DroolsReproducer;

public class KieSessionUpdate implements KieSessionOperation {

    private final int id;
    private final Object entity;
    private final Method variableSetter;
    private final Object value;

    public KieSessionUpdate(int id, Object entity, VariableDescriptor<?> variableDescriptor) {
        this.id = id;
        this.entity = entity;
        this.value = variableDescriptor.getValue(entity);
        String variableName = variableDescriptor.getVariableName();
        String setterName = "set" + String.valueOf(variableName.charAt(0)).toUpperCase() + variableName.substring(1);
        try {
            this.variableSetter = entity.getClass().getMethod(setterName, variableDescriptor.getVariablePropertyType());
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void invoke(KieSession kieSession) {
        try {
            variableSetter.invoke(entity, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
        kieSession.update(kieSession.getFactHandle(entity), entity);
    }

    @Override
    public String toString() {
        String entityName = DroolsReproducer.getVariableName(entity);
        return "        " + entityName + "." + variableSetter.getName() + "(" + DroolsReproducer.getVariableName(value) +
                ");\n" + "        kieSession.update(kieSession.getFactHandle(" + entityName + "), " + entityName + ");";
    }

}
