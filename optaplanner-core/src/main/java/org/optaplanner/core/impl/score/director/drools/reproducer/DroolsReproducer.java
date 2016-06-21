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
package org.optaplanner.core.impl.score.director.drools.reproducer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionDelete;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionDispose;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionFireAllRules;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DroolsReproducer {

    private static final Logger log = LoggerFactory.getLogger("org.optaplanner.drools.reproducer");
    private static final int MAX_OPERATIONS_PER_METHOD = 1000;
    private final List<KieSessionOperation> journal = new ArrayList<>();
    private final List<Fact> facts = new ArrayList<>();
    private String domainPackage;

    public static String getVariableName(Object fact) {
        try {
            return fact == null ? "null"
                    : "fact" + fact.getClass().getSimpleName() + "_" +
                    fact.getClass().getMethod("getId").invoke(fact);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            log.error("Cannot create variable name", ex);
        }
        return null;
    }

    public void setDomainPackage(String domainPackage) {
        this.domainPackage = domainPackage;
    }

    private void printInit() {
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
                // TODO don't hard-code score DRL
                "        kfs.write(kieServices.getResources().newClassPathResource(\"org/optaplanner/examples/nurserostering/solver/nurseRosteringScoreRules.drl\").setResourceType(ResourceType.DRL));\n" +
                "        kieServices.newKieBuilder(kfs).buildAll();\n" +
                "        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());\n" +
                "        kieSession = kieContainer.newKieSession();\n" +
                "");
    }

    public void addFacts(Collection<Object> workingFacts) {
        if (log.isDebugEnabled()) {
            HashMap<Object, Fact> existingInstances = new HashMap<Object, Fact>();
            for (Object fact : workingFacts) {
                Fact f = new Fact(fact);
                facts.add(f);
                existingInstances.put(fact, f);
            }

            for (Object fact : workingFacts) {
                existingInstances.get(fact).setUp(existingInstances);
            }
        }
    }

    public void replay(KieSession oldKieSession) {
        for (Fact f : facts) {
            f.reset();
        }

        KieSession newKieSession = oldKieSession.getKieBase().newKieSession();
        oldKieSession.dispose();

        try {
            for (KieSessionOperation op : journal) {
                op.invoke(newKieSession);
            }
        } finally {
            dump(journal);
        }

    }

    private void dump(List<KieSessionOperation> record) {
        printInit();
        for (Fact fact : facts) {
            fact.printSetup(log);
        }
        log.debug("    }\n");

        for (Fact fact : facts) {
            fact.printInitialization(log);
        }

        log.debug("\n    private void chunk1() {");

        int opCounter = 0;
        for (KieSessionOperation op : record) {
            opCounter++;
            if (opCounter % MAX_OPERATIONS_PER_METHOD == 0) {
                // There's 64k limit for Java method size so we need to split into multiple methods
                log.debug("    }\n");
                log.debug("    private void chunk{}() {", opCounter / MAX_OPERATIONS_PER_METHOD + 1);
            }
            log.debug("{}", op);
        }

        log.debug(
                "    }\n");
        log.debug(
                "    @Test\n" +
                "    public void test() {");
        for (int i = 1; i <= opCounter / MAX_OPERATIONS_PER_METHOD + 1; i++) {
            log.debug("        chunk{}();", i);
        }
        log.debug("    }\n}");
    }

    //------------------------------------------------------------------------------------------------------------------
    // KIE session
    //------------------------------------------------------------------------------------------------------------------
    //
    public void insert(Object fact) {
        journal.add(new KieSessionInsert(fact));
    }

    public void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        if (log.isDebugEnabled()) {
            journal.add(new KieSessionUpdate(entity, variableDescriptor));
        }
    }

    public void delete(Object entity) {
        journal.add(new KieSessionDelete(entity));
    }

    public void fireAllRules() {
        journal.add(new KieSessionFireAllRules());
    }

    public void dispose() {
        journal.add(new KieSessionDispose());
    }

    private static class Fact {

        private final Object instance;
        private final String variableName;
        private final HashMap<String, Method> getters = new HashMap<>();
        private final HashMap<String, Method> setters = new HashMap<>();
        private final HashMap<Method, ValueProvider> attributes = new HashMap<>();

        public Fact(Object instance) {
            this.instance = instance;
            this.variableName = getVariableName(instance);
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

        public void setUp(Map<Object, Fact> existingInstances) {
            for (Field field : instance.getClass().getDeclaredFields()) {
                String fieldKey = field.getName().toLowerCase();
                if (setters.containsKey(fieldKey) && getters.containsKey(fieldKey)) {
                    Object value;
                    try {
                        value = getters.get(fieldKey).invoke(instance);
                    } catch (Exception ex) {
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
                            attributes.put(setter, new ExistingInstanceValueProvider(value, existingInstances.get(value).variableName));
                        } else if (field.getType().equals(java.util.List.class)) {
                            String id = variableName + "_" + field.getName();
                            attributes.put(setter, new ListValueProvider(value, id, existingInstances));
                        } else if (field.getType().equals(java.util.Map.class)) {
                            String id = variableName + "_" + field.getName();
                            attributes.put(setter, new MapValueProvider(value, id, existingInstances));
                        } else {
                            throw new IllegalStateException("Unsupported type: " + field.getType());
                        }
                    }
                }
            }
        }

        public void set(String variableName, Object value) {
            try {
                setters.get(variableName).invoke(value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

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

        public void printInitialization(Logger log) {
            log.debug("    {} {} = new {}();", instance.getClass().getSimpleName(), variableName, instance.getClass().getSimpleName());
        }

        public void printSetup(Logger log) {
            log.debug("        //{}", instance);
            for (Map.Entry<Method, ValueProvider> entry : attributes.entrySet()) {
                Method setter = entry.getKey();
                ValueProvider value = entry.getValue();
                value.printSetup(log);
                log.debug("        {}.{}({});", variableName, setter.getName(), value.toString());
            }
        }

        @Override
        public String toString() {
            return variableName;
        }
    }

    private interface ValueProvider {

        Object get();

        void printSetup(Logger log);
    }

    private static abstract class AbstractValueProvider implements ValueProvider {

        protected final Object value;

        public AbstractValueProvider(Object value) {
            this.value = value;
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public void printSetup(Logger log) {
            // no setup required
        }

    }

    private static class PrimitiveValueProvider extends AbstractValueProvider {

        public PrimitiveValueProvider(Object value) {
            super(value);
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    private static class StringValueProvider extends AbstractValueProvider {

        public StringValueProvider(Object value) {
            super(value);
        }

        @Override
        public String toString() {
            return '"' + (String) value + '"';
        }
    }

    private static class EnumValueProvider extends AbstractValueProvider {

        public EnumValueProvider(Object value) {
            super(value);
        }

        @Override
        public String toString() {
            return value.getClass().getSimpleName() + "." + ((Enum) value).name();
        }
    }

    private static class ExistingInstanceValueProvider extends AbstractValueProvider {

        private final String identifier;

        public ExistingInstanceValueProvider(Object value, String identifier) {
            super(value);
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

    private static class ListValueProvider extends AbstractValueProvider {

        private final String identifier;
        private final Map<Object, Fact> existingInstances;

        public ListValueProvider(Object value, String identifier, Map<Object, Fact> existingInstances) {
            super(value);
            this.identifier = identifier;
            this.existingInstances = existingInstances;
        }

        @Override
        public void printSetup(Logger log) {
            log.debug("        java.util.ArrayList {} = new java.util.ArrayList();", identifier);
            for (Object item : ((java.util.List<?>) value)) {
                log.debug("        //{}", item);
                log.debug("        {}.add({});", identifier, existingInstances.get(item));
            }
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

    private static class MapValueProvider extends AbstractValueProvider {

        private final String identifier;
        private final Map<Object, Fact> existingInstances;

        public MapValueProvider(Object value, String identifier, Map<Object, Fact> existingInstances) {
            super(value);
            this.identifier = identifier;
            this.existingInstances = existingInstances;
        }

        @Override
        public void printSetup(Logger log) {
            log.debug("        java.util.HashMap {} = new java.util.HashMap();", identifier);
            for (Map.Entry<? extends Object, ? extends Object> entry : ((java.util.Map<?, ?>) value).entrySet()) {
                log.debug("        //{} => {}", entry.getKey(), entry.getValue());
                log.debug("        {}.put({}, {});", identifier,
                          existingInstances.get(entry.getKey()),
                          existingInstances.get(entry.getValue()));
            }
        }

        @Override
        public String toString() {
            return identifier;
        }
    }
}
