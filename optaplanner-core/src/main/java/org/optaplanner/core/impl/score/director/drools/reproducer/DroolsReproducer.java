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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.Fact;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DroolsReproducer {

    private static final Logger log = LoggerFactory.getLogger(DroolsReproducer.class);
    private static final Logger reproducerLog = LoggerFactory.getLogger("org.optaplanner.drools.reproducer");

    public static KieSessionJournal newJournal() {
        if (reproducerLog.isInfoEnabled()) {
            return new KieSessionJournalImpl();
        } else {
            return new NoopJournal();
        }
    }

    public static void replay(KieSessionJournal journal, KieSession oldKieSession, RuntimeException originalException) {
        if (!reproducerLog.isInfoEnabled()) {
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
        assertOriginalExceptionReproduced(originalException, journal.replay(oldKieSession),
                                          "Cannot reproduce original exception even without journal modifications. " +
                                          "This is a bug!");
        log.info("{} updates. Dropping oldest updates...", journal.getMoveOperations().size());
        KieSessionJournalImpl prunedJournal = cutJournalHead(originalException, oldKieSession, journal);
        log.info("{} updates remaining. Removing random operations...", prunedJournal.getMoveOperations().size());
        prunedJournal = pruneUpdateJournalOperations(originalException, oldKieSession, prunedJournal);
        log.info("{} updates remaining.", prunedJournal.getMoveOperations().size());
        log.info("{} inserts. Pruning inserts...", prunedJournal.getInitialInserts().size());
        prunedJournal = pruneInsertJournalOperations(originalException, oldKieSession, prunedJournal);
        log.info("{} inserts remaining.", prunedJournal.getInitialInserts().size());
        log.info("{} facts. Pruning facts...", prunedJournal.getFacts().size());
        prunedJournal = pruneFacts(prunedJournal);
        log.info("{} facts remaining.", prunedJournal.getFacts().size());
        // TODO prune setup code
        printReproducer(prunedJournal);
        RuntimeException reproducedException = prunedJournal.replay(oldKieSession);
        assertOriginalExceptionReproduced(originalException, reproducedException,
                                          "Cannot reproduce original exception after pruning the journal. " +
                                          "This is a bug!");
        throw reproducedException;
    }

    private static KieSessionJournalImpl cutJournalHead(RuntimeException ex,
                                                        KieSession kieSession,
                                                        KieSessionJournal journal) {
        HeadCuttingMutator m = new HeadCuttingMutator(journal.getMoveOperations());
        while (m.canMutate()) {
            long start = System.currentTimeMillis();
            KieSessionJournalImpl testJournal = new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(ex, kieSession, testJournal);
            double tookSeconds = (System.currentTimeMillis() - start) / 1000d;
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            log.debug("{} with journal size: {} (took {}s)", outcome, m.getResult().size(), tookSeconds);
            if (!reproduced) {
                m.revert();
            }
        }
        return new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.getResult());
    }

    private static KieSessionJournalImpl pruneUpdateJournalOperations(RuntimeException ex,
                                                                      KieSession kieSession,
                                                                      KieSessionJournal journal) {
        RemoveRandomBlockMutator<KieSessionOperation> m = new RemoveRandomBlockMutator<KieSessionOperation>(journal.getMoveOperations());
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            KieSessionJournalImpl testJournal = new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(ex, kieSession, testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<KieSessionOperation> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        return new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.getResult());
    }

    private static KieSessionJournalImpl pruneInsertJournalOperations(RuntimeException originalException,
                                                                      KieSession kieSession,
                                                                      KieSessionJournal journal) {
        RemoveRandomBlockMutator<KieSessionInsert> m = new RemoveRandomBlockMutator<KieSessionInsert>(journal.getInitialInserts());
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            KieSessionJournalImpl testJournal = new KieSessionJournalImpl(journal.getFacts(), m.mutate(), journal.getMoveOperations());
            boolean reproduced = reproduce(originalException, kieSession, testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<KieSessionInsert> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        return new KieSessionJournalImpl(journal.getFacts(), m.getResult(), journal.getMoveOperations());
    }

    private static KieSessionJournalImpl pruneFacts(KieSessionJournal journal) {
        ArrayList<Fact> minimal = new ArrayList<Fact>();
        for (KieSessionInsert insert : journal.getInitialInserts()) {
            addWithDependencies(insert.getFact(), minimal);
        }
        for (KieSessionOperation op : journal.getMoveOperations()) {
            if (op instanceof KieSessionUpdate) {
                Fact f = ((KieSessionUpdate) op).getValue();
                if (f != null) {
                    addWithDependencies(f, minimal);
                }
            }
        }
        return new KieSessionJournalImpl(minimal, journal.getInitialInserts(), journal.getMoveOperations());
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

    private static boolean reproduce(RuntimeException originalException,
                                     KieSession oldKieSession,
                                     KieSessionJournal journal) {
        RuntimeException ex = journal.replay(oldKieSession);
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
    private static void printReproducer(KieSessionJournal journal) {
        printInit(journal);
        printSetup(journal);
        printTest(journal);
    }

    private static void printInit(KieSessionJournal journal) {
        String domainPackage = null;
        for (Fact fact : journal.getFacts()) {
            Class<? extends Object> factClass = fact.getInstance().getClass();
            for (Annotation ann : factClass.getAnnotations()) {
                if (PlanningEntity.class.equals(ann.annotationType())) {
                    domainPackage = factClass.getPackage().getName();
                }
                break;
            }
            if (domainPackage != null) {
                break;
            }
        }

        if (domainPackage == null) {
            throw new IllegalStateException("Cannot determine planning domain package.");
        }

        reproducerLog.info("package {};\n", domainPackage);
        SortedSet<String> imports = new TreeSet<String>();
        imports.add("org.junit.Before");
        imports.add("org.junit.Test");
        imports.add("org.kie.api.KieServices");
        imports.add("org.kie.api.builder.KieFileSystem");
        imports.add("org.kie.api.builder.model.KieModuleModel");
        imports.add("org.kie.api.io.ResourceType");
        imports.add("org.kie.api.runtime.KieContainer");
        imports.add("org.kie.api.runtime.KieSession");
        for (Fact fact : journal.getFacts()) {
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
        for (Fact fact : journal.getFacts()) {
            fact.printInitialization(reproducerLog);
        }
        reproducerLog.info("");
    }

    private static void printSetup(KieSessionJournal journal) {
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
        for (Fact fact : journal.getFacts()) {
            fact.printSetup(reproducerLog);
        }
        reproducerLog.info("");
        for (KieSessionOperation insert : journal.getInitialInserts()) {
            insert.print(reproducerLog);
        }
        reproducerLog.info("    }\n");
    }

    private static void printTest(KieSessionJournal journal) {
        reproducerLog.info(
                "    @Test\n" +
                "    public void test() {");
        for (KieSessionOperation op : journal.getMoveOperations()) {
            op.print(reproducerLog);
        }
        reproducerLog.info("    }\n}");
    }

}
