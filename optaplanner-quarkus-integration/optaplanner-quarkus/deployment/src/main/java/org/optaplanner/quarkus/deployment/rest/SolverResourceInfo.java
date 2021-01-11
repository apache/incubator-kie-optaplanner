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

package org.optaplanner.quarkus.deployment.rest;

public final class SolverResourceInfo {

    private final Class<?> resourceInterface;

    private final Class<?> solutionType;

    private final Class<?> problemIdType;

    private final String resourcePath;

    public SolverResourceInfo(Class<?> resourceInterface, Class<?> solutionType, Class<?> problemIdType) {
        this.resourceInterface = resourceInterface;
        this.solutionType = solutionType;
        this.problemIdType = problemIdType;
        this.resourcePath = resourcePathFromSolutionType(solutionType.getSimpleName());
    }

    public Class<?> getResourceInterface() {
        return resourceInterface;
    }

    public Class<?> getSolutionType() {
        return solutionType;
    }

    public Class<?> getProblemIdType() {
        return problemIdType;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    private String resourcePathFromSolutionType(String solutionType) {
        char[] simpleNameChars = solutionType.toCharArray();
        simpleNameChars[0] = Character.toLowerCase(simpleNameChars[0]);
        return new String(simpleNameChars);
    }
}
