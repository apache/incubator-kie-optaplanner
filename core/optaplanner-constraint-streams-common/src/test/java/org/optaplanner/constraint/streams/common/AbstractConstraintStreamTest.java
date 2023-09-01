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

package org.optaplanner.constraint.streams.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.optaplanner.core.api.score.constraint.ConstraintMatchTotal.composeConstraintId;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.DefaultConstraintJustification;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishSolution;

@ExtendWith(ConstraintStreamTestExtension.class)
@Execution(ExecutionMode.CONCURRENT)
public abstract class AbstractConstraintStreamTest {

    protected static final String TEST_CONSTRAINT_NAME = "testConstraintName";

    protected final ConstraintStreamImplSupport implSupport;

    protected final void assumeBavet() {
        implSupport.assumeBavet();
    }

    protected void assumeDrools() {
        implSupport.assumeDrools();
    }

    protected AbstractConstraintStreamTest(ConstraintStreamImplSupport implSupport) {
        this.implSupport = Objects.requireNonNull(implSupport);
    }

    // ************************************************************************
    // SimpleScore creation and assertion methods
    // ************************************************************************

    protected InnerScoreDirector<TestdataLavishSolution, SimpleScore> buildScoreDirector(
            Function<ConstraintFactory, Constraint> function) {
        return buildScoreDirector(TestdataLavishSolution.buildSolutionDescriptor(), factory -> new Constraint[] {
                function.apply(factory)
        });
    }

    protected <Score_ extends Score<Score_>, Solution_> InnerScoreDirector<Solution_, Score_> buildScoreDirector(
            SolutionDescriptor<Solution_> solutionDescriptorSupplier, ConstraintProvider constraintProvider) {
        return implSupport.buildScoreDirector(solutionDescriptorSupplier, constraintProvider);
    }

    protected <Solution_> void assertScore(InnerScoreDirector<Solution_, SimpleScore> scoreDirector,
            AssertableMatch... assertableMatches) {
        scoreDirector.triggerVariableListeners();
        SimpleScore score = scoreDirector.calculateScore();
        int scoreTotal = Arrays.stream(assertableMatches)
                .mapToInt(assertableMatch -> assertableMatch.score)
                .sum();
        if (implSupport.isConstreamMatchEnabled()) {
            String constraintPackage = scoreDirector.getSolutionDescriptor().getSolutionClass().getPackage().getName();
            for (AssertableMatch assertableMatch : assertableMatches) {
                Map<String, ConstraintMatchTotal<SimpleScore>> constraintMatchTotals =
                        scoreDirector.getConstraintMatchTotalMap();
                String constraintId = composeConstraintId(constraintPackage, assertableMatch.constraintName);
                ConstraintMatchTotal<SimpleScore> constraintMatchTotal = constraintMatchTotals.get(constraintId);
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
            Map<String, ConstraintMatchTotal<SimpleScore>> constraintMatchTotalMap =
                    scoreDirector.getConstraintMatchTotalMap();
            for (ConstraintMatchTotal<SimpleScore> constraintMatchTotal : constraintMatchTotalMap.values()) {
                for (ConstraintMatch<SimpleScore> constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
                    if (Arrays.stream(assertableMatches)
                            .filter(assertableMatch -> assertableMatch.constraintName
                                    .equals(constraintMatch.getConstraintName()))
                            .noneMatch(assertableMatch -> assertableMatch.isEqualTo(constraintMatch))) {
                        fail("The constraintMatch (" + constraintMatch + ") is in excess,"
                                + " it's not in the assertableMatches (" + Arrays.toString(assertableMatches) + ").");
                    }
                }
            }
        }
        assertThat(score.score()).isEqualTo(scoreTotal);
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

        public boolean isEqualTo(ConstraintMatch<?> constraintMatch) {
            if (score != ((SimpleScore) constraintMatch.getScore()).score()) {
                return false;
            }
            if (!constraintName.equals(constraintMatch.getConstraintName())) {
                return false;
            }
            ConstraintJustification justification = constraintMatch.getJustification();
            if (justification instanceof DefaultConstraintJustification) {
                List<?> actualJustificationList = ((DefaultConstraintJustification) justification).getFacts();
                if (actualJustificationList.size() != justificationList.size()) {
                    return false;
                }
                // Can't simply compare the lists, since the elements may be in different orders. The order is not relevant.
                return justificationList.containsAll(actualJustificationList);
            } else { // Support for custom justification mapping.
                if (justificationList.size() != 1) {
                    Assertions.fail("Expected number of justifications (" + justificationList.size() +
                            ") does not match actual (1; " + justification + ").");
                }
                return justification == justificationList.get(0);
            }
        }

        @Override
        public String toString() {
            return constraintName + " " + justificationList + "=" + score;
        }

    }

    protected static Set<Object> asSet(Object... facts) {
        return Arrays.stream(facts).collect(Collectors.toSet());
    }

    protected static final class TestConstraintJustification<Score_ extends Score<Score_>>
            implements ConstraintJustification {

        private final Score_ score;
        private final Object[] facts;

        public TestConstraintJustification(Score_ score, Object... facts) {
            this.score = Objects.requireNonNull(score);
            this.facts = Objects.requireNonNull(facts);
        }

        public Score_ getScore() {
            return score;
        }

        public Object[] getFacts() {
            return facts;
        }

    }

}
