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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;

final class QuadExistenceMutator<A, B, C, D, E> implements Mutator {

    private final boolean shouldExist;
    private final Class<E> otherFactType;
    private final AbstractPentaJoiner<A, B, C, D, E>[] joiners;

    public QuadExistenceMutator(AbstractConstraintModelJoiningNode<E, AbstractPentaJoiner<A, B, C, D, E>> node,
            boolean shouldExist) {
        this.shouldExist = shouldExist;
        this.otherFactType = node.getOtherFactType();
        this.joiners = node.get().stream()
                .toArray(AbstractPentaJoiner[]::new);
    }

    @Override
    public AbstractRuleAssembler apply(AbstractRuleAssembler ruleAssembler) {
        if (shouldExist) {
            return new QuadRuleAssembler(((QuadLeftHandSide) ruleAssembler.leftHandSide).exists(otherFactType, joiners));
        } else {
            return new QuadRuleAssembler(((QuadLeftHandSide) ruleAssembler.leftHandSide).notExists(otherFactType, joiners));
        }
    }

}
