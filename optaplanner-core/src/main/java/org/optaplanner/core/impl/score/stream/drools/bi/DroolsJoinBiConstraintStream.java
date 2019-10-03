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
package org.optaplanner.core.impl.score.stream.drools.bi;

import java.util.Collection;

import org.drools.model.Declaration;
import org.drools.model.PatternDSL;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsAbstractUniConstraintStream;

public class DroolsJoinBiConstraintStream<Solution_, A, B> extends DroolsAbstractBiConstraintStream<Solution_, A, B> {

    private final DroolsAbstractUniConstraintStream<Solution_, A> leftParentStream;
    private final DroolsAbstractUniConstraintStream<Solution_, B> rightParentStream;
    private final AbstractBiJoiner<A, B> biJoiner;
    private final PatternDSL.PatternDef<B> rightPattern;

    public DroolsJoinBiConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent,
            DroolsAbstractUniConstraintStream<Solution_, B> otherStream, BiJoiner<A, B> biJoiner) {
        super(constraintFactory, null);
        this.leftParentStream = parent;
        this.rightParentStream = otherStream;
        this.biJoiner = (AbstractBiJoiner<A, B>) biJoiner;
        this.rightPattern = otherStream.getPattern().expr(getLeftVariableDeclaration(), (b, a) -> matches(a, b));
    }

    @Override
    public DroolsAbstractUniConstraintStream<Solution_, A> getLeftParentStream() {
        return leftParentStream;
    }

    @Override
    public DroolsAbstractUniConstraintStream<Solution_, B> getRightParentStream() {
        return rightParentStream;
    }

    @Override
    public Declaration<A> getLeftVariableDeclaration() {
        return leftParentStream.getVariableDeclaration();
    }

    @Override
    public PatternDSL.PatternDef<A> getLeftPattern() {
        return leftParentStream.getPattern();
    }

    @Override
    public Declaration<B> getRightVariableDeclaration() {
        return rightParentStream.getVariableDeclaration();
    }

    @Override
    public PatternDSL.PatternDef<B> getRightPattern() {
        return rightPattern;
    }

    private static boolean matches(Object left, Object right, JoinerType type) {
        switch (type) {
            case EQUAL:
                return left.equals(right);
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
                return matchesComparable((Comparable)left, right, type);
            case CONTAINING:
                return ((Collection) left).contains(right);
            case DISJOINT:
            case INTERSECTING:
                return matchesCollection((Collection)left, (Collection) right, type);
            default:
                throw new IllegalStateException("Unsupported joiner type (" + type + ").");
        }
    }

    private static boolean matchesComparable(Comparable left, Object right, JoinerType joinerType) {
        int comparison = left.compareTo(right);
        switch (joinerType) {
            case LESS_THAN:
                return comparison < 0;
            case LESS_THAN_OR_EQUAL:
                return comparison <= 0;
            case GREATER_THAN:
                return comparison > 0;
            case GREATER_THAN_OR_EQUAL:
                return comparison >= 0;
            default:
                throw new IllegalStateException("Unsupported joiner type (" + joinerType + ").");
        }
    }

    private static boolean matchesCollection(Collection leftCollection, Collection rightCollection,
            JoinerType joinerType) {
        switch (joinerType) {
            case DISJOINT:
                return leftCollection.stream().noneMatch(rightCollection::contains) &&
                        rightCollection.stream().noneMatch(leftCollection::contains);
            case INTERSECTING:
                return leftCollection.stream().anyMatch(rightCollection::contains) ||
                        rightCollection.stream().anyMatch(leftCollection::contains);
            default:
                throw new IllegalStateException("Unsupported joiner type (" + joinerType + ").");
        }
    }

    private boolean matches(A left, B right) {
        Object[] leftMappings = biJoiner.getLeftCombinedMapping().apply(left);
        Object[] rightMappings = biJoiner.getRightCombinedMapping().apply(right);
        JoinerType[] joinerTypes = biJoiner.getJoinerTypes();
        for (int i = 0; i < leftMappings.length; i++) {
            Object leftMapping = leftMappings[i];
            Object rightMapping = rightMappings[i];
            JoinerType joinerType = joinerTypes[i];
            boolean matches = matches(leftMapping, rightMapping, joinerType);
            if (!matches) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "BiJoin() with " + childStreamList.size()  + " children";
    }

}
