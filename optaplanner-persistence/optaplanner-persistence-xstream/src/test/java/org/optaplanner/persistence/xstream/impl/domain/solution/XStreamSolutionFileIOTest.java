/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.persistence.xstream.impl.domain.solution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfIterator;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCode;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.persistence.xstream.impl.testdata.domain.XStreamTestdataEntity;
import org.optaplanner.persistence.xstream.impl.testdata.domain.XStreamTestdataSolution;
import org.optaplanner.persistence.xstream.impl.testdata.domain.XStreamTestdataValue;

class XStreamSolutionFileIOTest {

    private static File solutionTestDir;

    @BeforeAll
    static void setup() {
        solutionTestDir = new File("target/solutionTest/");
        solutionTestDir.mkdirs();
    }

    @Test
    void readAndWrite() {
        XStreamSolutionFileIO<XStreamTestdataSolution> solutionFileIO = new XStreamSolutionFileIO<>(
                XStreamTestdataSolution.class);
        File file = new File(solutionTestDir, "testdataSolution.xml");

        XStreamTestdataSolution original = new XStreamTestdataSolution("s1");
        XStreamTestdataValue originalV1 = new XStreamTestdataValue("v1");
        original.setValueList(asList(originalV1, new XStreamTestdataValue("v2")));
        original.setEntityList(
                asList(new XStreamTestdataEntity("e1"), new XStreamTestdataEntity("e2", originalV1),
                        new XStreamTestdataEntity("e3")));
        original.setScore(SimpleScore.of(-123));
        solutionFileIO.write(original, file);
        XStreamTestdataSolution copy = solutionFileIO.read(file);

        assertThat(copy).isNotSameAs(original);
        assertCode("s1", copy);
        assertAllCodesOfIterator(copy.getValueList().iterator(), "v1", "v2");
        assertAllCodesOfIterator(copy.getEntityList().iterator(), "e1", "e2", "e3");
        XStreamTestdataValue copyV1 = copy.getValueList().get(0);
        XStreamTestdataEntity copyE2 = copy.getEntityList().get(1);
        assertCode("v1", copyE2.getValue());
        assertThat(copyE2.getValue()).isSameAs(copyV1);
        assertThat(copy.getScore()).isEqualTo(SimpleScore.of(-123));
    }

    private static <X> List<X> asList(X... values) {
        // XStream does illegal access to JDK internals if Arrays.asList(...) is used instead.
        return Arrays.stream(values)
                .collect(Collectors.toList());
    }

}
