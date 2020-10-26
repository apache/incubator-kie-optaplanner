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

package org.optaplanner.core.impl.score.stream.drools;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.model.Model;
import org.drools.model.functions.accumulate.GroupKey;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.kie.api.KieBase;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieSession;
import org.kie.internal.event.rule.RuleEventManager;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;
import org.optaplanner.core.impl.score.director.drools.OptaPlannerRuleEventListener;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.ConstraintSession;
import org.optaplanner.core.impl.score.stream.ConstraintSessionFactory;
import org.optaplanner.core.impl.score.stream.drools.common.FactTuple;
import org.optaplanner.core.impl.score.stream.drools.common.rules.RuleAssembly;

public final class DroolsConstraintSessionFactory<Solution_, Score_ extends Score<Score_>>
        implements ConstraintSessionFactory<Solution_, Score_> {

    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final Model originalModel;
    private final KieBase originalKieBase;
    private final Map<Rule, DroolsConstraint<Solution_>> compiledRuleToConstraintMap;
    private final Map<String, org.drools.model.Rule> constraintToModelRuleMap;
    private final Map<Rule, Class[]> compiledRuleToExpectedTypesMap;
    private KieBase currentKieBase;
    private Set<String> currentlyDisabledConstraintIdSet = null;

    public DroolsConstraintSessionFactory(SolutionDescriptor<Solution_> solutionDescriptor, Model model,
            Map<org.drools.model.Rule, Class[]> modelRuleToExpectedTypesMap,
            DroolsConstraint<Solution_>... constraints) {
        this.solutionDescriptor = solutionDescriptor;
        this.originalModel = model;
        this.originalKieBase = KieBaseBuilder.createKieBaseFromModel(model);
        this.currentKieBase = originalKieBase;
        this.compiledRuleToConstraintMap = Arrays.stream(constraints)
                .collect(toMap(constraint -> currentKieBase.getRule(constraint.getConstraintPackage(),
                        constraint.getConstraintName()), Function.identity()));
        this.constraintToModelRuleMap = Arrays.stream(constraints)
                .collect(toMap(Constraint::getConstraintId, constraint -> model.getRules().stream()
                        .filter(rule -> Objects.equals(rule.getName(), constraint.getConstraintName()))
                        .filter(rule -> Objects.equals(rule.getPackage(), constraint.getConstraintPackage()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Impossible state: Rule for constraint (" +
                                constraint + ") not found."))));
        this.compiledRuleToExpectedTypesMap = compiledRuleToConstraintMap.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> {
                    DroolsConstraint<Solution_> constraint = e.getValue();
                    org.drools.model.Rule modelRule = constraintToModelRuleMap.get(constraint.getConstraintId());
                    return modelRuleToExpectedTypesMap.get(modelRule);
                }));
    }

    /**
     * Converts justification list to another justification list, this one matching the expected scoring stream.
     * For example, if a scoring stream of cardinality 2 operates on facts of A and B, the list returned by this
     * method will only have these two facts. Order is not guaranteed.
     *
     * <p>
     * Due to the nature of the justification list coming from Drools, this method is very fragile.
     * The facts often come unordered and mixed with other facts not relevant to the problem at hand.
     * Therefore, this method is a set of heuristics that makes all the constraint stream tests pass.
     * However, it is possible that, as new constraint stream building block combinations are tested, the set of
     * heuristics inside this method will have to be redesigned.
     *
     * @param justificationList unordered list of justifications coming from the score director
     * @param expectedCount how many justifications are expected to be returned (1 for uni stream, 2 for bi stream, ...)
     * @param expectedTypes as defined by {@link RuleAssembly#getExpectedJustificationTypes()}.
     * @return never null
     */
    private static List<Object> matchJustificationsToOutput(List<Object> justificationList, int expectedCount,
            Class<?>... expectedTypes) {
        if (expectedTypes.length == 0) {
            throw new IllegalStateException("Impossible: there are no 0-cardinality constraint streams.");
        }
        List<Object> ungroupedJustificationList = ungroupJustifications(justificationList);
        Object[] matching = new Object[expectedTypes.length];
        // First process non-Object matches, as those are the most descriptive.
        for (int i = 0; i < expectedTypes.length; i++) {
            Class<?> expectedType = expectedTypes[i];
            if (Objects.equals(expectedType, Object.class)) {
                continue;
            }
            Object match = ungroupedJustificationList.stream()
                    .filter(j -> expectedType.isAssignableFrom(j.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Impossible: no justification of type ("
                            + expectedType + ")."));
            ungroupedJustificationList.remove(match);
            matching[i] = match;
        }
        // Fill the remaining places with Object matches, but keep their original order coming from expectedMatches.
        for (int i = 0; i < expectedTypes.length; i++) {
            if (matching[i] != null) {
                continue;
            }
            Object match = ungroupedJustificationList.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Impossible: there are no more constraint matches."));
            ungroupedJustificationList.remove(match);
            matching[i] = match;
        }
        if (matching.length > 1) {
            // The justifications will be enumerated. A, B, C, ...
            return Arrays.asList(matching);
        }
        Object item = matching[0];
        Class<?> expectedType = expectedTypes[0];
        if (FactTuple.class.isAssignableFrom(expectedType) || item instanceof FactTuple) {
            /*
             * The justifications will all come from a single tuple (eg. BiTuple<A, B>).
             * If stream cardinality < tuple cardinality, we will ignore some of the later elements in the tuple.
             * This happens when we're doing a groupBy() without any collectors - in that case, the latter
             * elements of the tuple will be dummy collector(s).
             */
            return ((FactTuple) item).asList()
                    .subList(0, expectedCount);
        } else {
            // This comes from a simple uni stream.
            return Collections.singletonList(item);
        }
    }

    private static List<Object> ungroupJustifications(List<Object> justificationList) {
        // Send all groupKey instances to the beginning of the justification list as we can not rely on Drools putting
        // them there for us.
        Predicate<Object> isGroupKey = o -> o instanceof GroupKey;
        Stream<Object> groupKeyValues = justificationList.stream()
                .filter(isGroupKey)
                .flatMap(groupKey -> {
                    Object ungrouped = ((GroupKey) groupKey).getKey();
                    if (ungrouped instanceof FactTuple) {
                        FactTuple factTuple = (FactTuple) ungrouped;
                        return factTuple.asList().stream();
                    }
                    return Stream.of(ungrouped);
                });
        Stream<Object> otherValues = justificationList.stream()
                .filter(o -> !isGroupKey.test(o))
                .flatMap(o -> {
                    if (o instanceof Object[]) { // Double accumulates return results as arrays of two objects.
                        return Arrays.stream((Object[]) o);
                    } else {
                        return Stream.of(o);
                    }
                });
        return Stream.concat(groupKeyValues, otherValues)
                .collect(Collectors.toList());
    }

    @Override
    public ConstraintSession<Solution_, Score_> buildSession(boolean constraintMatchEnabled, Solution_ workingSolution) {
        ScoreDefinition<Score_> scoreDefinition = solutionDescriptor.getScoreDefinition();
        // Make sure the constraint justifications match what comes out of Bavet.
        AbstractScoreHolder<Score_> scoreHolder = scoreDefinition.buildScoreHolder(constraintMatchEnabled);
        scoreHolder.setJustificationListConverter(
                (justificationList, rule) -> {
                    DroolsConstraint<Solution_> constraint = compiledRuleToConstraintMap.get(rule);
                    Class[] expectedTypes = compiledRuleToExpectedTypesMap.get(rule);
                    return matchJustificationsToOutput(justificationList,
                            constraint.getConsequence().getTerminalNode().getCardinality(), expectedTypes);
                });
        // Determine which rules to enable based on the fact that their constraints carry weight.
        Score_ zeroScore = scoreDefinition.getZeroScore();
        Set<String> disabledConstraintIdSet = new LinkedHashSet<>(0);
        compiledRuleToConstraintMap.forEach((compiledRule, constraint) -> {
            Score_ constraintWeight = (Score_) constraint.extractConstraintWeight(workingSolution);
            scoreHolder.configureConstraintWeight(compiledRule, constraintWeight);
            if (constraintWeight.equals(zeroScore)) {
                disabledConstraintIdSet.add(constraint.getConstraintId());
            }
        });
        // Determine the KieBase to use.
        if (disabledConstraintIdSet.isEmpty()) { // Shortcut; don't change the original KieBase.
            currentKieBase = originalKieBase;
            currentlyDisabledConstraintIdSet = null;
        } else if (!disabledConstraintIdSet.equals(currentlyDisabledConstraintIdSet)) {
            // Only rebuild the active KieBase when the set of disabled constraints changed.
            ModelImpl model = new ModelImpl().withGlobals(originalModel.getGlobals());
            constraintToModelRuleMap.forEach((constraintId, modelRule) -> {
                if (disabledConstraintIdSet.contains(constraintId)) {
                    return;
                }
                model.addRule(modelRule);
            });
            currentKieBase = KieBaseBuilder.createKieBaseFromModel(model);
            currentlyDisabledConstraintIdSet = disabledConstraintIdSet;
        }
        // Create the session itself.
        KieSession kieSession = currentKieBase.newKieSession();
        ((RuleEventManager) kieSession).addEventListener(new OptaPlannerRuleEventListener()); // Enables undo in rules.
        kieSession.setGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY, scoreHolder);
        return new DroolsConstraintSession<>(solutionDescriptor, kieSession, scoreHolder);
    }

}
