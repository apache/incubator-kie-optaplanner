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

package org.optaplanner.core.impl.util;

import java.util.Objects;

public class Break<PointType_, DifferenceType_> {
    final PointType_ beforeItem;
    final PointType_ afterItem;
    final DifferenceType_ length;

    public Break(PointType_ beforeItem, PointType_ afterItem, DifferenceType_ length) {
        this.beforeItem = beforeItem;
        this.afterItem = afterItem;
        this.length = length;
    }

    public PointType_ getBeforeItem() {
        return beforeItem;
    }

    public PointType_ getAfterItem() {
        return afterItem;
    }

    public DifferenceType_ getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Break<?, ?> aBreak = (Break<?, ?>) o;
        return Objects.equals(beforeItem, aBreak.beforeItem) && Objects.equals(afterItem, aBreak.afterItem)
                && Objects.equals(length, aBreak.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beforeItem, afterItem, length);
    }

    @Override
    public String toString() {
        return "Break{" +
                "beforeItem=" + beforeItem +
                ", afterItem=" + afterItem +
                ", length=" + length +
                '}';
    }
}
