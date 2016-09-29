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
package org.optaplanner.core.impl.score.director.drools.testgen.mutation;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGenHeadCuttingMutatorTest {

    private ArrayList<Integer> list = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = 0; i < 25; i++) {
            list.add(i);
        }
    }

    @Test
    public void mutateUntilListIsEmpty() {
        TestGenHeadCuttingMutator<Integer> m = new TestGenHeadCuttingMutator<Integer>(list);
        assertTrue(m.canMutate());

        // 0.8 * 25 = 20 cut
        assertThat(m.mutate().size()).isEqualTo(5);
        assertTrue(m.canMutate());
        m.revert();
        assertThat(m.getResult().size()).isEqualTo(25);

        assertTrue(m.canMutate());
        // 0.4 * 25 = 10 cut
        assertThat(m.mutate().size()).isEqualTo(15);
        assertTrue(m.canMutate());
        // 10 + 0.4 * 15 = 16 cut
        assertThat(m.mutate().size()).isEqualTo(9);
        assertTrue(m.canMutate());
        // 16 + 0.4 * 9 = 19 cut
        assertThat(m.mutate().size()).isEqualTo(6);
        assertTrue(m.canMutate());
        m.revert();
        assertThat(m.getResult().size()).isEqualTo(9);

        assertTrue(m.canMutate());
        // 16 + 0.2 * 9 = 17 cut
        assertThat(m.mutate().size()).isEqualTo(8);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(7);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(6);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(5);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(4);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(3);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(2);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(1);
        assertTrue(m.canMutate());
        assertThat(m.mutate().size()).isEqualTo(0);
        assertFalse(m.canMutate());
        assertThat(m.getResult().size()).isEqualTo(0);
    }

    @Test
    public void testImpossibleMutation() {
        TestGenHeadCuttingMutator<Integer> m = new TestGenHeadCuttingMutator<Integer>(list);
        assertTrue(m.canMutate());

        assertTrue(m.canMutate());
        // 0.8 * 25 = 20 cut
        assertThat(m.mutate().size()).isEqualTo(5);
        m.revert();

        assertTrue(m.canMutate());
        // 0.4 * 25 = 10 cut
        assertThat(m.mutate().size()).isEqualTo(15);
        m.revert();

        assertTrue(m.canMutate());
        // 0.2 * 25 = 5 cut
        assertThat(m.mutate().size()).isEqualTo(20);
        m.revert();

        assertTrue(m.canMutate());
        // 0.1 * 25 = 2 cut
        assertThat(m.mutate().size()).isEqualTo(23);
        m.revert();

        assertTrue(m.canMutate());
        // 0.05 * 25 = 1 cut
        assertThat(m.mutate().size()).isEqualTo(24);
        m.revert();

        // impossible to mutate to full list
        assertFalse(m.canMutate());
        assertThat(m.getResult().size()).isEqualTo(25);
    }
}
