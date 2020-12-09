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

package org.optaplanner.core.impl.score.stream.bavet.uni;

public final class BavetFilterUniTuple<A> extends BavetAbstractUniTuple<A> {

    private final BavetFilterUniNode<A> node;
    private final BavetAbstractUniTuple<A> parentTuple;

    public BavetFilterUniTuple(BavetFilterUniNode<A> node, BavetAbstractUniTuple<A> parentTuple) {
        this.node = node;
        this.parentTuple = parentTuple;
    }

    @Override
    public String toString() {
        return "Filter(" + getFactsString() + ") with " + childTupleList.size() + " children";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public BavetFilterUniNode<A> getNode() {
        return node;
    }

    @Override
    public A getFactA() {
        return parentTuple.getFactA();
    }

}
