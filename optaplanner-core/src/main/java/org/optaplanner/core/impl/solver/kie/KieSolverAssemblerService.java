/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.solver.kie;

import org.kie.api.internal.assembler.KieAssemblerService;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;

/**
 * Only called for solver configuration files in a kjar that have the {@link ResourceType#SOLVER} extension.
 */
public class KieSolverAssemblerService implements KieAssemblerService {

    public KieSolverAssemblerService() {
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.SOLVER;
    }

    @Override
    public void addResource(Object kbuilder, Resource resource, ResourceType type, ResourceConfiguration configuration) {
        // Do nothing for now
    }
}
