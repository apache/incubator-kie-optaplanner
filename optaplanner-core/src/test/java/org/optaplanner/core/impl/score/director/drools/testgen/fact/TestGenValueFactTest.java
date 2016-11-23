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
package org.optaplanner.core.impl.score.director.drools.testgen.fact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.collection.TestdataEntityCollectionPropertyEntity;

import static org.junit.Assert.*;

public class TestGenValueFactTest {

    @Test
    public void initialization() {
        TestdataValue instance = new TestdataValue();
        TestGenValueFact fact = new TestGenValueFact(321, instance);

        assertSame(instance, fact.getInstance());
        assertEquals("testdataValue_321", fact.toString());

        StringBuilder sb = new StringBuilder(100);
        fact.printInitialization(sb);
        assertEquals("    private final TestdataValue testdataValue_321 = new TestdataValue();\n", sb.toString());
    }

    @Test
    public void importsAndDependenciesSimple() {
        HashMap<Object, TestGenFact> instances = new HashMap<>();
        TestdataEntity entity = new TestdataEntity();
        TestGenValueFact f1 = new TestGenValueFact(0, entity);
        TestdataValue value = new TestdataValue();
        TestGenValueFact f2 = new TestGenValueFact(1, value);

        instances.put(entity, f1);

        f1.setUp(instances);
        assertEquals(1, f1.getImports().size());
        assertTrue(f1.getImports().contains(TestdataEntity.class));
        assertEquals(0, f1.getDependencies().size());

        entity.setValue(value);
        instances.put(value, f2);

        f2.setUp(instances);
        assertEquals(1, f2.getImports().size());
        assertTrue(f2.getImports().contains(TestdataValue.class));
        assertTrue(f1.getDependencies().isEmpty());

        f1.setUp(instances);
        assertEquals(1, f1.getImports().size());
        assertTrue(f1.getImports().contains(TestdataEntity.class));
        assertEquals(1, f1.getDependencies().size());
        assertTrue(f1.getDependencies().contains(f2));
    }

    @Test
    public void importsAndDependenciesComplex() {
        TestdataEntityCollectionPropertyEntity a = new TestdataEntityCollectionPropertyEntity("a", null);
        TestdataEntityCollectionPropertyEntity b = new TestdataEntityCollectionPropertyEntity("b", null);
        TestdataEntityCollectionPropertyEntity c = new TestdataEntityCollectionPropertyEntity("c", null);
        TestdataEntityCollectionPropertyEntity d = new TestdataEntityCollectionPropertyEntity("d", null);
        TestdataEntityCollectionPropertyEntity e = new TestdataEntityCollectionPropertyEntity("e", null);
        TestdataEntityCollectionPropertyEntity f = new TestdataEntityCollectionPropertyEntity("f", null);
        TestdataEntityCollectionPropertyEntity g = new TestdataEntityCollectionPropertyEntity("g", null);
        TestdataEntityCollectionPropertyEntity h = new TestdataEntityCollectionPropertyEntity("h", null);
        TestdataEntityCollectionPropertyEntity i = new TestdataEntityCollectionPropertyEntity("i", null);
        a.setEntityList(Arrays.asList(b, c));
        a.setEntitySet(new HashSet<>(Arrays.asList(d, e)));
        a.setStringToEntityMap(new HashMap<>());
        a.getStringToEntityMap().put("f", f);
        a.getStringToEntityMap().put("g", g);
        a.setEntityToStringMap(new HashMap<>());
        a.getEntityToStringMap().put(h, "h");
        a.getEntityToStringMap().put(i, "i");

        TestGenValueFact fa = new TestGenValueFact(0, a);
        TestGenValueFact fb = new TestGenValueFact(1, b);
        TestGenValueFact fc = new TestGenValueFact(2, c);
        TestGenValueFact fd = new TestGenValueFact(3, d);
        TestGenValueFact fe = new TestGenValueFact(4, e);
        TestGenValueFact ff = new TestGenValueFact(5, f);
        TestGenValueFact fg = new TestGenValueFact(6, g);
        TestGenValueFact fh = new TestGenValueFact(7, h);
        TestGenValueFact fi = new TestGenValueFact(8, i);

        HashMap<Object, TestGenFact> instances = new HashMap<>();
        instances.put(a, fa);
        instances.put(b, fb);
        instances.put(c, fc);
        instances.put(d, fd);
        instances.put(e, fe);
        instances.put(f, ff);
        instances.put(g, fg);
        instances.put(h, fh);
        instances.put(i, fi);

        fa.setUp(instances);

        List<TestGenFact> dependencies = fa.getDependencies();
        assertEquals(8, dependencies.size());
        assertTrue(dependencies.contains(fb));
        assertTrue(dependencies.contains(fc));
        assertTrue(dependencies.contains(fd));
        assertTrue(dependencies.contains(fe));
        assertTrue(dependencies.contains(ff));
        assertTrue(dependencies.contains(fg));
        assertTrue(dependencies.contains(fh));
        assertTrue(dependencies.contains(fi));

        List<Class<?>> imports = fa.getImports();
        assertTrue(imports.contains(TestdataEntityCollectionPropertyEntity.class));
        assertTrue(imports.contains(ArrayList.class));
        assertTrue(imports.contains(HashSet.class));
        assertTrue(imports.contains(HashMap.class));
        assertTrue(imports.contains(String.class));
    }
}
