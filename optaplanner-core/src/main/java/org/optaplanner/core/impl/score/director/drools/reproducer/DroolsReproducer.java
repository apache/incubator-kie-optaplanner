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
import java.util.Objects;
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
    private List<Fact> facts = new ArrayList<>();
    private HashMap<Object, Fact> existingInstances = new HashMap<Object, Fact>();
    private final SortedSet<String> imports = new TreeSet<>();
    private List<KieSessionInsert> initialInsertJournal = new ArrayList<>();
    private List<KieSessionOperation> updateJournal = new ArrayList<>();
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
            for (Object fact : workingFacts) {
                Fact f = new Fact(fact);
                facts.add(f);
                existingInstances.put(fact, f);
                addImport(fact);
            }

            for (Fact fact : facts) {
                fact.setUp(existingInstances);
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
        RuntimeException testException = test(oldKieSession, initialInsertJournal, updateJournal);
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
        updateJournal = cutJournalHead(originalException, oldKieSession, updateJournal);
        log.debug("\n// Now trying to remove random operations.\n");
        updateJournal = pruneUpdateJournalOperations(originalException, oldKieSession, updateJournal);
        log.debug("\n// Pruning facts.\n");
        initialInsertJournal = pruneInsertJournalOperations(originalException, oldKieSession, initialInsertJournal);
        facts.retainAll(pruneFacts());
        // TODO prune setup code
        printTest();
        // TODO check that original exception is still reproduced after fact pruning
        throw test(oldKieSession, initialInsertJournal, updateJournal);
    }

    private List<KieSessionOperation> cutJournalHead(RuntimeException ex,
                                                     KieSession kieSession,
                                                     List<KieSessionOperation> journal) {
        HeadCuttingMutator m = new HeadCuttingMutator(journal);
        while (m.canMutate()) {
            long start = System.currentTimeMillis();
            boolean reproduced = reproduce(ex, kieSession, initialInsertJournal, m.mutate());
            double tookSeconds = (System.currentTimeMillis() - start) / 1000d;
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            log.debug("// {} with journal size: {} (took {}s)", outcome, m.getResult().size(), tookSeconds);
            if (!reproduced) {
                m.revert();
            }
        }
        return m.getResult();
    }

    private List<KieSessionOperation> pruneUpdateJournalOperations(RuntimeException ex,
                                                                   KieSession kieSession,
                                                                   List<KieSessionOperation> updateJournal) {
        RemoveRandomItemMutator<KieSessionOperation> m = new RemoveRandomItemMutator<>(updateJournal);
        while (m.canMutate()) {
            log.debug("// Current journal size: {}", m.getResult().size());
            boolean reproduced = reproduce(ex, kieSession, initialInsertJournal, m.mutate());
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            log.debug("// {} without operation #{}", outcome, m.getRemovedItem().getId());
            if (!reproduced) {
                m.revert();
            }
        }
        return m.getResult();
    }

    private List<KieSessionInsert> pruneInsertJournalOperations(RuntimeException ex,
                                                                KieSession kieSession,
                                                                List<KieSessionInsert> insertJournal) {
        RemoveRandomItemMutator<KieSessionInsert> m = new RemoveRandomItemMutator<>(insertJournal);
        while (m.canMutate()) {
            log.debug("// Current journal size: {}", m.getResult().size());
            boolean reproduced = reproduce(ex, kieSession, m.mutate(), updateJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            log.debug("// {} without operation #{}", outcome, m.getRemovedItem().getId());
            if (!reproduced) {
                m.revert();
            }
        }
        return m.getResult();
    }

    private ArrayList<Fact> pruneFacts() {
        ArrayList<Fact> minimal = new ArrayList<>();
        for (KieSessionInsert insert : initialInsertJournal) {
            addWithDependencies(existingInstances.get(insert.getFact()), minimal);
        }
        for (KieSessionOperation op : updateJournal) {
            if (op instanceof KieSessionUpdate) {
                Fact f = existingInstances.get(((KieSessionUpdate) op).getValue());
                if (f != null) {
                    addWithDependencies(f, minimal);
                }
            }
        }
        return minimal;
    }

    private static void addWithDependencies(Fact f, List<Fact> factList) {
        if (factList.contains(f)) {
            return;
        }
        factList.add(f);
        for (Fact dependency : f.getDependencies()) {
            addWithDependencies(dependency, factList);
        }
    }

    private boolean reproduce(RuntimeException originalException,
                              KieSession oldKieSession,
                              List<KieSessionInsert> insertJournal,
                              List<KieSessionOperation> updateJournal) {
        RuntimeException ex = test(oldKieSession, insertJournal, updateJournal);
        if (ex == null) {
            return false;
        } else if (areEqual(originalException, ex)) {
            return true;
        } else {
            log.debug("// Unexpected exception: {}: {}", ex.getClass(), ex.getMessage());
            return false;
        }
    }

    private RuntimeException test(KieSession oldKieSession,
                                  List<KieSessionInsert> insertJournal,
                                  List<KieSessionOperation> updateJournal) {
        KieSession newKieSession = oldKieSession.getKieBase().newKieSession();

        for (String globalKey : oldKieSession.getGlobals().getGlobalKeys()) {
            newKieSession.setGlobal(globalKey, oldKieSession.getGlobal(globalKey));
        }

        // reset facts to the original state
        for (Fact fact : facts) {
            fact.reset();
        }

        // insert facts into KIE session
        for (KieSessionOperation insert : insertJournal) {
            insert.invoke(newKieSession);
        }

        // replay tested journal
        try {
            for (KieSessionOperation op : updateJournal) {
                op.invoke(newKieSession);
            }
            return null;
        } catch (RuntimeException ex) {
            return ex;
        } finally {
            newKieSession.dispose();
        }
    }

    private static boolean areEqual(RuntimeException originalException, RuntimeException testException) {
        if (!originalException.getClass().equals(testException.getClass())) {
            return false;
        }
        if (!Objects.equals(originalException.getMessage(), testException.getMessage())) {
            return false;
        }
        if (testException.getStackTrace().length == 0) {
            throw new IllegalStateException("Caught exception with empty stack trace => can't compare to the original." +
                    " Use '-XX:-OmitStackTraceInFastThrow' to turn off this optimization.", testException);
        }
        // TODO check all org.drools elements?
        return originalException.getStackTrace()[0].equals(testException.getStackTrace()[0]);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Test printing
    //------------------------------------------------------------------------------------------------------------------
    //
    private void printTest() {
        printInit();
        printSetup();

        log.info("    private void chunk1() {");

        int opCounter = 0;
        for (KieSessionOperation op : updateJournal) {
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
        updateJournal.add(new KieSessionInsert(operationId++, fact));
    }

    public void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        if (log.isInfoEnabled()) {
            updateJournal.add(new KieSessionUpdate(operationId++, entity, variableDescriptor));
        }
    }

    public void delete(Object entity) {
        updateJournal.add(new KieSessionDelete(operationId++, entity));
    }

    public void fireAllRules() {
        updateJournal.add(new KieSessionFireAllRules(operationId++));
    }

    public void dispose() {
        facts.clear();
        existingInstances.clear();
        imports.clear();
        initialInsertJournal.clear();
        updateJournal.clear();
        operationId = 0;
    }

}
