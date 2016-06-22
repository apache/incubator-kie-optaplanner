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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.Fact;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionDelete;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionFireAllRules;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DroolsReproducer {

    private static final Logger log = LoggerFactory.getLogger("org.optaplanner.drools.reproducer");
    private static final int MAX_OPERATIONS_PER_METHOD = 1000;
    private final List<Fact> facts = new ArrayList<>();
    private final SortedSet<String> imports = new TreeSet<>();
    private final List<KieSessionOperation> initialInsertJournal = new ArrayList<>();
    private final List<KieSessionOperation> journal = new ArrayList<>();
    private String domainPackage;
    private int operationId = 0;

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
        if (log.isInfoEnabled()) {
            HashMap<Object, Fact> existingInstances = new HashMap<Object, Fact>();
            for (Object fact : workingFacts) {
                Fact f = new Fact(fact);
                facts.add(f);
                existingInstances.put(fact, f);
                addImport(fact);
            }

            for (Object fact : workingFacts) {
                existingInstances.get(fact).setUp(existingInstances);
            }
        }
    }

    private void addImport(Object fact) {
        String pkg = fact.getClass().getPackage().getName();
        if (!pkg.equals(domainPackage) && !pkg.startsWith("java")) {
            imports.add(fact.getClass().getCanonicalName());
        }
    }

    public void replay(KieSession oldKieSession, RuntimeException originalException) {
        RuntimeException testException = test(oldKieSession, journal);
        if (testException == null) {
            throw new IllegalStateException("Cannot reproduce original exception even without journal modifications. " +
                    "This is a bug!");
        }
        if (!areEqual(originalException, testException)) {
            throw new IllegalStateException("Cannot reproduce original exception even without journal modifications. " +
                    "This is a bug!" +
                    "\nExpected [" + originalException.getClass() + ": " + originalException.getMessage() + "]" +
                    "\nCaused [" + testException.getClass() + ": " + testException.getMessage() + "]", testException);
        }
        List<KieSessionOperation> minimalJournal = pruneFromTheStart(originalException, oldKieSession, journal);
        log.debug("\n// Now trying to remove random operations.\n");
        minimalJournal = tryRandomMutations(originalException, oldKieSession, minimalJournal);
        printTest(minimalJournal);
        throw test(oldKieSession, minimalJournal);
    }

    private List<KieSessionOperation> pruneFromTheStart(RuntimeException ex, KieSession kieSession, List<KieSessionOperation> journal) {
        double dropFactor = 0.8;
        int dropSize = 0;
        int dropIncrement = (int) (journal.size() * dropFactor);
        List<KieSessionOperation> reproducingJournal = journal;
        while (dropIncrement > 0) {
            dropSize += dropIncrement;
            List<KieSessionOperation> testedJournal = journal.subList(dropSize, journal.size());
            long start = System.currentTimeMillis();
            boolean reproduced = reproduce(ex, kieSession, testedJournal);
            double tookSeconds = (System.currentTimeMillis() - start) / 1000d;
            if (reproduced) {
                log.debug("// Reproduced with journal size: {} (took {}s)", testedJournal.size(), tookSeconds);
                reproducingJournal = testedJournal;
            } else {
                log.debug("// Can't reproduce with journal size: {} (took {}s)", testedJournal.size(), tookSeconds);
                // revert drop size
                dropSize -= dropIncrement;
                // reduce drop factor
                dropFactor /= 2;
                log.debug("// >> reducing next drop size to {}", dropIncrement);
            }
            // determine next drop increment
            dropIncrement = (int) (testedJournal.size() * dropFactor);
        }
        return new ArrayList<>(reproducingJournal);
    }

    private List<KieSessionOperation> tryRandomMutations(RuntimeException ex, KieSession kieSession, List<KieSessionOperation> journal) {
        boolean reduced = true;
        ArrayList<KieSessionOperation> reproducingJournal = new ArrayList<>(journal);
        Random random = new Random(0);
        while (reduced) {
            log.debug("// Current journal size: {}", reproducingJournal.size());
            ArrayList<Integer> indices = new ArrayList<>(reproducingJournal.size());
            for (int i = 0; i < reproducingJournal.size(); i++) {
                indices.add(i);
            }
            Collections.shuffle(indices, random);
            reduced = false;
            for (Integer index : indices) {
                ArrayList<KieSessionOperation> testedJournal = new ArrayList<>(reproducingJournal);
                KieSessionOperation op = testedJournal.get(index);
                testedJournal.remove(index.intValue());
                if (reproduce(ex, kieSession, testedJournal)) {
                    log.debug("// Reproduced without operation #{}", op.getId());
                    reproducingJournal = testedJournal;
                    reduced = true;
                    break;
                } else {
                    log.debug("// Can't reproduce without operation #{}", op.getId());
                }
            }
        }
        return reproducingJournal;
    }

    private boolean reproduce(RuntimeException originalException, KieSession oldKieSession, List<KieSessionOperation> journal) {
        RuntimeException ex = test(oldKieSession, journal);
        if (ex == null) {
            return false;
        } else if (areEqual(originalException, ex)) {
            return true;
        } else {
            log.debug("// Unexpected exception: {}: {}", ex.getClass(), ex.getMessage());
            return false;
        }
    }

    private boolean areEqual(RuntimeException originalException, RuntimeException testException) {
        return originalException.getClass().equals(testException.getClass()) &&
                Objects.equals(originalException.getMessage(), testException.getMessage());
    }

    private RuntimeException test(KieSession oldKieSession, List<KieSessionOperation> journal) {
        KieSession newKieSession = oldKieSession.getKieBase().newKieSession();

        for (String globalKey : oldKieSession.getGlobals().getGlobalKeys()) {
            newKieSession.setGlobal(globalKey, oldKieSession.getGlobal(globalKey));
        }

        // reset facts to the original state
        for (Fact fact : facts) {
            fact.reset();
        }

        // insert facts into KIE session
        for (KieSessionOperation insert : initialInsertJournal) {
            insert.invoke(newKieSession);
        }

        // replay tested journal
        try {
            for (KieSessionOperation op : journal) {
                op.invoke(newKieSession);
            }
            return null;
        } catch (RuntimeException ex) {
            return ex;
        } finally {
            newKieSession.dispose();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Test printing
    //------------------------------------------------------------------------------------------------------------------
    //
    private void printTest(List<KieSessionOperation> journal) {
        printInit();
        printSetup();

        log.info("    private void chunk1() {");

        int opCounter = 0;
        for (KieSessionOperation op : journal) {
            opCounter++;
            if (opCounter % MAX_OPERATIONS_PER_METHOD == 0) {
                // There's 64k limit for Java method size so we need to split into multiple methods
                log.info("    }\n");
                log.info("    private void chunk{}() {", opCounter / MAX_OPERATIONS_PER_METHOD + 1);
            }
            log.debug("        //operation #{}", op.getId());
            log.info("{}", op);
        }

        log.info(
                "    }\n");
        log.info(
                "    @Test\n" +
                "    public void test() {");
        for (int i = 1; i <= opCounter / MAX_OPERATIONS_PER_METHOD + 1; i++) {
            log.info("        chunk{}();", i);
        }
        log.info(
                "    }\n}");
    }

    private void printInit() {
        log.info(
                "package {};\n", domainPackage);
        imports.add("org.junit.Before");
        imports.add("org.junit.Test");
        imports.add("org.kie.api.KieServices");
        imports.add("org.kie.api.builder.KieFileSystem");
        imports.add("org.kie.api.builder.model.KieModuleModel");
        imports.add("org.kie.api.io.ResourceType");
        imports.add("org.kie.api.runtime.KieContainer");
        imports.add("org.kie.api.runtime.KieSession");
        for (String cls : imports) {
            log.info("import {};", cls);
        }
        log.info(
                "\n" +
                "public class DroolsReproducerTest {\n" +
                "\n" +
                "    KieSession kieSession;");
        for (Fact fact : facts) {
            fact.printInitialization(log);
        }
        log.info("");
    }

    private void printSetup() {
        log.info(
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
        log.info("");
        for (KieSessionOperation insert : initialInsertJournal) {
            log.debug("        //operation #{}", insert.getId());
            log.info("{}", insert);
        }
        log.info(
                "    }\n");
    }

    //------------------------------------------------------------------------------------------------------------------
    // KIE session operations
    //------------------------------------------------------------------------------------------------------------------
    //
    public void insertInitial(Object fact) {
        initialInsertJournal.add(new KieSessionInsert(operationId++, fact));
    }

    public void insert(Object fact) {
        journal.add(new KieSessionInsert(operationId++, fact));
    }

    public void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        if (log.isInfoEnabled()) {
            journal.add(new KieSessionUpdate(operationId++, entity, variableDescriptor));
        }
    }

    public void delete(Object entity) {
        journal.add(new KieSessionDelete(operationId++, entity));
    }

    public void fireAllRules() {
        journal.add(new KieSessionFireAllRules(operationId++));
    }

    public void dispose() {
        facts.clear();
        imports.clear();
        initialInsertJournal.clear();
        journal.clear();
        operationId = 0;
    }

}
