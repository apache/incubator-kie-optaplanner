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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.optaplanner.persistence.minizinc.parser.ParseException;

public class OptaPlannerFlatZincCompilerTest {

    @Test
    @Disabled("Generated ConstraintProvider does not match constraints")
    public void testThreeColor() throws ParseException {
        String solution = OptaPlannerFlatZincCompiler
                .solve(OptaPlannerFlatZincCompilerTest.class.getResourceAsStream("/three-color-model.fzn"));
        Map<String, String> parsedSolution = parseSolution(solution);
        String wa = parsedSolution.get("wa");
        String nt = parsedSolution.get("nt");
        String sa = parsedSolution.get("sa");
        String q = parsedSolution.get("q");
        String nsw = parsedSolution.get("nsw");
        String v = parsedSolution.get("v");
        String t = parsedSolution.get("t");

        System.out.println(solution);
        assertThat(List.of(wa, nt, sa, q, nsw, v, t)).describedAs("all initialized")
                .allSatisfy(variable -> assertThat(variable).isNotNull());

        assertThat(wa).describedAs("wa != nt").isNotEqualTo(nt);
        assertThat(wa).describedAs("wa != sa").isNotEqualTo(sa);
        assertThat(nt).describedAs("nt != sa").isNotEqualTo(sa);
        assertThat(nt).describedAs("nt != q").isNotEqualTo(q);
        assertThat(sa).describedAs("sa != q").isNotEqualTo(q);
        assertThat(sa).describedAs("sa != nsw").isNotEqualTo(nsw);
        assertThat(sa).describedAs("sa != v").isNotEqualTo(v);
        assertThat(q).describedAs("q != nsw").isNotEqualTo(nsw);
        assertThat(nsw).describedAs("nsw != v").isNotEqualTo(v);
    }

    private Map<String, String> parseSolution(String solution) {
        Map<String, String> out = new HashMap<>();
        for (String line : solution.split("\n")) {
            String[] parts = line.split(" = ");
            out.put(parts[0], parts[1]);
        }
        return out;
    }

}
