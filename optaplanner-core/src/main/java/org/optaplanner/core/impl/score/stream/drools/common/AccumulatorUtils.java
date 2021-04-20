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

package org.optaplanner.core.impl.score.stream.drools.common;

import org.drools.core.rule.Declaration;
import org.drools.core.spi.Tuple;

final class AccumulatorUtils {

    static class OffsetHolder {
        public int offset;
    }

    public static Tuple findTupleAndOffset(Tuple tuple, OffsetHolder holder, Declaration declr) {
        int offset = 0;
        while (tuple.getIndex() != declr.getTupleIndex()) {
            tuple = tuple.getParent();
            offset++;
        }
        holder.offset = offset;
        return tuple;
    }

    public static Tuple getTuple(int deltaOffset, Tuple tuple) {
        switch (deltaOffset) {
            case 0:
                return tuple;
            case 1:
                return tuple.getParent();
            case 2:
                return tuple.getParent()
                        .getParent();
            case 3:
                return tuple.getParent()
                        .getParent()
                        .getParent();
            case 4:
                return tuple.getParent()
                        .getParent()
                        .getParent()
                        .getParent();
            default:
                throw new UnsupportedOperationException("Impossible state: tuple delta offset (" + deltaOffset + ").");
        }
    }

}
