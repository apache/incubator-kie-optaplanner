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

package org.optaplanner.core.impl.score.stream.drools;

import java.util.Objects;
import org.drools.ancompiler.KieBaseUpdaterANC;
import org.drools.model.Model;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.KieBaseMutabilityOption;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.DirectFiringOption;
import org.kie.internal.builder.conf.PropertySpecificOption;
import org.kie.internal.event.rule.RuleEventManager;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.drools.OptaPlannerRuleEventListener;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;
import org.optaplanner.core.impl.score.inliner.WeightedScoreImpacter;
import org.optaplanner.core.impl.score.stream.ConstraintSessionFactory;

public final class DroolsConstraintSessionFactory<Solution_, Score_ extends Score<Score_>>
        implements ConstraintSessionFactory<Solution_, Score_> {

    private final ScoreDefinition<Score_> scoreDefinition;
    private final DroolsConstraintFactory<Solution_> constraintFactory;
    private final Constraint[] constraints;
    private final boolean droolsAlphaNetworkCompilationEnabled;

    public DroolsConstraintSessionFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            DroolsConstraintFactory<Solution_> coinstraintFactory, boolean droolsAlphaNetworkCompilationEnabled,
            Constraint... constraints) {
        this.scoreDefinition = solutionDescriptor.getScoreDefinition();
        this.constraintFactory = Objects.requireNonNull(coinstraintFactory);
        this.constraints = Objects.requireNonNull(constraints);
        this.droolsAlphaNetworkCompilationEnabled = droolsAlphaNetworkCompilationEnabled;
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

    private static KieSession buildKieSessionFromKieBase(KieBase kieBase) {
        KieSessionConfiguration config = KieServices.get().newKieSessionConfiguration();
        config.setOption(DirectFiringOption.YES); // For performance; not applicable to DRL due to insertLogical etc.
        Environment environment = KieServices.get().newEnvironment();
        return kieBase.newKieSession(config, environment);
    }

    public boolean isDroolsAlphaNetworkCompilationEnabled() {
        return droolsAlphaNetworkCompilationEnabled;
    }

    @Override
    public SessionDescriptor buildSession(boolean constraintMatchEnabled, Solution_ workingSolution) {
        Score_ zeroScore = scoreDefinition.getZeroScore();
        ScoreInliner<Score_> scoreInliner = scoreDefinition.buildScoreInliner(constraintMatchEnabled);

        ModelImpl model = new ModelImpl();
        for (Constraint constraint : constraints) {
            if (constraint.getConstraintFactory() != constraintFactory) {
                throw new IllegalStateException("The constraint (" + constraint.getConstraintId()
                        + ") must be created from the same constraintFactory.");
            }
            DroolsConstraint<Solution_> droolsConstraint = (DroolsConstraint<Solution_>) constraint;
            Score_ weight = (Score_) droolsConstraint.extractConstraintWeight(workingSolution);
            if (weight.equals(zeroScore)) { // Disable the rule for this constraint.
                continue;
            }
            WeightedScoreImpacter impacter = scoreInliner.buildWeightedScoreImpacter(weight);
            model.addRule(droolsConstraint.buildRule(impacter));
        }
        // Create the session itself.
        // TODO expensive; figure out some cache?
        KieBase kieBase = buildKieBaseFromModel(model, droolsAlphaNetworkCompilationEnabled);
        KieSession kieSession = buildKieSessionFromKieBase(kieBase);
        ((RuleEventManager) kieSession).addEventListener(new OptaPlannerRuleEventListener()); // Enables undo in rules.
        return new SessionDescriptor<>(kieSession, scoreInliner);
    }

    public static final class SessionDescriptor<Score_ extends Score<Score_>> {

        private final KieSession session;
        private final ScoreInliner<Score_> scoreInliner;

        public SessionDescriptor(KieSession session, ScoreInliner<Score_> scoreInliner) {
            this.session = session;
            this.scoreInliner = scoreInliner;
        }

        public KieSession getSession() {
            return session;
        }

        public ScoreInliner<Score_> getScoreInliner() {
            return scoreInliner;
        }
    }

}
