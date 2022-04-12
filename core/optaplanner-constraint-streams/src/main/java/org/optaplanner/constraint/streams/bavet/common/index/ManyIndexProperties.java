/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.score.stream.JoinerType;

final class ManyIndexProperties implements IndexProperties {

    private final List<Object> properties;

    private ManyIndexProperties(List<Object> properties) {
        this.properties = properties;
    }

    ManyIndexProperties(Object... properties) {
        this.properties = Arrays.asList(properties);
    }

    @Override
    public int getLength() {
        return properties.size();
    }

    @Override
    public <Type_> Type_ getProperty(int index) {
        return (Type_) properties.get(index);
    }

    public static final class Builder {

        private final List<Object> finishedChunks = new ArrayList<>(0);
        private Chunk currentChunk = null;

        void addValue(JoinerType joinerType, Object value) {
            switch (joinerType) {
                case EQUAL:
                    currentChunk = currentChunk == null ? new Chunk() : currentChunk;
                    currentChunk.addValue(value);
                case LESS_THAN_OR_EQUAL:
                case LESS_THAN:
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                    if (currentChunk != null) {
                        finishedChunks.add(currentChunk.properties);
                        currentChunk = null;
                    }
                    finishedChunks.add(value);
                default:
            }
        }

        ManyIndexProperties build() {
            if (currentChunk != null) {
                finishedChunks.add(currentChunk.properties);
                currentChunk = null;
            }
            return new ManyIndexProperties(finishedChunks);
        }

    }

    public static final class Chunk {

        private int count;
        private Object properties;

        void addValue(Object value) {
            if (count == 0) {
                properties = value;
            } else {
                if (count == 1) {
                    Object property = properties;
                    properties = new ArrayList<>(2);
                    ((List<Object>) properties).add(property);
                }
                ((List<Object>) properties).add(value);
            }
            count += 1;
        }

    }

    @Override
    public String toString() {
        return properties.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
