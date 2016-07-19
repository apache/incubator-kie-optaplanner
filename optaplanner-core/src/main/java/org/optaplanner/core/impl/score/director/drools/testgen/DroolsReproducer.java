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
package org.optaplanner.core.impl.score.director.drools.testgen;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.impl.score.director.drools.testgen.fact.TestGenFact;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO rename to TestGenerator
final class DroolsReproducer {

    private static final Logger log = LoggerFactory.getLogger(DroolsReproducer.class);
    private static final Logger reproducerLog = LoggerFactory.getLogger("org.optaplanner.drools.reproducer");
    private final OriginalProblemReproducer reproducer;
    private KieSessionJournal journal;

    static void replay(KieSessionJournal journal, OriginalProblemReproducer reproducer) {
        new DroolsReproducer(journal, reproducer).doReplay();
    }

    private DroolsReproducer(KieSessionJournal journal, OriginalProblemReproducer reproducer) {
        this.journal = journal;
        this.reproducer = reproducer;
    }

    private void doReplay() {
        log.info("Starting replay & reduce to find a minimal Drools reproducer for: {}", reproducer);
        assertOriginalExceptionReproduced("Cannot reproduce the original problem even without journal modifications. " +
                "This is a bug!");
        log.info("The KIE session journal has {} facts, {} inserts and {} updates.",
                 journal.getFacts().size(), journal.getInitialInserts().size(), journal.getMoveOperations().size());
        dropOldestUpdates();
        pruneUpdates();
        pruneInserts();
        pruneFacts();
        // TODO prune setup code
        printReproducer();
        assertOriginalExceptionReproduced("Cannot reproduce the original problem after pruning the journal. " +
                "This is a bug!");
    }

    private void dropOldestUpdates() {
        log.info("Dropping oldest updates...", journal.getMoveOperations().size());
        HeadCuttingMutator m = new HeadCuttingMutator(journal.getMoveOperations());
        while (m.canMutate()) {
            long start = System.currentTimeMillis();
            KieSessionJournal testJournal = new KieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(testJournal);
            double tookSeconds = (System.currentTimeMillis() - start) / 1000d;
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            log.debug("{} with journal size: {} (took {}s)", outcome, m.getResult().size(), tookSeconds);
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new KieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.getResult());
        log.info("{} updates remaining.", journal.getMoveOperations().size());
    }

    private void pruneUpdates() {
        log.info("Pruning updates...", journal.getMoveOperations().size());
        RemoveRandomBlockMutator<TestGenKieSessionOperation> m = new RemoveRandomBlockMutator<TestGenKieSessionOperation>(journal.getMoveOperations());
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            KieSessionJournal testJournal = new KieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<TestGenKieSessionOperation> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new KieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.getResult());
        log.info("{} updates remaining.", journal.getMoveOperations().size());
    }

    private void pruneInserts() {
        log.info("Pruning inserts...", journal.getInitialInserts().size());
        RemoveRandomBlockMutator<TestGenKieSessionInsert> m = new RemoveRandomBlockMutator<TestGenKieSessionInsert>(journal.getInitialInserts());
        while (m.canMutate()) {
            log.debug("Current journal size: {}", m.getResult().size());
            KieSessionJournal testJournal = new KieSessionJournal(journal.getFacts(), m.mutate(), journal.getMoveOperations());
            boolean reproduced = reproduce(testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<TestGenKieSessionInsert> block = m.getRemovedBlock();
            log.debug("{} without block of {} [{} - {}]",
                      outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new KieSessionJournal(journal.getFacts(), m.getResult(), journal.getMoveOperations());
        log.info("{} inserts remaining.", journal.getInitialInserts().size());
    }

    private void pruneFacts() {
        log.info("Pruning {} facts...", journal.getFacts().size());
        ArrayList<TestGenFact> minimal = new ArrayList<TestGenFact>();
        for (TestGenKieSessionInsert insert : journal.getInitialInserts()) {
            addWithDependencies(insert.getFact(), minimal);
        }
        for (TestGenKieSessionOperation op : journal.getMoveOperations()) {
            if (op instanceof TestGenKieSessionUpdate) {
                TestGenFact f = ((TestGenKieSessionUpdate) op).getValue();
                if (f != null) {
                    addWithDependencies(f, minimal);
                }
            }
        }
        journal = new KieSessionJournal(minimal, journal.getInitialInserts(), journal.getMoveOperations());
        log.info("{} facts remaining.", journal.getFacts().size());
    }

    private static void addWithDependencies(TestGenFact f, List<TestGenFact> factList) {
        if (factList.contains(f)) {
            return;
        }
        factList.add(f);
        for (TestGenFact dependency : f.getDependencies()) {
            addWithDependencies(dependency, factList);
        }
    }

    private boolean reproduce(KieSessionJournal testJournal) {
        return reproducer.isReproducible(testJournal);
    }

    private void assertOriginalExceptionReproduced(String message) {
        reproducer.assertReproducible(journal, message);
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
        for (TestGenFact fact : journal.getFacts()) {
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
        for (TestGenFact fact : journal.getFacts()) {
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
        for (TestGenFact fact : journal.getFacts()) {
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
        for (TestGenFact fact : journal.getFacts()) {
            fact.printSetup(reproducerLog);
        }
        reproducerLog.info("");
        for (TestGenKieSessionOperation insert : journal.getInitialInserts()) {
            insert.print(reproducerLog);
        }
        reproducerLog.info("    }\n");
    }

    private void printTest() {
        reproducerLog.info(
                "    @Test\n" +
                "    public void test() {");
        for (TestGenKieSessionOperation op : journal.getMoveOperations()) {
            op.print(reproducerLog);
        }
        reproducerLog.info("    }\n}");
    }

}
