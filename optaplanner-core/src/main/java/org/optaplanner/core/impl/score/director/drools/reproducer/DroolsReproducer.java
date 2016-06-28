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
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.NullFact;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.ValueFact;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionDelete;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionFireAllRules;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DroolsReproducer {

    private static final Logger log = LoggerFactory.getLogger(DroolsReproducer.class);
    private static final Logger reproducerLog = LoggerFactory.getLogger("org.optaplanner.drools.reproducer");
    private boolean enabled = reproducerLog.isInfoEnabled();
    private String domainPackage;
    private List<Fact> facts = new ArrayList<Fact>();
    private HashMap<Object, Fact> existingInstances = new HashMap<Object, Fact>();
    private List<KieSessionInsert> initialInsertJournal = new ArrayList<KieSessionInsert>();
    private List<KieSessionOperation> updateJournal = new ArrayList<KieSessionOperation>();
    private int operationId = 0;

    public void setDomainPackage(String domainPackage) {
        this.domainPackage = domainPackage;
    }

    public void addFacts(Collection<Object> workingFacts) {
        if (enabled) {
            int i = 0;
            for (Object fact : workingFacts) {
                Fact f = new ValueFact(i++, fact);
                facts.add(f);
                existingInstances.put(fact, f);
            }

            for (Fact fact : facts) {
                fact.setUp(existingInstances);
            }
        }
    }

    public void replay(KieSession oldKieSession, RuntimeException originalException) {
        if (!enabled) {
            for (StackTraceElement element : originalException.getStackTrace()) {
                if (element.getClassName().startsWith("org.drools")) {
                    log.info("OptaPlanner failed due to an exception in Drools. This is probably a bug in Drools. " +
                            "You may activate an automatic reproducer generator by setting 'org.optaplanner.drools.reproducer' " +
                            "logger level to INFO or lower.");
                    break;
                }
            }
            throw originalException;
        }

        log.info("Starting replay & reduce to find a minimal Drools reproducer for:", originalException);
        assertOriginalExceptionReproduced(originalException, test(oldKieSession, initialInsertJournal, updateJournal),
                                          "Cannot reproduce original exception even without journal modifications. " +
                                          "This is a bug!");
        log.info("{} updates. Dropping oldest updates...", updateJournal.size());
        updateJournal = cutJournalHead(originalException, oldKieSession, updateJournal);
        log.info("{} updates remaining. Removing random operations...", updateJournal.size());
        updateJournal = pruneUpdateJournalOperations(originalException, oldKieSession, updateJournal);
        log.info("{} updates remaining.", updateJournal.size());
        log.info("{} inserts. Pruning inserts...", initialInsertJournal.size());
        initialInsertJournal = pruneInsertJournalOperations(originalException, oldKieSession, initialInsertJournal);
        log.info("{} inserts remaining.", initialInsertJournal.size());
        log.info("{} facts. Pruning facts...", facts.size());
        facts.retainAll(pruneFacts());
        log.info("{} facts remaining.", facts.size());
        // TODO prune setup code
        printTest();
        assertOriginalExceptionReproduced(originalException, test(oldKieSession, initialInsertJournal, updateJournal),
                                          "Cannot reproduce original exception after pruning the journal. " +
                                          "This is a bug!");
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
            log.debug("{} with journal size: {} (took {}s)", outcome, m.getResult().size(), tookSeconds);
            if (!reproduced) {
                m.revert();
            }
        }
        return m.getResult();
    }

    private List<KieSessionOperation> pruneUpdateJournalOperations(RuntimeException ex,
                                                                   KieSession kieSession,
                                                                   List<KieSessionOperation> updateJournal) {
        RemoveRandomBlockMutator<KieSessionOperation> m = new RemoveRandomBlockMutator<KieSessionOperation>(updateJournal);
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            boolean reproduced = reproduce(ex, kieSession, initialInsertJournal, m.mutate());
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<KieSessionOperation> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        return m.getResult();
    }

    private List<KieSessionInsert> pruneInsertJournalOperations(RuntimeException ex,
                                                                KieSession kieSession,
                                                                List<KieSessionInsert> insertJournal) {
        RemoveRandomBlockMutator<KieSessionInsert> m = new RemoveRandomBlockMutator<KieSessionInsert>(insertJournal);
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            boolean reproduced = reproduce(ex, kieSession, m.mutate(), updateJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<KieSessionInsert> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        return m.getResult();
    }

    private ArrayList<Fact> pruneFacts() {
        ArrayList<Fact> minimal = new ArrayList<Fact>();
        for (KieSessionInsert insert : initialInsertJournal) {
            addWithDependencies(insert.getFact(), minimal);
        }
        for (KieSessionOperation op : updateJournal) {
            if (op instanceof KieSessionUpdate) {
                Fact f = ((KieSessionUpdate) op).getValue();
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
            if (ex.getMessage().startsWith("No fact handle for ")) {
                // this is common when removing insert of a fact that is later updated - not interesting
                log.debug("Can't remove insert: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            } else {
                log.info("Unexpected exception", ex);
            }
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

    private static void assertOriginalExceptionReproduced(RuntimeException originalException,
                                                          RuntimeException testException,
                                                          String message) {
        if (testException == null) {
            throw new IllegalStateException(message);
        }
        if (!areEqual(originalException, testException)) {
            throw new IllegalStateException(message +
                    "\nExpected [" + originalException.getClass() + ": " + originalException.getMessage() + "]" +
                    "\nCaused [" + testException.getClass() + ": " + testException.getMessage() + "]", testException);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Test printing
    //------------------------------------------------------------------------------------------------------------------
    //
    private void printTest() {
        printInit();
        printSetup();

        reproducerLog.info(
                "    @Test\n" +
                "    public void test() {");
        for (KieSessionOperation op : updateJournal) {
            op.print(reproducerLog);
        }
        reproducerLog.info(
                "    }\n}");
    }

    private void printInit() {
        reproducerLog.info(
                "package {};\n", domainPackage);
        SortedSet<String> imports = new TreeSet<String>();
        imports.add("org.junit.Before");
        imports.add("org.junit.Test");
        imports.add("org.kie.api.KieServices");
        imports.add("org.kie.api.builder.KieFileSystem");
        imports.add("org.kie.api.builder.model.KieModuleModel");
        imports.add("org.kie.api.io.ResourceType");
        imports.add("org.kie.api.runtime.KieContainer");
        imports.add("org.kie.api.runtime.KieSession");
        for (Fact fact : facts) {
            for (Class<?> cls : fact.getImports()) {
                if (!cls.getPackage().getName().equals(domainPackage)) {
                    imports.add(cls.getCanonicalName());
                }
            }
        }

        for (String cls : imports) {
            reproducerLog.info("import {};", cls);
        }
        reproducerLog.info(
                "\n" +
                "public class DroolsReproducerTest {\n" +
                "\n" +
                "    KieSession kieSession;");
        for (Fact fact : facts) {
            fact.printInitialization(reproducerLog);
        }
        reproducerLog.info("");
    }

    private void printSetup() {
        reproducerLog.info(
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
            fact.printSetup(reproducerLog);
        }
        reproducerLog.info("");
        for (KieSessionOperation insert : initialInsertJournal) {
            insert.print(reproducerLog);
        }
        reproducerLog.info(
                "    }\n");
    }

    //------------------------------------------------------------------------------------------------------------------
    // KIE session operations
    //------------------------------------------------------------------------------------------------------------------
    //
    public void insertInitial(Object fact) {
        if (enabled) {
            initialInsertJournal.add(new KieSessionInsert(operationId++, existingInstances.get(fact)));
        }
    }

    public void insert(Object fact) {
        if (enabled) {
            updateJournal.add(new KieSessionInsert(operationId++, existingInstances.get(fact)));
        }
    }

    public void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        if (enabled) {
            Fact entityFact = existingInstances.get(entity);
            Object value = variableDescriptor.getValue(entity);
            Fact valueFact = value == null ? new NullFact() : existingInstances.get(value);
            updateJournal.add(new KieSessionUpdate(operationId++, entityFact, variableDescriptor, valueFact));
        }
    }

    public void delete(Object entity) {
        if (enabled) {
            updateJournal.add(new KieSessionDelete(operationId++, existingInstances.get(entity)));
        }
    }

    public void fireAllRules() {
        if (enabled) {
            updateJournal.add(new KieSessionFireAllRules(operationId++));
        }
    }

    public void dispose() {
        if (enabled) {
            facts.clear();
            existingInstances.clear();
            initialInsertJournal.clear();
            updateJournal.clear();
            operationId = 0;
        }
    }

}
