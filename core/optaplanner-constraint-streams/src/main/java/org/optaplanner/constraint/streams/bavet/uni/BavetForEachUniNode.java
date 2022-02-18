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

package org.optaplanner.constraint.streams.bavet.uni;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.constraint.streams.bavet.BavetConstraintSession;
import org.optaplanner.constraint.streams.bavet.common.BavetAbstractTuple;
import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;

public final class BavetForEachUniNode<A> extends BavetAbstractUniNode<A> {

    private final Class<A> forEachClass;

    private List<BavetAbstractUniNode<A>> childNodeList = new ArrayList<>();

    public BavetForEachUniNode(BavetConstraintSession session, int nodeIndex,
            Class<A> forEachClass) {
        super(session, nodeIndex);
        this.forEachClass = forEachClass;
    }

    @Override
    public void addChildNode(BavetAbstractUniNode<A> childNode) {
        childNodeList.add(childNode);
    }

    @Override
    public List<BavetAbstractUniNode<A>> getChildNodeList() {
        return childNodeList;
    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    @Override
    public int hashCode() {
        return forEachClass.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof BavetForEachUniNode) {
            BavetForEachUniNode<?> other = (BavetForEachUniNode<?>) o;
            return forEachClass.equals(other.forEachClass);
        } else {
            return false;
        }
    }

    // ************************************************************************
    // Runtime
    // ************************************************************************

    public BavetForEachUniTuple<A> createTuple(A a) {
        return new BavetForEachUniTuple<>(this, a, childNodeList.size());
    }

    @Override
    public BavetAbstractUniTuple<A> createTuple(BavetAbstractUniTuple<A> parentTuple) {
        throw new IllegalStateException("The fromUniNode (" + getClass().getSimpleName()
                + ") can't have a parentTuple (" + parentTuple + ");");
    }

    @Override
    public void refresh(BavetAbstractTuple uncastTuple) {
        BavetForEachUniTuple<A> tuple = (BavetForEachUniTuple<A>) uncastTuple;
        List<BavetAbstractTuple> childTupleList = tuple.getChildTupleList();
        for (BavetAbstractTuple childTuple : childTupleList) {
            // TODO the entire FromUniNode isn't really doing anything
            // so the destruction/construction is just an update op unless it's CREATING or DYING
            childTuple.getNode().transitionTuple(childTuple, BavetTupleState.DYING);
        }
        childTupleList.clear();
        if (tuple.isActive()) {
            for (BavetAbstractUniNode<A> childNode : childNodeList) {
                BavetAbstractUniTuple<A> childTuple = childNode.createTuple(tuple);
                childTupleList.add(childTuple);
                childNode.transitionTuple(childTuple, BavetTupleState.CREATING);
            }
        }
    }

    @Override
    public String toString() {
        return "From(" + forEachClass.getSimpleName() + ") with " + childNodeList.size() + " children";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

}
