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

package org.optaplanner.core.impl.score.director.drools;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Global;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieSession;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.domain.constraintweight.descriptor.ConstraintConfigurationDescriptor;
import org.optaplanner.core.impl.domain.constraintweight.descriptor.ConstraintWeightDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;

/**
 * Drools implementation of {@link ScoreDirectorFactory}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @see DroolsScoreDirector
 * @see ScoreDirectorFactory
 */
public class DroolsScoreDirectorFactory<Solution_> extends AbstractScoreDirectorFactory<Solution_> {

    private final KieBase kieBase;

    protected Map<Rule, Function<Solution_, Score<?>>> ruleToConstraintWeightExtractorMap;

    /**
     * @param solutionDescriptor never null
     * @param kieBase never null
     */
    public DroolsScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor, KieBase kieBase) {
        super(solutionDescriptor);
        this.kieBase = kieBase;
        checkIfGlobalScoreHolderExists(kieBase);
        createRuleToConstraintWeightExtractorMap(kieBase);
        solutionDescriptor.checkIfProblemFactsExist();
    }

    protected void checkIfGlobalScoreHolderExists(KieBase kieBase) {
        boolean hasGlobalScoreHolder = false;
        for (KiePackage kiePackage : kieBase.getKiePackages()) {
            for (Global global : kiePackage.getGlobalVariables()) {
                if (DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY.equals(global.getName())) {
                    hasGlobalScoreHolder = true;
                    // TODO Fail fast once global.getType() can be turned into a Class instead of a String
                    //                    if (!ScoreHolder.class.isAssignableFrom(global.getType())) {
                    //                        throw new IllegalStateException("The global with name (" + global.getName()
                    //                                + ") has a type (" + global.getType()
                    //                                + ") that does not implement " + ScoreHolder.class.getSimpleName() + ".");
                    //                    }
                    break;
                }
            }
        }
        if (!hasGlobalScoreHolder) {
            throw new IllegalArgumentException("The kieBase with kiePackages (" + kieBase.getKiePackages()
                    + ") has no global field called " + DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY + ".\n"
                    + "Check if the rule files are found and if the global field is spelled correctly.");
        }
    }

    protected void createRuleToConstraintWeightExtractorMap(KieBase kieBase) {
        ConstraintConfigurationDescriptor<Solution_> constraintConfigurationDescriptor = solutionDescriptor
                .getConstraintConfigurationDescriptor();
        if (constraintConfigurationDescriptor == null) {
            ruleToConstraintWeightExtractorMap = new LinkedHashMap<>(0);
            return;
        }
        Collection<ConstraintWeightDescriptor<Solution_>> constraintWeightDescriptors = constraintConfigurationDescriptor
                .getConstraintWeightDescriptors();
        ruleToConstraintWeightExtractorMap = new LinkedHashMap<>(constraintWeightDescriptors.size());
        for (ConstraintWeightDescriptor<Solution_> constraintWeightDescriptor : constraintWeightDescriptors) {
            String constraintPackage = constraintWeightDescriptor.getConstraintPackage();
            String constraintName = constraintWeightDescriptor.getConstraintName();
            Rule rule = kieBase.getRule(constraintPackage, constraintName);
            if (rule == null) {
                Rule potentialRule = kieBase.getKiePackages().stream().flatMap(kiePackage -> kiePackage.getRules().stream())
                        .filter(selectedRule -> selectedRule.getName().equals(constraintName)).findFirst().orElse(null);
                throw new IllegalStateException("The constraintConfigurationClass ("
                        + constraintConfigurationDescriptor.getConstraintConfigurationClass()
                        + ") has a " + ConstraintWeight.class.getSimpleName()
                        + " annotated member (" + constraintWeightDescriptor.getMemberAccessor()
                        + ") with constraintPackage/rulePackage (" + constraintPackage
                        + ") and constraintName/ruleName (" + constraintName
                        + ") for which no Drools rule exist in the DRL.\n"
                        + (potentialRule != null ? "Maybe the constraintPackage (" + constraintPackage + ") is wrong,"
                                + " because there is a rule with the same ruleName (" + constraintName
                                + "), but in a different rulePackage (" + potentialRule.getPackageName() + ")."
                                : "Maybe there is a typo in the constraintName (" + constraintName
                                        + ") so it not identical to the constraint's ruleName."));
            }
            ruleToConstraintWeightExtractorMap.put(rule, constraintWeightDescriptor.createExtractor());
        }
    }

    public Map<Rule, Function<Solution_, Score<?>>> getRuleToConstraintWeightExtractorMap() {
        return ruleToConstraintWeightExtractorMap;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public DroolsScoreDirector<Solution_> buildScoreDirector(
            boolean lookUpEnabled, boolean constraintMatchEnabledPreference) {
        return new DroolsScoreDirector<>(this, lookUpEnabled, constraintMatchEnabledPreference);
    }

    public KieSession newKieSession() {
        return kieBase.newKieSession();
    }

}
