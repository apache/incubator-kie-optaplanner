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

import org.drools.model.Declaration;
import org.drools.model.PatternDSL;

public interface RuleMetadata<FactType> {

    static InferredRuleMetadata ofInferred(Declaration<LogicalTuple> variableDeclaration,
            PatternDSL.PatternDef<LogicalTuple> pattern) {
        return new InferredRuleMetadata(variableDeclaration, pattern);
    }

    static <A> GenuineRuleMetadata<A> of(Declaration<A> variableDeclaration, PatternDSL.PatternDef<A> pattern) {
        return new GenuineRuleMetadata<>(variableDeclaration, pattern);
    }

    Declaration<FactType> getVariableDeclaration();

    PatternDSL.PatternDef<FactType> getPattern();
}
