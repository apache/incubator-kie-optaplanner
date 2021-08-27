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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.impl.domain.valuerange.buildin.primint.IntValueRange;
import org.optaplanner.persistence.minizinc.model.FlatZincAnnotation;
import org.optaplanner.persistence.minizinc.model.FlatZincArray;
import org.optaplanner.persistence.minizinc.model.FlatZincConstraint;
import org.optaplanner.persistence.minizinc.model.FlatZincExpr;
import org.optaplanner.persistence.minizinc.model.FlatZincGoal;
import org.optaplanner.persistence.minizinc.model.FlatZincPlanningVariable;
import org.optaplanner.persistence.minizinc.model.FlatZincPredicate;
import org.optaplanner.persistence.minizinc.model.FlatZincSolve;
import org.optaplanner.persistence.minizinc.model.TypeNameValue;
import org.optaplanner.persistence.minizinc.parser.ParseException;

public class OptaPlannerFlatZincParserTest {

    @Test
    public void testParseFromFile() throws ParseException {
        List<FlatZincPredicate> expectedPredicateList = List.of();
        List<TypeNameValue> expectedParameterList = List.of(new TypeNameValue(int[].class, "X_INTRODUCED_0_",
                new FlatZincArray(List.of(new FlatZincExpr(1), new FlatZincExpr(-1)))));
        ValueRange colors = new IntValueRange(1, 4);
        FlatZincAnnotation outputVarAnno = new FlatZincAnnotation("output_var", List.of());
        List<FlatZincPlanningVariable> expectedPlanningVariables =
                List.of(new FlatZincPlanningVariable(colors, "wa", List.of(outputVarAnno)),
                        new FlatZincPlanningVariable(colors, "nt", List.of(outputVarAnno)),
                        new FlatZincPlanningVariable(colors, "sa", List.of(outputVarAnno)),
                        new FlatZincPlanningVariable(colors, "q", List.of(outputVarAnno)),
                        new FlatZincPlanningVariable(colors, "nsw", List.of(outputVarAnno)),
                        new FlatZincPlanningVariable(colors, "v", List.of(outputVarAnno)),
                        new FlatZincPlanningVariable(colors, "t", List.of(outputVarAnno)));
        FlatZincExpr parameterName = new FlatZincExpr("X_INTRODUCED_0_");
        FlatZincExpr zero = new FlatZincExpr(0);
        FlatZincExpr wa = new FlatZincExpr("wa");
        FlatZincExpr nt = new FlatZincExpr("nt");
        FlatZincExpr sa = new FlatZincExpr("sa");
        FlatZincExpr q = new FlatZincExpr("q");
        FlatZincExpr nsw = new FlatZincExpr("nsw");
        FlatZincExpr v = new FlatZincExpr("v");
        FlatZincExpr t = new FlatZincExpr("t");
        List<FlatZincConstraint> expectedConstraints = List.of(new FlatZincConstraint("int_lin_ne",
                List.of(parameterName,
                        new FlatZincArray(List.of(wa, nt)),
                        zero),
                List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(wa, sa)),
                                zero),
                        List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(nt, sa)),
                                zero),
                        List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(nt, q)),
                                zero),
                        List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(sa, q)),
                                zero),
                        List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(sa, nsw)),
                                zero),
                        List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(sa, v)),
                                zero),
                        List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(q, nsw)),
                                zero),
                        List.of()),
                new FlatZincConstraint("int_lin_ne",
                        List.of(parameterName,
                                new FlatZincArray(List.of(nsw, v)),
                                zero),
                        List.of()));
        FlatZincSolve expectedSolve = new FlatZincSolve(List.of(), FlatZincGoal.SATISFY, null);
        FlatZincModel expected = new FlatZincModel(expectedPredicateList, expectedParameterList, expectedPlanningVariables,
                expectedConstraints, expectedSolve);
        FlatZincModel actual =
                OptaPlannerFlatZincParser.parse(OptaPlannerFlatZincParserTest.class.getResourceAsStream("/model.fzn"));

        // Need to use recursive comparison since ValueRange do not typically implement hashCode/equals
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
