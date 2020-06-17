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

package org.optaplanner.core.api.score.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.optaplanner.core.api.score.constraint.ConstraintMatchTotal.composeConstraintId;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.stream.ConstraintStreamScoreDirectorFactory;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishSolution;

@ExtendWith(ConstraintStreamTestExtension.class)
public abstract class AbstractConstraintStreamTest {

    protected static final String TEST_CONSTRAINT_NAME = "testConstraintName";

    protected final boolean constraintMatchEnabled;
    protected final ConstraintStreamImplType constraintStreamImplType;

    protected void assumeBavet() {
        Assumptions.assumeTrue(constraintStreamImplType == ConstraintStreamImplType.BAVET,
                "This functionality is not yet supported in Drools-based constraint streams.");
    }

    protected void assumeDrools() {
        Assumptions.assumeTrue(constraintStreamImplType == ConstraintStreamImplType.DROOLS,
                "This functionality is not yet supported in Bavet constraint streams.");
    }

    public AbstractConstraintStreamTest(boolean constraintMatchEnabled, ConstraintStreamImplType constraintStreamImplType) {
        this.constraintMatchEnabled = constraintMatchEnabled;
        this.constraintStreamImplType = constraintStreamImplType;
    }

    // ************************************************************************
    // SimpleScore creation and assertion methods
    // ************************************************************************

    protected InnerScoreDirector<TestdataLavishSolution> buildScoreDirector(Function<ConstraintFactory, Constraint> function) {
        return buildScoreDirector(TestdataLavishSolution::buildSolutionDescriptor, function);
    }

    protected <Solution_> InnerScoreDirector<Solution_> buildScoreDirector(
            Supplier<SolutionDescriptor<Solution_>> solutionDescriptorSupplier,
            Function<ConstraintFactory, Constraint> function) {
        ConstraintStreamScoreDirectorFactory<Solution_> scoreDirectorFactory = new ConstraintStreamScoreDirectorFactory<>(
                solutionDescriptorSupplier.get(),
                (constraintFactory) -> new Constraint[] { function.apply(constraintFactory) },
                constraintStreamImplType);
        return scoreDirectorFactory.buildScoreDirector(false, constraintMatchEnabled);
    }

    protected void assertScore(InnerScoreDirector<TestdataLavishSolution> scoreDirector,
            AssertableMatch... assertableMatches) {
        scoreDirector.triggerVariableListeners();
        SimpleScore score = (SimpleScore) scoreDirector.calculateScore();
        int scoreTotal = Arrays.stream(assertableMatches)
                .mapToInt(assertableMatch -> assertableMatch.score)
                .sum();
        if (constraintMatchEnabled) {
            String constraintPackage = scoreDirector.getSolutionDescriptor().getSolutionClass().getPackage().getName();
            for (AssertableMatch assertableMatch : assertableMatches) {
                Map<String, ConstraintMatchTotal> constraintMatchTotals = scoreDirector.getConstraintMatchTotalMap();
                String constraintId = composeConstraintId(constraintPackage, assertableMatch.constraintName);
                ConstraintMatchTotal constraintMatchTotal = constraintMatchTotals.get(constraintId);
                if (constraintMatchTotal == null) {
                    throw new IllegalStateException("Requested constraint matches for unknown constraint (" +
                            constraintId + ").");
                }
                if (constraintMatchTotal.getConstraintMatchSet().stream().noneMatch(assertableMatch::isEqualTo)) {
                    fail("The assertableMatch (" + assertableMatch + ") is lacking,"
                            + " it's not in the constraintMatchSet ("
                            + constraintMatchTotal.getConstraintMatchSet() + ").");
                }
            }
            List<ConstraintMatch> constraintMatches = scoreDirector.getConstraintMatchTotalMap().values()
                    .stream()
                    .flatMap(t -> t.getConstraintMatchSet().stream())
                    .collect(Collectors.toList());
            for (ConstraintMatch constraintMatch : constraintMatches) {
                if (Arrays.stream(assertableMatches)
                        .filter(assertableMatch -> assertableMatch.constraintName.equals(constraintMatch.getConstraintName()))
                        .noneMatch(assertableMatch -> assertableMatch.isEqualTo(constraintMatch))) {
                    fail("The constraintMatch (" + constraintMatch + ") is in excess,"
                            + " it's not in the assertableMatches (" + Arrays.toString(assertableMatches) + ").");
                }
            }
        }
        assertThat(score.getScore()).isEqualTo(scoreTotal);
    }

    protected static AssertableMatch assertMatch(Object... justifications) {
        return assertMatchWithScore(-1, justifications);
    }

    protected static AssertableMatch assertMatch(String constraintName, Object... justifications) {
        return assertMatchWithScore(-1, constraintName, justifications);
    }

    protected static AssertableMatch assertMatchWithScore(int score, Object... justifications) {
        return assertMatchWithScore(score, TEST_CONSTRAINT_NAME, justifications);
    }

    protected static AssertableMatch assertMatchWithScore(int score, String constraintName, Object... justifications) {
        return new AssertableMatch(score, constraintName, justifications);
    }

    protected static class AssertableMatch {

        private final int score;
        private final String constraintName;
        private final List<Object> justificationList;

        public AssertableMatch(int score, String constraintName, Object... justifications) {
            this.justificationList = Arrays.asList(justifications);
            this.constraintName = constraintName;
            this.score = score;
        }

        public boolean isEqualTo(ConstraintMatch constraintMatch) {
            if (score != ((SimpleScore) constraintMatch.getScore()).getScore()) {
                return false;
            }
            if (!constraintName.equals(constraintMatch.getConstraintName())) {
                return false;
            }
            List<Object> actualJustificationList = constraintMatch.getJustificationList();
            // Can't simply compare the lists, since the elements may be in different orders. The order is not relevant.
            if (actualJustificationList.size() != justificationList.size()) {
                return false;
            }
            return justificationList.containsAll(actualJustificationList);
        }

        @Override
        public String toString() {
            return constraintName + " " + justificationList + "=" + score;
        }

    }

}
