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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.impl.score.director.drools.testgen.fact.TestGenFact;
import org.optaplanner.core.impl.score.director.drools.testgen.mutation.HeadCuttingMutator;
import org.optaplanner.core.impl.score.director.drools.testgen.mutation.RemoveRandomBlockMutator;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionUpdate;
import org.optaplanner.core.impl.score.director.drools.testgen.reproducer.OriginalProblemReproducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TestGenerator {

    private static final Logger log = LoggerFactory.getLogger(TestGenerator.class);
    private final OriginalProblemReproducer reproducer;
    private TestGenKieSessionJournal journal;

    static void createTest(TestGenKieSessionJournal journal, OriginalProblemReproducer reproducer) {
        new TestGenerator(journal, reproducer).run();
    }

    private TestGenerator(TestGenKieSessionJournal journal, OriginalProblemReproducer reproducer) {
        this.journal = journal;
        this.reproducer = reproducer;
    }

    private void run() {
        log.info("Creating a minimal test that reproduces following Drools problem: {}", reproducer);
        log.info("The KIE session journal has {} facts, {} inserts and {} updates.",
                journal.getFacts().size(), journal.getInitialInserts().size(), journal.getMoveOperations().size());
        log.info("Trying to reproduce with the complete KIE session journal...");
        assertOriginalExceptionReproduced("Cannot reproduce the original problem even without journal modifications. "
                + "This is a bug!");
        log.info("Reproduced.");
        dropOldestUpdates();
        pruneUpdates();
        pruneInserts();
        pruneFacts();
        // TODO prune setup code
        printReproducer();
        assertOriginalExceptionReproduced("Cannot reproduce the original problem after pruning the journal. "
                + "This is a bug!");
    }

    private void dropOldestUpdates() {
        log.info("Dropping oldest updates...", journal.getMoveOperations().size());
        HeadCuttingMutator<TestGenKieSessionOperation> m = new HeadCuttingMutator<TestGenKieSessionOperation>(journal.getMoveOperations());
        while (m.canMutate()) {
            long start = System.currentTimeMillis();
            TestGenKieSessionJournal testJournal = new TestGenKieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(testJournal);
            double tookSeconds = (System.currentTimeMillis() - start) / 1000d;
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            log.debug("    {} with journal size: {} (took {}s)", outcome, m.getResult().size(), tookSeconds);
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new TestGenKieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.getResult());
        log.info("{} updates remaining.", journal.getMoveOperations().size());
    }

    private void pruneUpdates() {
        log.info("Pruning updates...", journal.getMoveOperations().size());
        RemoveRandomBlockMutator<TestGenKieSessionOperation> m = new RemoveRandomBlockMutator<TestGenKieSessionOperation>(journal.getMoveOperations());
        while (m.canMutate()) {
            log.debug("    Current journal size: {}", m.getResult().size());
            TestGenKieSessionJournal testJournal = new TestGenKieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.mutate());
            boolean reproduced = reproduce(testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<TestGenKieSessionOperation> block = m.getRemovedBlock();
            log.debug("    {} without block of {} [{} - {}]",
                    outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new TestGenKieSessionJournal(journal.getFacts(), journal.getInitialInserts(), m.getResult());
        log.info("{} updates remaining.", journal.getMoveOperations().size());
    }

    private void pruneInserts() {
        log.info("Pruning inserts...", journal.getInitialInserts().size());
        RemoveRandomBlockMutator<TestGenKieSessionInsert> m = new RemoveRandomBlockMutator<TestGenKieSessionInsert>(journal.getInitialInserts());
        while (m.canMutate()) {
            log.debug("    Current journal size: {}", m.getResult().size());
            TestGenKieSessionJournal testJournal = new TestGenKieSessionJournal(journal.getFacts(), m.mutate(), journal.getMoveOperations());
            boolean reproduced = reproduce(testJournal);
            String outcome = reproduced ? "Reproduced" : "Can't reproduce";
            List<TestGenKieSessionInsert> block = m.getRemovedBlock();
            log.debug("    {} without block of {} [{} - {}]",
                    outcome, block.size(), block.get(0), block.get(block.size() - 1));
            if (!reproduced) {
                m.revert();
            }
        }
        journal = new TestGenKieSessionJournal(journal.getFacts(), m.getResult(), journal.getMoveOperations());
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
        journal = new TestGenKieSessionJournal(minimal, journal.getInitialInserts(), journal.getMoveOperations());
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

    private boolean reproduce(TestGenKieSessionJournal testJournal) {
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
        StringBuilder sb = new StringBuilder(1 << 15); // 2^15 initial capacity
        printInit(sb);
        printSetup(sb);
        printTest(sb);
        writeTestFile(sb);
    }

    private void printInit(StringBuilder sb) {
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

        sb.append(String.format("package %s;%n%n", domainPackage));
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
            sb.append(String.format("import %s;%n", cls));
        }
        sb.append(System.lineSeparator())
                .append("public class DroolsReproducerTest {").append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("    KieSession kieSession;").append(System.lineSeparator());
        for (TestGenFact fact : journal.getFacts()) {
            fact.printInitialization(sb);
        }
        sb.append(System.lineSeparator());
    }

    private void printSetup(StringBuilder sb) {
        sb
                .append("    @Before").append(System.lineSeparator())
                .append("    public void setUp() {").append(System.lineSeparator())
                .append("        KieServices kieServices = KieServices.Factory.get();").append(System.lineSeparator())
                .append("        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();").append(System.lineSeparator())
                .append("        KieFileSystem kfs = kieServices.newKieFileSystem();").append(System.lineSeparator())
                .append("        kfs.writeKModuleXML(kieModuleModel.toXML());").append(System.lineSeparator())
                // TODO don't hard-code score DRL
                .append("        kfs.write(kieServices.getResources()").append(System.lineSeparator())
                .append("                .newClassPathResource(\"org/optaplanner/examples/nurserostering/solver/nurseRosteringScoreRules.drl\")").append(System.lineSeparator())
                .append("                .setResourceType(ResourceType.DRL));").append(System.lineSeparator())
                .append("        kieServices.newKieBuilder(kfs).buildAll();").append(System.lineSeparator())
                .append("        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());").append(System.lineSeparator())
                .append("        kieSession = kieContainer.newKieSession();").append(System.lineSeparator())
                .append(System.lineSeparator());
        for (TestGenFact fact : journal.getFacts()) {
            fact.printSetup(sb);
        }
        sb.append(System.lineSeparator());
        for (TestGenKieSessionOperation insert : journal.getInitialInserts()) {
            insert.print(sb);
        }
        sb.append("    }")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
    }

    private void printTest(StringBuilder sb) {
        sb
                .append("    @Test").append(System.lineSeparator())
                .append("    public void test() {").append(System.lineSeparator());
        for (TestGenKieSessionOperation op : journal.getMoveOperations()) {
            op.print(sb);
        }
        sb
                .append("    }").append(System.lineSeparator())
                .append("}").append(System.lineSeparator());
    }

    private void writeTestFile(StringBuilder sb) {
        FileOutputStream fos;
        File file = new File("DroolsReproducerTest.java");
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            log.error("Cannot open test file: " + file.toString(), ex);
            return;
        }
        OutputStreamWriter out;
        try {
            out = new OutputStreamWriter(fos, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error("Can't open", ex);
            return;
        }
        try {
            out.append(sb);
        } catch (IOException ex) {
            log.error("Can't write", ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                log.error("Can't close", ex);
            }
        }
    }

}
