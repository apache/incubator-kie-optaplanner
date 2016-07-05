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
    private final KieSession oldKieSession;
    private final RuntimeException originalException;
    private KieSessionJournal journal;

    public static KieSessionJournal newJournal() {
        if (reproducerLog.isInfoEnabled()) {
            return new KieSessionJournalImpl();
        } else {
            return new NoopJournal();
        }
    }

    public static void replay(KieSessionJournal journal, KieSession kieSession, RuntimeException originalException) {
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
        throw new DroolsReproducer(kieSession, originalException, journal).doReplay();
    }

    private RuntimeException doReplay() {
        log.info("Starting replay & reduce to find a minimal Drools reproducer for:", originalException);
        assertOriginalExceptionReproduced("Cannot reproduce original exception even without journal modifications. " +
                "This is a bug!");
        log.info("The KIE session journal has {} facts, {} inserts and {} updates.",
                 journal.getFacts().size(), journal.getInitialInserts().size(), journal.getMoveOperations().size());
        dropOldestUpdates();
        pruneUpdates();
        pruneInserts();
        pruneFacts();
        // TODO prune setup code
        printReproducer();
        RuntimeException reproducedException = journal.replay(oldKieSession);
        assertOriginalExceptionReproduced("Cannot reproduce original exception after pruning the journal. " +
                "This is a bug!");
        return reproducedException;
    }

    public DroolsReproducer(KieSession oldKieSession, RuntimeException originalException, KieSessionJournal journal) {
        this.oldKieSession = oldKieSession;
        this.originalException = originalException;
        this.journal = journal;
    }

    private void dropOldestUpdates() {
        log.info("Dropping oldest updates...", journal.getMoveOperations().size());
        HeadCuttingMutator m = new HeadCuttingMutator(journal.getMoveOperations());
        while (m.canMutate()) {
            long start = System.currentTimeMillis();
            KieSessionJournalImpl testJournal = new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(testJournal);
            double tookSeconds = (System.currentTimeMillis() - start) / 1000d;
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            log.debug("{} with journal size: {} (took {}s)", outcome, m.getResult().size(), tookSeconds);
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.getResult());
        log.info("{} updates remaining.", journal.getMoveOperations().size());
    }

    private void pruneUpdates() {
        log.info("Pruning updates...", journal.getMoveOperations().size());
        RemoveRandomBlockMutator<KieSessionOperation> m = new RemoveRandomBlockMutator<KieSessionOperation>(journal.getMoveOperations());
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            KieSessionJournalImpl testJournal = new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<KieSessionOperation> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new KieSessionJournalImpl(journal.getFacts(), journal.getInitialInserts(), m.getResult());
        log.info("{} updates remaining.", journal.getMoveOperations().size());
    }

    private void pruneInserts() {
        log.info("Pruning inserts...", journal.getInitialInserts().size());
        RemoveRandomBlockMutator<KieSessionInsert> m = new RemoveRandomBlockMutator<KieSessionInsert>(journal.getInitialInserts());
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            KieSessionJournalImpl testJournal = new KieSessionJournalImpl(journal.getFacts(), m.mutate(), journal.getMoveOperations());
            boolean reproduced = reproduce(testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<KieSessionInsert> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new KieSessionJournalImpl(journal.getFacts(), m.getResult(), journal.getMoveOperations());
        log.info("{} inserts remaining.", journal.getInitialInserts().size());
    }

    private void pruneFacts() {
        log.info("Pruning {} facts...", journal.getFacts().size());
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
        journal = new KieSessionJournalImpl(minimal, journal.getInitialInserts(), journal.getMoveOperations());
        log.info("{} facts remaining.", journal.getFacts().size());
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

    private boolean reproduce(KieSessionJournal journal) {
        RuntimeException reproducedEx = journal.replay(oldKieSession);
        if (reproducedEx == null) {
            return false;
        } else if (areEqual(originalException, reproducedEx)) {
            return true;
        } else {
            if (reproducedEx.getMessage().startsWith("No fact handle for ")) {
                // this is common when removing insert of a fact that is later updated - not interesting
                log.debug("Can't remove insert: {}: {}", reproducedEx.getClass().getSimpleName(), reproducedEx.getMessage());
            } else {
                log.info("Unexpected exception", reproducedEx);
            }
            return false;
        }
    }

    private static boolean areEqual(RuntimeException originalException, RuntimeException reproducedException) {
        if (!originalException.getClass().equals(reproducedException.getClass())) {
            return false;
        }
        if (!Objects.equals(originalException.getMessage(), reproducedException.getMessage())) {
            return false;
        }
        if (reproducedException.getStackTrace().length == 0) {
            throw new IllegalStateException("Caught exception with empty stack trace => can't compare to the original." +
                    " Use '-XX:-OmitStackTraceInFastThrow' to turn off this optimization.", reproducedException);
        }
        // TODO check all org.drools elements?
        return originalException.getStackTrace()[0].equals(reproducedException.getStackTrace()[0]);
    }

    private void assertOriginalExceptionReproduced(String message) {
        RuntimeException reproducedException = journal.replay(oldKieSession);
        if (reproducedException == null) {
            throw new IllegalStateException(message + " No exception thrown.");
        }
        if (!areEqual(originalException, reproducedException)) {
            throw new IllegalStateException(message +
                    "\nExpected [" + originalException.getClass() + ": " + originalException.getMessage() + "]" +
                    "\nCaused [" + reproducedException.getClass() + ": " + reproducedException.getMessage() + "]", reproducedException);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Test printing
    //------------------------------------------------------------------------------------------------------------------
    //
    private void printReproducer() {
        printInit();
        printSetup();
        printTest();
    }

    private void printInit() {
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
        for (Fact fact : journal.getFacts()) {
            fact.printSetup(reproducerLog);
        }
        reproducerLog.info("");
        for (KieSessionOperation insert : journal.getInitialInserts()) {
            insert.print(reproducerLog);
        }
        reproducerLog.info("    }\n");
    }

    private void printTest() {
        reproducerLog.info(
                "    @Test\n" +
                "    public void test() {");
        for (KieSessionOperation op : journal.getMoveOperations()) {
            op.print(reproducerLog);
        }
        reproducerLog.info("    }\n}");
    }

}
