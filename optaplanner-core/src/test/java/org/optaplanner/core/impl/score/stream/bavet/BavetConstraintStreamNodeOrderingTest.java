/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.bavet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.api.score.stream.Joiners.equal;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.impl.score.director.stream.ConstraintStreamScoreDirector;
import org.optaplanner.core.impl.score.director.stream.ConstraintStreamScoreDirectorFactory;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetAbstractUniNode;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetFilterUniNode;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetFromUniNode;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetJoinBridgeUniNode;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishEntity;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishSolution;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishValue;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishValueGroup;

public class BavetConstraintStreamNodeOrderingTest {

    private final Function<ConstraintFactory, Constraint> constraintProvider =
            factory -> factory.fromUniquePair(TestdataLavishEntity.class,
                    equal(TestdataLavishEntity::getEntityGroup))
                    .filter((a, b) -> !a.equals(b))
                    .join(TestdataLavishValueGroup.class)
                    .filter((a, b, valueGroup) -> false)
                    .penalize("Some constraint", SimpleScore.ONE);
    private BavetConstraintSession<TestdataLavishSolution, SimpleScore> session;

    @BeforeEach
    void initializeSession() {
        ConstraintStreamScoreDirector<TestdataLavishSolution, SimpleScore> scoreDirector =
                buildScoreDirector(constraintProvider);
        scoreDirector.setWorkingSolution(TestdataLavishSolution.generateSolution());
        session = (BavetConstraintSession<TestdataLavishSolution, SimpleScore>) scoreDirector.getSession();
    }

    @Test
    void correctNumberOfFromNodes() {
        List<BavetFromUniNode<Object>> lavishEntityFromNodeList = session.findFromNodeList(TestdataLavishEntity.class);
        assertThat(lavishEntityFromNodeList).hasSize(1); // fromUniquePair() uses just one fromNode.
        List<BavetFromUniNode<Object>> lavishValueGroupFromNodeList =
                session.findFromNodeList(TestdataLavishValueGroup.class);
        assertThat(lavishValueGroupFromNodeList).hasSize(1); // join uses just one fromNode.
        List<BavetFromUniNode<Object>> lavishValueFromNodeList = session.findFromNodeList(TestdataLavishValue.class);
        assertThat(lavishValueFromNodeList).isEmpty(); // Not used in the constraint.
    }

    @Test
    void fromUniquePair() {
        List<BavetFromUniNode<Object>> lavishEntityFromNodeList = session.findFromNodeList(TestdataLavishEntity.class);

        BavetFromUniNode<Object> fromNode = lavishEntityFromNodeList.get(0);
        assertThat(fromNode.getNodeIndex())
                .as("fromNode is the first node of the constraint stream.")
                .isEqualTo(0);

        List<BavetAbstractUniNode<Object>> fromNodeChildNodes = fromNode.getChildNodes();
        assertThat(fromNodeChildNodes)
                .as("fromNode has a single child, a filterNode.")
                .hasSize(1);

        BavetFilterUniNode<Object> filterNode = (BavetFilterUniNode<Object>) fromNodeChildNodes.get(0);
        assertThat(filterNode.getNodeIndex())
                .as("filterNode is the second node of the constraint stream.")
                .isEqualTo(1);

        List<BavetAbstractUniNode<Object>> filterChildNodes = filterNode.getChildNodes();
        assertThat(filterChildNodes)
                .as("filterNode has two children, left and right join bridge for the unique pair.")
                .hasSize(2);

        BavetJoinBridgeUniNode<Object> leftJoinBridgeNode = (BavetJoinBridgeUniNode<Object>) filterChildNodes.get(0);
        assertThat(leftJoinBridgeNode.getNodeIndex())
                .as("Left JoinBridge is the third node of the constraint stream.")
                .isEqualTo(2);

        BavetJoinBridgeUniNode<Object> rightJoinBridgeNode = (BavetJoinBridgeUniNode<Object>) filterChildNodes.get(1);
        assertThat(rightJoinBridgeNode.getNodeIndex())
                .as("Right JoinBridge is the fourth node of the constraint stream.")
                .isEqualTo(2); // TODO needs to be 3
    }

    protected ConstraintStreamScoreDirector<TestdataLavishSolution, SimpleScore> buildScoreDirector(
            Function<ConstraintFactory, Constraint> function) {
        ConstraintStreamScoreDirectorFactory<TestdataLavishSolution, SimpleScore> scoreDirectorFactory =
                new ConstraintStreamScoreDirectorFactory<>(TestdataLavishSolution.buildSolutionDescriptor(),
                        (constraintFactory) -> new Constraint[] { function.apply(constraintFactory) },
                        ConstraintStreamImplType.BAVET);
        return scoreDirectorFactory.buildScoreDirector(false, false);
    }

}
