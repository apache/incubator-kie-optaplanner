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

package org.optaplanner.persistence.minizinc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.optaplanner.persistence.minizinc.parser.ParseException;

public class OptaPlannerFlatZincParserTest {

    @Test
    public void testParseFromFile() throws ParseException {
        FlatZincModel flatZincModel =
                OptaPlannerFlatZincParser.parse(OptaPlannerFlatZincParserTest.class.getResourceAsStream("/model.fzn"));
        assertThat(flatZincModel.toString()).isEqualTo("");
    }
}
