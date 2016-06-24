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
package org.optaplanner.core.impl.score.director.drools.reproducer.fact;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.impl.domain.common.ReflectionHelper;
import org.optaplanner.core.impl.domain.common.accessor.BeanPropertyMemberAccessor;
import org.slf4j.Logger;

public class ValueFact implements Fact {

    private final Object instance;
    private final String variableName;
    private final HashMap<BeanPropertyMemberAccessor, ValueProvider> attributes = new HashMap<BeanPropertyMemberAccessor, ValueProvider>();
    private final List<Fact> dependencies = new ArrayList<Fact>();

    public ValueFact(int id, Object instance) {
        this.instance = instance;
        this.variableName = instance.getClass().getSimpleName().substring(0, 1).toLowerCase() +
                instance.getClass().getSimpleName().substring(1) + "_" + id;
    }

    @Override
    public void setUp(Map<Object, Fact> existingInstances) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            String fieldName = field.getName();
            Method setter = ReflectionHelper.getSetterMethod(instance.getClass(), field.getType(), fieldName);
            Method getter = ReflectionHelper.getGetterMethod(instance.getClass(), fieldName);
            if (setter != null && getter != null) {
                BeanPropertyMemberAccessor accessor = new BeanPropertyMemberAccessor(getter);
                Object value = accessor.executeGetter(instance);
                if (value != null) {
                    if (field.getType().equals(String.class)) {
                        attributes.put(accessor, new StringValueProvider(value));
                    } else if (field.getType().isPrimitive()) {
                        attributes.put(accessor, new PrimitiveValueProvider(value));
                    } else if (field.getType().isEnum()) {
                        attributes.put(accessor, new EnumValueProvider(value));
                    } else if (existingInstances.containsKey(value)) {
                        attributes.put(accessor, new ExistingInstanceValueProvider(value, existingInstances.get(value).toString()));
                        dependencies.add(existingInstances.get(value));
                    } else if (field.getType().equals(java.util.List.class)) {
                        String id = variableName + "_" + field.getName();
                        Type[] typeArgs = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                        ListValueProvider listValueProvider = new ListValueProvider(value, id, typeArgs[0], existingInstances);
                        attributes.put(accessor, listValueProvider);
                        dependencies.addAll(listValueProvider.getFacts());
                    } else if (field.getType().equals(java.util.Map.class)) {
                        String id = variableName + "_" + field.getName();
                        Type[] typeArgs = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                        MapValueProvider mapValueProvider = new MapValueProvider(value, id, typeArgs, existingInstances);
                        attributes.put(accessor, mapValueProvider);
                        dependencies.addAll(mapValueProvider.getFacts());
                    } else if (field.getType().getName().matches("org\\.joda\\.time\\.LocalDate(Time)?")) {
                        attributes.put(accessor, new JodaTimeValueProvider(value));
                    } else {
                        throw new IllegalStateException("Unsupported type: " + field.getType());
                    }
                } else {
                    attributes.put(accessor, new NullValueProvider());
                }
            }
        }
    }

    @Override
    public List<Fact> getDependencies() {
        return dependencies;
    }

    @Override
    public void reset() {
        for (Map.Entry<BeanPropertyMemberAccessor, ValueProvider> entry : attributes.entrySet()) {
            BeanPropertyMemberAccessor accessor = entry.getKey();
            ValueProvider value = entry.getValue();
            accessor.executeSetter(instance, value.get());
        }
    }

    @Override
    public void printInitialization(Logger log) {
        log.info("    {} {} = new {}();", instance.getClass().getSimpleName(), variableName, instance.getClass().getSimpleName());
    }

    @Override
    public void printSetup(Logger log) {
        log.info("        //{}", instance);
        for (Map.Entry<BeanPropertyMemberAccessor, ValueProvider> entry : attributes.entrySet()) {
            BeanPropertyMemberAccessor accessor = entry.getKey();
            Method setter = ReflectionHelper.getSetterMethod(instance.getClass(), accessor.getType(), accessor.getName());
            ValueProvider value = entry.getValue();
            value.printSetup(log);
            log.info("        {}.{}({});", variableName, setter.getName(), value.toString());
        }
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return variableName;
    }

}
