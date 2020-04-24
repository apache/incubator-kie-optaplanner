/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.test.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptaPlannerExtension implements ParameterResolver {

    private static final Logger logger = LoggerFactory.getLogger(OptaPlannerExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(ConstraintVerifier.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        logger.info("@{}", extensionContext.getElement());
        // TODO support both test method and constructor parameters (consult SpringExtension)
        Class<?> providerClass = (Class<?>) ((ParameterizedType) parameterContext.getParameter().getParameterizedType())
                .getActualTypeArguments()[0];
        logger.info("==={}", providerClass);
        ConstraintProvider constraintProvider = null;
        try {
            constraintProvider = (ConstraintProvider) providerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            logger.error("Wrong constraint provider: {}", providerClass);
        }

        Class<?> testClass = extensionContext.getRequiredTestClass();
        ConstraintProviderTest annotation = testClass.getAnnotation(ConstraintProviderTest.class);
        // TODO annotation not null
        // TODO if (solution == void || entities == {}) => solverConfig
        String solverConfigFile = annotation.solverConfig();
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(solverConfigFile);
        return ConstraintVerifier.build(constraintProvider, solverConfig);
        // TODO else return build(cp, solution, entities);
    }
}
