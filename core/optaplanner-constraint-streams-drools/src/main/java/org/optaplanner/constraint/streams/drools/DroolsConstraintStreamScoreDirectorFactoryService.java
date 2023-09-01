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

package org.optaplanner.constraint.streams.drools;

import static org.optaplanner.core.api.score.stream.ConstraintStreamImplType.BAVET;

import java.util.Objects;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactory;
import org.optaplanner.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactoryService;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorType;

public final class DroolsConstraintStreamScoreDirectorFactoryService<Solution_, Score_ extends Score<Score_>>
        extends AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> {

    @Override
    public ScoreDirectorType getSupportedScoreDirectorType() {
        return ScoreDirectorType.CONSTRAINT_STREAMS;
    }

    @Override
    public Supplier<AbstractScoreDirectorFactory<Solution_, Score_>> buildScoreDirectorFactory(ClassLoader classLoader,
            SolutionDescriptor<Solution_> solutionDescriptor, ScoreDirectorFactoryConfig config,
            EnvironmentMode environmentMode) {
        ConstraintStreamImplType constraintStreamImplType_ =
                Objects.requireNonNullElse(config.getConstraintStreamImplType(), ConstraintStreamImplType.DROOLS);
        if (constraintStreamImplType_ == BAVET) {
            return null;
        }
        if (config.getConstraintProviderClass() != null) {
            if (!ConstraintProvider.class.isAssignableFrom(config.getConstraintProviderClass())) {
                throw new IllegalArgumentException(
                        "The constraintProviderClass (" + config.getConstraintProviderClass()
                                + ") does not implement " + ConstraintProvider.class.getSimpleName() + ".");
            }
            return () -> {
                ConstraintProvider constraintProvider = ConfigUtils.newInstance(config,
                        "constraintProviderClass", config.getConstraintProviderClass());
                ConfigUtils.applyCustomProperties(constraintProvider, "constraintProviderClass",
                        config.getConstraintProviderCustomProperties(), "constraintProviderCustomProperties");
                boolean isDroolsAlphaNetworkEnabled =
                        Objects.requireNonNullElse(config.isDroolsAlphaNetworkCompilationEnabled(), true);
                if (config.getGizmoKieBaseSupplier() != null) {
                    return new DroolsConstraintStreamScoreDirectorFactory<>(solutionDescriptor,
                            (KieBaseDescriptor<Solution_>) config.getGizmoKieBaseSupplier(),
                            isDroolsAlphaNetworkEnabled);
                }
                return buildScoreDirectorFactory(solutionDescriptor, constraintProvider, environmentMode,
                        isDroolsAlphaNetworkEnabled);
            };
        } else {
            if (config.getConstraintProviderCustomProperties() != null) {
                throw new IllegalStateException("If there is no constraintProviderClass (" + config.getConstraintProviderClass()
                        + "), then there can be no constraintProviderCustomProperties ("
                        + config.getConstraintProviderCustomProperties() + ") either.");
            }
            return null;
        }
    }

    @Override
    public boolean supportsImplType(ConstraintStreamImplType constraintStreamImplType) {
        return constraintStreamImplType == ConstraintStreamImplType.DROOLS;
    }

    @Override
    public AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> buildScoreDirectorFactory(
            SolutionDescriptor<Solution_> solutionDescriptor, ConstraintProvider constraintProvider,
            EnvironmentMode environmentMode, boolean droolsAlphaNetworkCompilationEnabled) {
        return new DroolsConstraintStreamScoreDirectorFactory<>(solutionDescriptor, constraintProvider,
                droolsAlphaNetworkCompilationEnabled);
    }

    @Override
    public ConstraintFactory buildConstraintFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            EnvironmentMode environmentMode) {
        return new DroolsConstraintFactory<>(solutionDescriptor);
    }

}
