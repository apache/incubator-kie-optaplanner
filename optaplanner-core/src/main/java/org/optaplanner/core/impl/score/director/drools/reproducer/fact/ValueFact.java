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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueFact implements Fact {

    private static final Logger log = LoggerFactory.getLogger(Fact.class);
    private final Object instance;
    private final String variableName;
    private final HashMap<String, Method> getters = new HashMap<>();
    private final HashMap<String, Method> setters = new HashMap<>();
    private final HashMap<Method, ValueProvider> attributes = new HashMap<>();
    private final List<Fact> dependencies = new ArrayList<>();

    public ValueFact(int id, Object instance) {
        this.instance = instance;
        this.variableName = instance.getClass().getSimpleName().substring(0, 1).toLowerCase() +
                instance.getClass().getSimpleName().substring(1) + "_" + id;
        for (Method method : instance.getClass().getMethods()) {
            String methodName = method.getName();
            String fieldName = methodName.replaceFirst("^(set|get|is)", "").toLowerCase();
            if (method.getReturnType().equals(Void.TYPE)) {
                setters.put(fieldName, method);
            } else {
                getters.put(fieldName, method);
            }
        }
    }

    @Override
    public void setUp(Map<Object, Fact> existingInstances) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            String fieldKey = field.getName().toLowerCase();
            if (setters.containsKey(fieldKey) && getters.containsKey(fieldKey)) {
                Object value;
                try {
                    value = getters.get(fieldKey).invoke(instance);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.error("Can't get value of {}.{}()", variableName, getters.get(fieldKey).getName());
                    throw new RuntimeException(ex);
                }
                Method setter = setters.get(fieldKey);
                if (value != null) {
                    if (field.getType().equals(String.class)) {
                        attributes.put(setter, new StringValueProvider(value));
                    } else if (field.getType().isPrimitive()) {
                        attributes.put(setter, new PrimitiveValueProvider(value));
                    } else if (field.getType().isEnum()) {
                        attributes.put(setter, new EnumValueProvider(value));
                    } else if (existingInstances.containsKey(value)) {
                        attributes.put(setter, new ExistingInstanceValueProvider(value, existingInstances.get(value).toString()));
                        dependencies.add(existingInstances.get(value));
                    } else if (field.getType().equals(java.util.List.class)) {
                        String id = variableName + "_" + field.getName();
                        ListValueProvider listValueProvider = new ListValueProvider(value, id, existingInstances);
                        attributes.put(setter, listValueProvider);
                        dependencies.addAll(listValueProvider.getFacts());
                    } else if (field.getType().equals(java.util.Map.class)) {
                        String id = variableName + "_" + field.getName();
                        MapValueProvider mapValueProvider = new MapValueProvider(value, id, existingInstances);
                        attributes.put(setter, mapValueProvider);
                        dependencies.addAll(mapValueProvider.getFacts());
                    } else if (field.getType().getName().matches("org\\.joda\\.time\\.LocalDate(Time)?")) {
                        attributes.put(setter, new JodaTimeValueProvider(value));
                    } else {
                        throw new IllegalStateException("Unsupported type: " + field.getType());
                    }
                } else {
                    attributes.put(setter, new NullValueProvider());
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
        for (Map.Entry<Method, ValueProvider> entry : attributes.entrySet()) {
            Object originalValue = entry.getValue().get();
            try {
                entry.getKey().invoke(instance, originalValue);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void printInitialization(Logger log) {
        log.info("    {} {} = new {}();", instance.getClass().getSimpleName(), variableName, instance.getClass().getSimpleName());
    }

    @Override
    public void printSetup(Logger log) {
        log.info("        //{}", instance);
        for (Map.Entry<Method, ValueProvider> entry : attributes.entrySet()) {
            Method setter = entry.getKey();
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
