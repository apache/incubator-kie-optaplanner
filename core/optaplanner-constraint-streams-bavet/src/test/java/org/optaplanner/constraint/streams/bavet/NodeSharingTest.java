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

package org.optaplanner.constraint.streams.bavet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;

class NodeSharingTest {

    @Test
    void nodeSharingForEach() {
        BavetConstraintFactory<TestdataSolution> constraintFactory =
                new BavetConstraintFactory<>(TestdataSolution.buildSolutionDescriptor(), EnvironmentMode.REPRODUCIBLE);
        UniConstraintStream<TestdataEntity> stream1 = constraintFactory.forEach(TestdataEntity.class);
        UniConstraintStream<TestdataEntity> stream2 = constraintFactory.forEach(TestdataEntity.class);
        assertThat(stream1).isSameAs(stream2);

        Predicate<TestdataEntity> predicate = entity -> true;
        UniConstraintStream<TestdataEntity> filteredStream1 = stream1.filter(predicate);
        UniConstraintStream<TestdataEntity> filteredStream2 = stream2.filter(predicate);
        assertThat(filteredStream1).isSameAs(filteredStream2);
    }

    @Test
    void nodeSharingForEachUniquePair() {
        BavetConstraintFactory<TestdataSolution> constraintFactory =
                new BavetConstraintFactory<>(TestdataSolution.buildSolutionDescriptor(), EnvironmentMode.REPRODUCIBLE);
        BiConstraintStream<TestdataEntity, TestdataEntity> stream1 =
                constraintFactory.forEachUniquePair(TestdataEntity.class);
        BiConstraintStream<TestdataEntity, TestdataEntity> stream2 =
                constraintFactory.forEachUniquePair(TestdataEntity.class);
        assertThat(stream1).isSameAs(stream2);

        BiPredicate<TestdataEntity, TestdataEntity> predicate = (a, b) -> true;
        BiConstraintStream<TestdataEntity, TestdataEntity> filteredStream1 = stream1.filter(predicate);
        BiConstraintStream<TestdataEntity, TestdataEntity> filteredStream2 = stream2.filter(predicate);
        assertThat(filteredStream1).isSameAs(filteredStream2);
    }

    @Test
    void nodeSharingForEachUniquePairAndOneMore() {
        BavetConstraintFactory<TestdataSolution> constraintFactory =
                new BavetConstraintFactory<>(TestdataSolution.buildSolutionDescriptor(), EnvironmentMode.REPRODUCIBLE);
        TriConstraintStream<TestdataEntity, TestdataEntity, TestdataEntity> stream1 =
                constraintFactory.forEachUniquePair(TestdataEntity.class)
                        .join(TestdataEntity.class);
        TriConstraintStream<TestdataEntity, TestdataEntity, TestdataEntity> stream2 =
                constraintFactory.forEachUniquePair(TestdataEntity.class)
                        .join(TestdataEntity.class);
        assertThat(stream1).isSameAs(stream2);

        TriPredicate<TestdataEntity, TestdataEntity, TestdataEntity> predicate = (a, b, c) -> true;
        TriConstraintStream<TestdataEntity, TestdataEntity, TestdataEntity> filteredStream1 = stream1.filter(predicate);
        TriConstraintStream<TestdataEntity, TestdataEntity, TestdataEntity> filteredStream2 = stream2.filter(predicate);
        assertThat(filteredStream1).isSameAs(filteredStream2);
    }

}