/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.util.function.Supplier;

import org.drools.model.Declaration;
import org.drools.model.PatternDSL;

import static org.drools.model.PatternDSL.pattern;

public interface DroolsMetadata<LogicalFactType, GenuineFactType> {

    static <A> DroolsInferredMetadata<A> ofInferred(Declaration<DroolsLogicalTuple> variableDeclaration,
            Supplier<PatternDSL.PatternDef<DroolsLogicalTuple>> patternBuilder) {
        return ofInferred(variableDeclaration, patternBuilder, 0);
    }

    static <A> DroolsInferredMetadata<A> ofInferred(Declaration<DroolsLogicalTuple> variableDeclaration,
            Supplier<PatternDSL.PatternDef<DroolsLogicalTuple>> patternBuilder, int itemId) {
        return new DroolsInferredMetadata<>(variableDeclaration, patternBuilder, itemId);
    }

    static <A> DroolsGenuineMetadata<A> ofGenuine(Declaration<A> variableDeclaration) {
        return new DroolsGenuineMetadata<>(variableDeclaration, () -> pattern(variableDeclaration));
    }

    GenuineFactType extract(LogicalFactType container);

    Declaration<LogicalFactType> getVariableDeclaration();

    PatternDSL.PatternDef<LogicalFactType> buildPattern();

}
