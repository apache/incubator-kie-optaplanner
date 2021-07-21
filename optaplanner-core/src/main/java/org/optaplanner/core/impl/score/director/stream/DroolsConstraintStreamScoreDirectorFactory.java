/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.director.stream;

import static java.util.stream.Collectors.*;
import static org.drools.model.DSL.globalOf;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.drools.ancompiler.KieBaseUpdaterANC;
import org.drools.model.Global;
import org.drools.model.Model;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.KieBaseMutabilityOption;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.DirectFiringOption;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;
import org.kie.internal.builder.conf.PropertySpecificOption;
import org.kie.internal.event.rule.RuleEventManager;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.drools.OptaPlannerRuleEventListener;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;
import org.optaplanner.core.impl.score.inliner.WeightedScoreImpacter;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.SessionDescriptor;

public final class DroolsConstraintStreamScoreDirectorFactory<Solution_, Score_ extends Score<Score_>>
        extends AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> {

    private final DroolsConstraint<Solution_>[] constraints;
    private final Score_ zeroScore;
    private final boolean droolsAlphaNetworkCompilationEnabled;
    private KieBaseDescriptor<Solution_> kieBaseDescriptor;

    public DroolsConstraintStreamScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            ConstraintProvider constraintProvider, boolean droolsAlphaNetworkCompilationEnabled) {
        super(solutionDescriptor);
        DroolsConstraintFactory<Solution_> constraintFactory =
                new DroolsConstraintFactory<>(solutionDescriptor, droolsAlphaNetworkCompilationEnabled);
        constraints = buildConstraints(constraintProvider, constraintFactory);
        this.constraintSessionFactory = constraintFactory.buildSessionFactory(constraints);
        DroolsConstraintFactory<Solution_> constraintFactory = new DroolsConstraintFactory<>(solutionDescriptor);
        constraints = Arrays.stream(buildConstraints(constraintProvider, constraintFactory))
                .map(DroolsConstraint.class::cast)
                .toArray(DroolsConstraint[]::new);
        this.zeroScore = (Score_) solutionDescriptor.getScoreDefinition().getZeroScore();
        this.droolsAlphaNetworkCompilationEnabled = droolsAlphaNetworkCompilationEnabled;
        // Fail fast on duplicate constraint IDs.
        Map<String, List<Constraint>> constraintsPerIdMap = Arrays.stream(constraints)
                .collect(groupingBy(Constraint::getConstraintId));
        constraintsPerIdMap.forEach((constraintId, constraintList) -> {
            if (constraintList.size() > 1) {
                throw new IllegalStateException(
                        "There are multiple constraints with the same name in a package (" + constraintId + ").");
            }
        });
    }

    @Override
    public DroolsConstraintStreamScoreDirector<Solution_, Score_> buildScoreDirector(boolean lookUpEnabled,
            boolean constraintMatchEnabledPreference) {
        return new DroolsConstraintStreamScoreDirector<>(this, lookUpEnabled, constraintMatchEnabledPreference);
    }

    public SessionDescriptor<Score_> newConstraintStreamingSession(boolean constraintMatchEnabled,
            Solution_ workingSolution) {
        if (kieBaseDescriptor == null) {
            Package pack = solutionDescriptor.getSolutionClass().getPackage();
            String constraintPackageName = (pack == null) ? "" : pack.getName();
            // Each constraint gets its own global, in which it will keep its impacter.
            // Impacters carry constraint weights, and therefore the instances are solution-specific.
            AtomicInteger idCounter = new AtomicInteger(0);
            Map<DroolsConstraint<Solution_>, Global<WeightedScoreImpacter>> constraintToGlobalMap =
                    Arrays.stream(constraints)
                            .collect(toMap(Function.identity(),
                                    c -> globalOf(WeightedScoreImpacter.class, constraintPackageName,
                                            "scoreImpacter" + idCounter.getAndIncrement())));
            ModelImpl model = Arrays.stream(constraints)
                    .map(constraint -> constraint.buildRule(constraintToGlobalMap.get(constraint)))
                    .reduce(new ModelImpl(), ModelImpl::addRule, (m, key) -> m);
            constraintToGlobalMap.forEach((constraint, global) -> model.addGlobal(global));
            KieBase kieBase = buildKieBaseFromModel(model, droolsAlphaNetworkCompilationEnabled);
            kieBaseDescriptor = new KieBaseDescriptor<>(constraintToGlobalMap, kieBase);
        }
        return buildSession(workingSolution, constraintMatchEnabled);
    }

    private static KieSession buildKieSessionFromKieBase(KieBase kieBase) {
        KieSessionConfiguration config = KieServices.get().newKieSessionConfiguration();
        config.setOption(DirectFiringOption.YES); // For performance; not applicable to DRL due to insertLogical etc.
        Environment environment = KieServices.get().newEnvironment();
        return kieBase.newKieSession(config, environment);
    }

    private SessionDescriptor<Score_> buildSession(Solution_ workingSolution, boolean constraintMatchEnabled) {
        // Extract constraint weights.
        Map<DroolsConstraint<Solution_>, Score_> constraintToWeightMap = Arrays.stream(constraints)
                .collect(toMap(Function.identity(), constraint -> constraint.extractConstraintWeight(workingSolution)));
        // Create the session itself.
        KieSession kieSession = buildKieSessionFromKieBase(kieBaseDescriptor.getKieBase());
        ((RuleEventManager) kieSession).addEventListener(new OptaPlannerRuleEventListener()); // Enables undo in rules.
        // Build and set the impacters for each constraint; this locks in the constraint weights.
        ScoreDefinition<Score_> scoreDefinition = solutionDescriptor.getScoreDefinition();
        ScoreInliner<Score_> scoreInliner =
                scoreDefinition.buildScoreInliner((Map) constraintToWeightMap, constraintMatchEnabled);
        constraintToWeightMap.forEach((constraint, weight) -> {
            if (Objects.equals(weight, zeroScore)) {
                return;
            }
            String globalName = kieBaseDescriptor.getConstraintToGlobalMap().get(constraint).getName();
            kieSession.setGlobal(globalName, scoreInliner.buildWeightedScoreImpacter(constraint));
        });
        // Return only the inliner as that holds the work product of the individual impacters.
        return new SessionDescriptor<>(kieSession, new ConstraintDisablingAgendaFilter(constraintToWeightMap), scoreInliner);
    }

    private static KieBase buildKieBaseFromModel(Model model, boolean droolsAlphaNetworkCompilationEnabled) {
        KieBaseConfiguration kieBaseConfiguration = KieServices.get().newKieBaseConfiguration();
        kieBaseConfiguration.setOption(KieBaseMutabilityOption.DISABLED); // For performance; applicable to DRL too.
        kieBaseConfiguration.setProperty(PropertySpecificOption.PROPERTY_NAME,
                PropertySpecificOption.DISABLED.name()); // Users of CS must not rely on underlying Drools gimmicks.
        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel(model, kieBaseConfiguration);
        if (droolsAlphaNetworkCompilationEnabled) {
            KieBaseUpdaterANC.generateAndSetInMemoryANC(kieBase); // Enable Alpha Network Compiler for performance.
        }
        return kieBase;
    }

    @Override
    public Constraint[] getConstraints() {
        return constraints;
    }

    public boolean isDroolsAlphaNetworkCompilationEnabled() {
        return droolsAlphaNetworkCompilationEnabled;
    }

    private final class ConstraintDisablingAgendaFilter implements AgendaFilter {

        private final Set<String> disabledConstraintIdSet;

        public ConstraintDisablingAgendaFilter(Map<DroolsConstraint<Solution_>, Score_> constraintToWeightMap) {
            this.disabledConstraintIdSet = constraintToWeightMap.entrySet()
                    .stream()
                    .filter(entry -> Objects.equals(entry.getValue(), zeroScore))
                    .map(Map.Entry::getKey)
                    .map(Constraint::getConstraintId)
                    .collect(toSet());
        }

        @Override
        public boolean accept(Match match) {
            if (disabledConstraintIdSet.isEmpty()) {
                return true;
            }
            Rule rule = match.getRule();
            String constraintId = ConstraintMatchTotal.composeConstraintId(rule.getPackageName(), rule.getName());
            return !disabledConstraintIdSet.contains(constraintId);
        }
    }

}
