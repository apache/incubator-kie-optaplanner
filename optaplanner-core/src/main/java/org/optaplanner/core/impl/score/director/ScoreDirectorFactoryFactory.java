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

package org.optaplanner.core.impl.score.director;

import static org.optaplanner.core.impl.score.director.ScoreDirectorType.*;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.easy.EasyScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.stream.AbstractConstraintStreamScoreDirectorFactory;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

public class ScoreDirectorFactoryFactory<Solution_, Score_ extends Score<Score_>> {

    private final ScoreDirectorFactoryConfig config;

    public ScoreDirectorFactoryFactory(ScoreDirectorFactoryConfig config) {
        this.config = config;
    }

    public InnerScoreDirectorFactory<Solution_, Score_> buildScoreDirectorFactory(ClassLoader classLoader,
            EnvironmentMode environmentMode, SolutionDescriptor<Solution_> solutionDescriptor) {
        AbstractScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory =
                decideMultipleScoreDirectorFactories(classLoader, solutionDescriptor);
        if (config.getAssertionScoreDirectorFactory() != null) {
            if (config.getAssertionScoreDirectorFactory().getAssertionScoreDirectorFactory() != null) {
                throw new IllegalArgumentException("A assertionScoreDirectorFactory ("
                        + config.getAssertionScoreDirectorFactory() + ") cannot have a non-null assertionScoreDirectorFactory ("
                        + config.getAssertionScoreDirectorFactory().getAssertionScoreDirectorFactory() + ").");
            }
            if (environmentMode.compareTo(EnvironmentMode.FAST_ASSERT) > 0) {
                throw new IllegalArgumentException("A non-null assertionScoreDirectorFactory ("
                        + config.getAssertionScoreDirectorFactory() + ") requires an environmentMode ("
                        + environmentMode + ") of " + EnvironmentMode.FAST_ASSERT + " or lower.");
            }
            ScoreDirectorFactoryFactory<Solution_, Score_> assertionScoreDirectorFactoryFactory =
                    new ScoreDirectorFactoryFactory<>(config.getAssertionScoreDirectorFactory());
            scoreDirectorFactory.setAssertionScoreDirectorFactory(assertionScoreDirectorFactoryFactory
                    .buildScoreDirectorFactory(classLoader, EnvironmentMode.NON_REPRODUCIBLE, solutionDescriptor));
        }
        scoreDirectorFactory.setInitializingScoreTrend(InitializingScoreTrend.parseTrend(
                config.getInitializingScoreTrend() == null ? InitializingScoreTrendLevel.ANY.name()
                        : config.getInitializingScoreTrend(),
                solutionDescriptor.getScoreDefinition().getLevelsSize()));
        if (environmentMode.isNonIntrusiveFullAsserted()) {
            scoreDirectorFactory.setAssertClonedSolution(true);
        }
        return scoreDirectorFactory;
    }

    protected AbstractScoreDirectorFactory<Solution_, Score_> decideMultipleScoreDirectorFactories(
            ClassLoader classLoader, SolutionDescriptor<Solution_> solutionDescriptor) {
        // Load all known Score Director Factories via SPI.
        ServiceLoader<ScoreDirectorFactoryProvider> scoreDirectorFactoryProviders =
                ServiceLoader.load(ScoreDirectorFactoryProvider.class);
        Map<ScoreDirectorType, InnerScoreDirectorFactory<Solution_, Score_>> factories =
                new EnumMap<>(ScoreDirectorType.class);
        for (ScoreDirectorFactoryProvider<Solution_, Score_> provider : scoreDirectorFactoryProviders) {
            InnerScoreDirectorFactory<Solution_, Score_> factory =
                    provider.getScoreDirectorFactory(classLoader, solutionDescriptor, config);
            if (factory != null) {
                factories.put(provider.getSupportedScoreDirectorType(), factory);
            }
        }
        EasyScoreDirectorFactory<Solution_, Score_> easyScoreDirectorFactory =
                (EasyScoreDirectorFactory<Solution_, Score_>) factories.get(EASY);
        AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> constraintStreamScoreDirectorFactory =
                (AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_>) factories.get(CONSTRAINT_STREAMS);
        IncrementalScoreDirectorFactory<Solution_, Score_> incrementalScoreDirectorFactory =
                (IncrementalScoreDirectorFactory<Solution_, Score_>) factories.get(INCREMENTAL);
        DroolsScoreDirectorFactory<Solution_, Score_> droolsScoreDirectorFactory =
                (DroolsScoreDirectorFactory<Solution_, Score_>) factories.get(DROOLS);

        // Make sure only one SDF is truly available.
        checkMultipleScoreDirectorFactoryTypes(easyScoreDirectorFactory, constraintStreamScoreDirectorFactory,
                incrementalScoreDirectorFactory, droolsScoreDirectorFactory);

        // Fail fast if the config doesn't match the SDF.
        AbstractScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory;
        if (easyScoreDirectorFactory != null) {
            validateNoDroolsAlphaNetworkCompilation();
            validateNoGizmoKieBaseSupplier();
            scoreDirectorFactory = easyScoreDirectorFactory;
        } else if (constraintStreamScoreDirectorFactory != null) {
            if (config.getConstraintStreamImplType() == ConstraintStreamImplType.BAVET) {
                validateNoDroolsAlphaNetworkCompilation();
                validateNoGizmoKieBaseSupplier();
            }
            scoreDirectorFactory = constraintStreamScoreDirectorFactory;
        } else if (incrementalScoreDirectorFactory != null) {
            validateNoDroolsAlphaNetworkCompilation();
            validateNoGizmoKieBaseSupplier();
            scoreDirectorFactory = incrementalScoreDirectorFactory;
        } else if (droolsScoreDirectorFactory != null) {
            scoreDirectorFactory = droolsScoreDirectorFactory;
        } else {
            throw new IllegalArgumentException("The scoreDirectorFactory lacks a configuration for an "
                    + "easyScoreCalculatorClass, a constraintProviderClass, an incrementalScoreCalculatorClass or a droolsScoreDirectorFactory.");
        }

        return scoreDirectorFactory;
    }

    private void checkMultipleScoreDirectorFactoryTypes(EasyScoreDirectorFactory easyScoreDirectorFactory,
            AbstractConstraintStreamScoreDirectorFactory constraintStreamScoreDirectorFactory,
            IncrementalScoreDirectorFactory incrementalScoreDirectorFactory,
            DroolsScoreDirectorFactory droolsScoreDirectorFactory) {
        if (Stream.of(easyScoreDirectorFactory, constraintStreamScoreDirectorFactory,
                incrementalScoreDirectorFactory, droolsScoreDirectorFactory)
                .filter(Objects::nonNull).count() > 1) {
            List<String> scoreDirectorFactoryPropertyList = new ArrayList<>(4);
            if (easyScoreDirectorFactory != null) {
                scoreDirectorFactoryPropertyList
                        .add("an easyScoreCalculatorClass (" + config.getEasyScoreCalculatorClass().getName() + ")");
            }
            if (constraintStreamScoreDirectorFactory != null) {
                scoreDirectorFactoryPropertyList
                        .add("a constraintProviderClass (" + config.getConstraintProviderClass().getName() + ")");
            }
            if (incrementalScoreDirectorFactory != null) {
                scoreDirectorFactoryPropertyList.add(
                        "an incrementalScoreCalculatorClass (" + config.getIncrementalScoreCalculatorClass().getName() + ")");
            }
            if (droolsScoreDirectorFactory != null) {
                String abbreviatedScoreDrlList = ConfigUtils.abbreviate(config.getScoreDrlList());
                String abbreviatedScoreDrlFileList = config.getScoreDrlFileList() == null ? ""
                        : ConfigUtils.abbreviate(config.getScoreDrlFileList()
                                .stream()
                                .map(File::getName)
                                .collect(Collectors.toList()));
                scoreDirectorFactoryPropertyList
                        .add("a scoreDrlList (" + abbreviatedScoreDrlList + ") or a scoreDrlFileList ("
                                + abbreviatedScoreDrlFileList + ")");
            }
            throw new IllegalArgumentException("The scoreDirectorFactory cannot have "
                    + String.join(" and ", scoreDirectorFactoryPropertyList) + " together.");
        }
    }

    private void validateNoDroolsAlphaNetworkCompilation() {
        if (config.getDroolsAlphaNetworkCompilationEnabled() != null) {
            throw new IllegalStateException("If there is no scoreDrl (" + config.getScoreDrlList()
                    + "), scoreDrlFile (" + config.getScoreDrlFileList() + ") or constraintProviderClass ("
                    + config.getConstraintProviderClass() + ") with " + ConstraintStreamImplType.DROOLS + " impl type ("
                    + config.getConstraintStreamImplType() + "), there can be no droolsAlphaNetworkCompilationEnabled ("
                    + config.getDroolsAlphaNetworkCompilationEnabled() + ") either.");
        }
    }

    private void validateNoGizmoKieBaseSupplier() {
        if (config.getGizmoKieBaseSupplier() != null) {
            throw new IllegalStateException("If there is no scoreDrl (" + config.getScoreDrlList()
                    + "), scoreDrlFile (" + config.getScoreDrlFileList() + ") or constraintProviderClass ("
                    + config.getConstraintProviderClass() + ") with " + ConstraintStreamImplType.DROOLS + " impl type ("
                    + config.getConstraintStreamImplType() + "), there can be no gizmoKieBaseSupplier ("
                    + config.getGizmoKieBaseSupplier() + ") either.");
        }
    }

}
