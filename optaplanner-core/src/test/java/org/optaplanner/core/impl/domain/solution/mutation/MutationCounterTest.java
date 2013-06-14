/*
 * Copyright 2013 JBoss Inc
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

package org.optaplanner.core.impl.domain.solution.mutation;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.optaplanner.core.impl.domain.solution.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

import static org.junit.Assert.*;

public class MutationCounterTest {

    @Test
    public void countMutationsNone() {
        SolutionDescriptor solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        MutationCounter mutationCounter = new MutationCounter(solutionDescriptor);

        TestdataValue val1 = new TestdataValue("1");
        TestdataValue val2 = new TestdataValue("2");
        TestdataValue val3 = new TestdataValue("3");
        List<TestdataValue> valueList = Arrays.asList(val1, val2, val3);

        TestdataEntity a_a = new TestdataEntity("a", val1);
        TestdataEntity a_b = new TestdataEntity("b", val1);
        TestdataEntity a_c = new TestdataEntity("c", val3);
        TestdataEntity a_d = new TestdataEntity("d", val3);
        List<TestdataEntity> aEntityList = Arrays.asList(a_a, a_b, a_c, a_d);

        TestdataSolution a = new TestdataSolution("solution");
        a.setValueList(valueList);
        a.setEntityList(aEntityList);

        TestdataEntity b_a = new TestdataEntity("a", val1);
        TestdataEntity b_b = new TestdataEntity("b", val1);
        TestdataEntity b_c = new TestdataEntity("c", val3);
        TestdataEntity b_d = new TestdataEntity("d", val3);
        List<TestdataEntity> bEntityList = Arrays.asList(b_a, b_b, b_c, b_d);

        TestdataSolution b = new TestdataSolution("solution");
        b.setValueList(valueList);
        b.setEntityList(bEntityList);

        assertEquals(0, mutationCounter.countMutations(a, b));
    }

    @Test
    public void countMutationsSome() {
        SolutionDescriptor solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        MutationCounter mutationCounter = new MutationCounter(solutionDescriptor);

        TestdataValue val1 = new TestdataValue("1");
        TestdataValue val2 = new TestdataValue("2");
        TestdataValue val3 = new TestdataValue("3");
        List<TestdataValue> valueList = Arrays.asList(val1, val2, val3);

        TestdataEntity a_a = new TestdataEntity("a", val1);
        TestdataEntity a_b = new TestdataEntity("b", val1);
        TestdataEntity a_c = new TestdataEntity("c", val3);
        TestdataEntity a_d = new TestdataEntity("d", val3);
        List<TestdataEntity> aEntityList = Arrays.asList(a_a, a_b, a_c, a_d);

        TestdataSolution a = new TestdataSolution("solution");
        a.setValueList(valueList);
        a.setEntityList(aEntityList);

        TestdataEntity b_a = new TestdataEntity("a", val3); // Mutated
        TestdataEntity b_b = new TestdataEntity("b", val1);
        TestdataEntity b_c = new TestdataEntity("c", val3);
        TestdataEntity b_d = new TestdataEntity("d", val2); // Mutated
        List<TestdataEntity> bEntityList = Arrays.asList(b_a, b_b, b_c, b_d);

        TestdataSolution b = new TestdataSolution("solution");
        b.setValueList(valueList);
        b.setEntityList(bEntityList);

        assertEquals(2, mutationCounter.countMutations(a, b));
    }

    @Test
    public void countMutationsAll() {
        SolutionDescriptor solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        MutationCounter mutationCounter = new MutationCounter(solutionDescriptor);

        TestdataValue val1 = new TestdataValue("1");
        TestdataValue val2 = new TestdataValue("2");
        TestdataValue val3 = new TestdataValue("3");
        List<TestdataValue> valueList = Arrays.asList(val1, val2, val3);

        TestdataEntity a_a = new TestdataEntity("a", val1);
        TestdataEntity a_b = new TestdataEntity("b", val1);
        TestdataEntity a_c = new TestdataEntity("c", val3);
        TestdataEntity a_d = new TestdataEntity("d", val3);
        List<TestdataEntity> aEntityList = Arrays.asList(a_a, a_b, a_c, a_d);

        TestdataSolution a = new TestdataSolution("solution");
        a.setValueList(valueList);
        a.setEntityList(aEntityList);

        TestdataEntity b_a = new TestdataEntity("a", val2); // Mutated
        TestdataEntity b_b = new TestdataEntity("b", val2); // Mutated
        TestdataEntity b_c = new TestdataEntity("c", val2); // Mutated
        TestdataEntity b_d = new TestdataEntity("d", val2); // Mutated
        List<TestdataEntity> bEntityList = Arrays.asList(b_a, b_b, b_c, b_d);

        TestdataSolution b = new TestdataSolution("solution");
        b.setValueList(valueList);
        b.setEntityList(bEntityList);

        assertEquals(4, mutationCounter.countMutations(a, b));
    }

}
