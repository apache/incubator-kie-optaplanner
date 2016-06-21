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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.Fact;
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
    private final List<KieSessionOperation> initialInsertJournal = new ArrayList<>();
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
            for (KieSessionOperation insert : initialInsertJournal) {
                insert.invoke(newKieSession);
            }
            for (KieSessionOperation op : journal) {
                op.invoke(newKieSession);
            }
        } finally {
            printTest(journal);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Test printing
    //------------------------------------------------------------------------------------------------------------------
    //
    private void printTest(List<KieSessionOperation> journal) {
        printInit();
        printSetup();

        log.debug("    private void chunk1() {");

        int opCounter = 0;
        for (KieSessionOperation op : journal) {
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
        log.debug(
                "    }\n}");
    }

    private void printInit() {
        log.debug(
                "package {};\n", domainPackage);
        log.debug(
                "import org.junit.Before;\n" +
                "import org.junit.Test;\n" +
                "import org.kie.api.KieServices;\n" +
                "import org.kie.api.builder.KieFileSystem;\n" +
                "import org.kie.api.builder.model.KieModuleModel;\n" +
                "import org.kie.api.io.ResourceType;\n" +
                "import org.kie.api.runtime.KieContainer;\n" +
                "import org.kie.api.runtime.KieSession;");
        // TODO import fact classes outside the domain package
        log.debug(
                "\n" +
                "public class DroolsReproducerTest {\n" +
                "\n" +
                "    KieSession kieSession;");
        for (Fact fact : facts) {
            fact.printInitialization(log);
        }
        log.debug("");
    }

    private void printSetup() {
        log.debug(
                "    @Before\n" +
                "    public void setUp() {\n" +
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
        for (Fact fact : facts) {
            fact.printSetup(log);
        }
        log.debug("");
        for (KieSessionOperation insert : initialInsertJournal) {
            log.debug("{}", insert);
        }
        log.debug(
                "    }\n");
    }

    //------------------------------------------------------------------------------------------------------------------
    // KIE session operations
    //------------------------------------------------------------------------------------------------------------------
    //
    public void insertInitial(Object fact) {
        initialInsertJournal.add(new KieSessionInsert(fact));
    }

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

}
