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

import java.util.function.Supplier;

import org.drools.ancompiler.KieBaseUpdaterANC;
import org.kie.api.KieBase;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;

/**
 * Exists so that ANC references can be decoupled from the base CS functionality,
 * and therefore ANC can be removed from the classpath entirely if necessary.
 *
 * @param <Solution_>
 * @param <Score_>
 */
public final class DroolsAncConstraintStreamScoreDirectorFactory<Solution_, Score_ extends Score<Score_>>
        extends DroolsConstraintStreamScoreDirectorFactory<Solution_, Score_> {

    public DroolsAncConstraintStreamScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            ConstraintProvider constraintProvider) {
        super(solutionDescriptor, buildKieBase(solutionDescriptor, constraintProvider));
    }

    public DroolsAncConstraintStreamScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            Supplier<KieBase> kieBaseDescriptorSupplier) {
        super(solutionDescriptor, kieBaseDescriptorSupplier);
    }

    public static <Solution_> KieBaseDescriptor<Solution_> buildKieBase(SolutionDescriptor<Solution_> solutionDescriptor,
            ConstraintProvider constraintProvider) {
        KieBaseDescriptor<Solution_> kieBaseDescriptor =
                DroolsConstraintStreamScoreDirectorFactory.buildKieBase(solutionDescriptor, constraintProvider);
        KieBaseUpdaterANC.generateAndSetInMemoryANC(kieBaseDescriptor.get());
        return kieBaseDescriptor;
    }

}
