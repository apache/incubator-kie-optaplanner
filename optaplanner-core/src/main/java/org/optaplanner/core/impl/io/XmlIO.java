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

package org.optaplanner.core.impl.io;

import java.io.Reader;
import java.io.Writer;

/**
 * Generic XML read/write facility.
 */
public interface XmlIO<T> {

    /**
     * Reads input XML.
     *
     * @param reader never null
     * @return generic type which acts as a root element in the input XML
     */
    T read(Reader reader);

    /**
     * Writes an object of a generic type to XML.
     *
     * @param root a root object of the graph to serialize to XML
     * @param writer writer never null
     */
    void write(T root, Writer writer);
}
