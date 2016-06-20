/*
 * Copyright 2016 JBoss by Red Hat.
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
package org.optaplanner.core.impl.score.director.drools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DroolsReproducer {

    private static final Logger log = LoggerFactory.getLogger("org.optaplanner.drools.reproducer");
    private static final int MAX_UPDATES_PER_METHOD = 1000;
    private int updates = 0;
    private String domainPackage;

    private String getVariableName(Object entity) {
        try {
            return entity == null ? "null"
                    : "fact" + entity.getClass().getSimpleName() + "_" +
                    entity.getClass().getMethod("getId").invoke(entity);
        } catch (SecurityException ex) {
            log.error("Cannot create variable name", ex);
        } catch (IllegalArgumentException ex) {
            log.error("Cannot create variable name", ex);
        } catch (IllegalAccessException ex) {
            log.error("Cannot create variable name", ex);
        } catch (NoSuchMethodException ex) {
            log.error("Cannot create variable name", ex);
        } catch (InvocationTargetException ex) {
            log.error("Cannot create variable name", ex);
        }
        return null;
    }

    void setDomainPackage(String domainPackage) {
        this.domainPackage = domainPackage;
    }

    void init() {
        log.debug("package {};\n", domainPackage);
        log.debug("import org.junit.Before;");
        log.debug("import org.junit.Test;");
        log.debug("import org.kie.api.KieServices;\n" +
                "import org.kie.api.builder.KieFileSystem;\n" +
                "import org.kie.api.builder.model.KieModuleModel;\n" +
                "import org.kie.api.io.ResourceType;\n" +
                "import org.kie.api.runtime.KieContainer;\n" +
                "import org.kie.api.runtime.KieSession;");
        log.debug("\npublic class DroolsReproducerTest {\n");
        log.debug("    KieSession kieSession;");
        log.debug("\n    @Before");
        log.debug("    public void setUp() {\n" +
                "        KieServices kieServices = KieServices.Factory.get();\n" +
                "        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();\n" +
                "        KieFileSystem kfs = kieServices.newKieFileSystem();\n" +
                "        kfs.writeKModuleXML(kieModuleModel.toXML());\n" +
                "        kfs.write(kieServices.getResources().newClassPathResource(\"org/optaplanner/examples/nurserostering/solver/nurseRosteringScoreRules.drl\").setResourceType(ResourceType.DRL));\n" +
                "        kieServices.newKieBuilder(kfs).buildAll();\n" +
                "        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());\n" +
                "        kieSession = kieContainer.newKieSession();\n" +
                "");
    }

    void addFacts(Collection<Object> workingFacts) {
        if (log.isDebugEnabled()) {
            HashMap<Object, String> variables = new HashMap<Object, String>();
            HashSet<Class<?>> classes = new HashSet<Class<?>>();
            for (Object fact : workingFacts) {
                variables.put(fact, getVariableName(fact));
                classes.add(fact.getClass());
            }
            for (Object fact : workingFacts) {
                log.debug("        //{}", fact);
                HashMap<String, Method> getters = new HashMap<String, Method>();
                HashMap<String, Method> setters = new HashMap<String, Method>();
                for (Method method : fact.getClass().getMethods()) {
                    String methodName = method.getName();
                    String fieldName = methodName.replaceFirst("^(set|get|is)", "").toLowerCase();
                    if (method.getReturnType().equals(Void.TYPE)) {
                        setters.put(fieldName, method);
                    } else {
                        getters.put(fieldName, method);
                    }
                }
                for (Field field : fact.getClass().getDeclaredFields()) {
                    String fieldKey = field.getName().toLowerCase();
                    if (setters.containsKey(fieldKey) && getters.containsKey(fieldKey)) {
                        Object value = null;
                        try {
                            value = getters.get(fieldKey).invoke(fact);
                        } catch (Exception ex) {
                            log.error("Can't get value of {}.{}()", variables.get(fact), getters.get(fieldKey).getName());
                            throw new RuntimeException(ex);
                        }
                        if (value != null) {
                            String valueRef = null;
                            if (field.getType().equals(String.class)) {
                                valueRef = '"' + (String) value + '"';
                            } else if (field.getType().isPrimitive()) {
                                valueRef = value.toString();
                            } else if (field.getType().isEnum()) {
                                valueRef = value.getClass().getSimpleName() + "." + ((Enum) value).name();
                            } else if (classes.contains(value.getClass())) {
                                valueRef = variables.get(value);
                            } else if (field.getType().equals(java.util.List.class)) {
                                valueRef = variables.get(fact) + "_" + field.getName();
                                log.debug("        java.util.ArrayList {} = new java.util.ArrayList();", valueRef);
                                for (Object item : ((java.util.List<?>) value)) {
                                    log.debug("        //{}", item);
                                    log.debug("        {}.add({});", valueRef, variables.get(item));
                                }
                            } else if (field.getType().equals(java.util.Map.class)) {
                                valueRef = variables.get(fact) + "_" + field.getName();
                                log.debug("        java.util.HashMap {} = new java.util.HashMap();", valueRef);
                                for (Map.Entry<? extends Object, ? extends Object> entry : ((java.util.Map<?, ?>) value).entrySet()) {
                                    log.debug("        //{} => {}", entry.getKey(), entry.getValue());
                                    log.debug("        {}.put({}, {});", valueRef,
                                              variables.get(entry.getKey()),
                                              variables.get(entry.getValue()));
                                }
                            } else {
                                throw new IllegalStateException("Unsupported type: " + field.getType());
                            }
                            log.debug("        {}.{}({});",
                                      variables.get(fact),
                                      setters.get(fieldKey).getName(),
                                      valueRef
                            );
                        }
                    }
                }
            }
        }
        log.debug("    }\n");

        for (Object fact : workingFacts) {
            log.debug("    {} {} = new {}();", fact.getClass().getSimpleName(), getVariableName(fact), fact.getClass().getSimpleName());
        }
        log.debug("\n    private void chunk1() {");
    }

    void close() {
        log.debug(
                "    }\n");
        log.debug(
                "    @Test\n" +
                "    public void test() {");
        for (int i = 1; i <= updates / MAX_UPDATES_PER_METHOD + 1; i++) {
            log.debug("        chunk{}();", i);
        }
        log.debug("    }\n}");
    }

    //------------------------------------------------------------------------------------------------------------------
    // KIE session
    //------------------------------------------------------------------------------------------------------------------
    //
    void dispose() {
        log.debug("        kieSession.dispose();");
    }

    void insert(Object fact) {
        log.debug("        kieSession.insert({});", getVariableName(fact));
    }

    void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        if (log.isDebugEnabled()) {
            updates++;
            if (updates % MAX_UPDATES_PER_METHOD == 0) {
                // There's 64k limit for Java method size so we need to split into multiple methods
                log.debug("    }\n");
                log.debug("    private void chunk{}() {", updates / MAX_UPDATES_PER_METHOD + 1);
            }
            String variableName = variableDescriptor.getVariableName();
            String entityName = getVariableName(entity);
            Object value = variableDescriptor.getValue(entity);
            log.debug("        {}.set{}({});",
                      entityName,
                      String.valueOf(variableName.charAt(0)).toUpperCase() + variableName.substring(1),
                      getVariableName(value));
            log.debug("        kieSession.update(kieSession.getFactHandle({}), {});", entityName, entityName);
        }
    }

    void delete(Object entity) {
        String entityName = getVariableName(entity);
        log.debug("        kieSession.delete(kieSession.getFactHandle({}), {});", entityName, entityName);
    }

    void fireAllRules() {
        log.debug("\n        kieSession.fireAllRules();\n");
    }
}
