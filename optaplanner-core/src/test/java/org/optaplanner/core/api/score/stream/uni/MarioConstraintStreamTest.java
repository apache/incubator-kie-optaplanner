/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.stream.uni;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScoreHolder;
import org.optaplanner.core.api.score.stream.AbstractConstraintStreamTest;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishSolution;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishValueGroup;

public class MarioConstraintStreamTest extends AbstractConstraintStreamTest {

    private static final String KEY = "MyValueGroup";

    private final TestdataLavishValueGroup valueGroup1 = new TestdataLavishValueGroup(KEY + " 1");
    private final TestdataLavishValueGroup valueGroup2 = new TestdataLavishValueGroup(KEY + " 2");

    public MarioConstraintStreamTest(boolean constraintMatchEnabled, ConstraintStreamImplType constraintStreamImplType) {
        super(constraintMatchEnabled, constraintStreamImplType);
    }

    @Before
    public void skipUnnecessaryTests() {
        Assume.assumeTrue(constraintStreamImplType == ConstraintStreamImplType.DROOLS);
        Assume.assumeTrue(constraintMatchEnabled);
    }

    @Test
    public void usingPlainDRL() {
        String drl = "package " + this.getClass().getPackage().getName() + ";\n" +
                "dialect \"java\"\n" +
                "\n" +
                "import org.optaplanner.core.api.score.buildin.simple.SimpleScoreHolder;\n" +
                "\n" +
                "import " + TestdataLavishValueGroup.class.getCanonicalName() + ";\n" +
                "\n" +
                "global SimpleScoreHolder scoreHolder;\n" +
                "\n" +
                "rule \"" + TEST_CONSTRAINT_NAME + "\"\n" +
                "    when\n" +
                "        TestdataLavishValueGroup(code str[startsWith] \"" + KEY + "\") \n" +
                "    then\n" +
                "        scoreHolder.addConstraintMatch(kcontext, -1);\n" +
                "end\n";
        System.out.println(drl);

        // Inserts all the facts to the KIE session; two facts match the LHS (valueGroup1, valueGroup2)
        final SimpleScoreHolder holder = new SimpleScoreHolder(constraintMatchEnabled);
        KieBase kieBase = loadKnowledgeBaseFromString(drl);
        KieSession kieSession = kieBase.newKieSession();
        kieSession.setGlobal("scoreHolder", holder);
        kieSession.insert(valueGroup1);
        kieSession.insert(valueGroup2);
        kieSession.fireAllRules();

        Assert.assertEquals(-2, holder.getScore());

        // Modifies one fact (valueGroup1 no longer matches the constraint stream).
        valueGroup1.setCode("Other code");
        kieSession.update(kieSession.getFactHandle(valueGroup1), valueGroup1);
        kieSession.fireAllRules();

        // This should pass, with just valueGroup2 matching. Instead, there are two.
        Assert.assertEquals(-1, holder.getScore());
    }

    @Test
    public void usingConstraintStreams() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();
        solution.getValueGroupList().add(valueGroup1);
        solution.getValueGroupList().add(valueGroup2);

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector((factory) -> {
            return factory.from(TestdataLavishValueGroup.class)
                    .filter(valueGroup -> valueGroup.getCode().startsWith("MyValueGroup"))
                    .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE);
        });

        // Inserts all the facts to the KIE session; two facts match the constraint stream (valueGroup1, valueGroup2)
        scoreDirector.setWorkingSolution(solution);

        // Assertion passes, both valueGroup1 and valueGroup2 match.
        assertScore(scoreDirector,
                assertMatch(valueGroup1),
                assertMatch(valueGroup2));

        // Modifies one fact (valueGroup1 no longer matches the constraint stream).
        scoreDirector.beforeProblemPropertyChanged(valueGroup1);
        valueGroup1.setCode("Other code");
        scoreDirector.afterProblemPropertyChanged(valueGroup1);

        // This should pass, with just valueGroup2 matching. Instead, there are two.
        assertScore(scoreDirector,
                assertMatch(valueGroup2));
    }

    protected KieBase loadKnowledgeBaseFromString(String drlContentString) {
        KieBaseConfiguration kBaseConfig = null;
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(drlContentString.getBytes()), ResourceType.DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
        if (kBaseConfig == null) {
            kBaseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        }
        InternalKnowledgeBase kbase = kBaseConfig == null ? KnowledgeBaseFactory.newKnowledgeBase() : KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
        kbase.addPackages( kbuilder.getKnowledgePackages());
        return kbase;
    }
}
