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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.impl.domain.valuerange.buildin.bigdecimal.BigDecimalValueRange;
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
    public void testThreeColor() throws ParseException {
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
                OptaPlannerFlatZincParser
                        .parse(OptaPlannerFlatZincParserTest.class.getResourceAsStream("/three-color-model.fzn"));

        // Need to use recursive comparison since ValueRange do not typically implement hashCode/equals
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    public void testLaplace() throws ParseException {
        List<FlatZincPredicate> expectedPredicateList = List.of();
        List<TypeNameValue> expectedParameterList = List.of(new TypeNameValue(BigDecimal[].class, "X_INTRODUCED_29_",
                new FlatZincArray(List.of(new FlatZincExpr(new BigDecimal("4.0")), new FlatZincExpr(new BigDecimal("-1.0")),
                        new FlatZincExpr(new BigDecimal("-1.0")), new FlatZincExpr(new BigDecimal("-1.0")),
                        new FlatZincExpr(new BigDecimal("-1.0"))))));
        ValueRange optimizedToConstant0ValueRange = new BigDecimalValueRange(new BigDecimal("0.0"), new BigDecimal("0.1"));
        ValueRange optimizedToConstant100ValueRange =
                new BigDecimalValueRange(new BigDecimal("100.0"), new BigDecimal("100.1"));
        ValueRange floatValueRange =
                new BigDecimalValueRange(BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.valueOf(Long.MAX_VALUE));
        FlatZincAnnotation outputArrayAnno = new FlatZincAnnotation("output_array",
                List.of(new FlatZincExpr(new int[] { 0, 1, 2, 3, 4 }), new FlatZincExpr(new int[] { 0, 1, 2, 3, 4 })));
        List<FlatZincExpr> planningVariableExpr = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            planningVariableExpr.add(new FlatZincExpr("X_INTRODUCED_" + i + "_"));
        }
        List<FlatZincPlanningVariable> expectedPlanningVariables =
                List.of(new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_0_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant100ValueRange, "X_INTRODUCED_1_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant100ValueRange, "X_INTRODUCED_2_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant100ValueRange, "X_INTRODUCED_3_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_4_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_5_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_6_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_7_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_8_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_9_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_10_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_11_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_12_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_13_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_14_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_15_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_16_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_17_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "X_INTRODUCED_18_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_19_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_20_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_21_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_22_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_23_", List.of()),
                        new FlatZincPlanningVariable(optimizedToConstant0ValueRange, "X_INTRODUCED_24_", List.of()),
                        new FlatZincPlanningVariable(floatValueRange, "t", List.of(outputArrayAnno),
                                new FlatZincArray(planningVariableExpr)));

        FlatZincExpr parameterName = new FlatZincExpr("X_INTRODUCED_29_");
        FlatZincExpr zero = new FlatZincExpr(new BigDecimal("-0.0"));
        List<FlatZincConstraint> expectedConstraints = List.of(new FlatZincConstraint("float_lin_eq",
                List.of(parameterName,
                        new FlatZincArray(List.of(planningVariableExpr.get(6), planningVariableExpr.get(11),
                                planningVariableExpr.get(5), planningVariableExpr.get(1), planningVariableExpr.get(7))),
                        zero),
                List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(7), planningVariableExpr.get(12),
                                        planningVariableExpr.get(6), planningVariableExpr.get(2), planningVariableExpr.get(8))),
                                zero),
                        List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(8), planningVariableExpr.get(13),
                                        planningVariableExpr.get(7), planningVariableExpr.get(3), planningVariableExpr.get(9))),
                                zero),
                        List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(11), planningVariableExpr.get(16),
                                        planningVariableExpr.get(10), planningVariableExpr.get(6),
                                        planningVariableExpr.get(12))),
                                zero),
                        List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(12), planningVariableExpr.get(17),
                                        planningVariableExpr.get(11), planningVariableExpr.get(7),
                                        planningVariableExpr.get(13))),
                                zero),
                        List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(13), planningVariableExpr.get(18),
                                        planningVariableExpr.get(12), planningVariableExpr.get(8),
                                        planningVariableExpr.get(14))),
                                zero),
                        List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(16), planningVariableExpr.get(21),
                                        planningVariableExpr.get(15), planningVariableExpr.get(11),
                                        planningVariableExpr.get(17))),
                                zero),
                        List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(17), planningVariableExpr.get(22),
                                        planningVariableExpr.get(16), planningVariableExpr.get(12),
                                        planningVariableExpr.get(18))),
                                zero),
                        List.of()),
                new FlatZincConstraint("float_lin_eq",
                        List.of(parameterName,
                                new FlatZincArray(List.of(planningVariableExpr.get(18), planningVariableExpr.get(23),
                                        planningVariableExpr.get(17), planningVariableExpr.get(13),
                                        planningVariableExpr.get(19))),
                                zero),
                        List.of()));
        FlatZincSolve expectedSolve = new FlatZincSolve(List.of(), FlatZincGoal.SATISFY, null);
        FlatZincModel expected = new FlatZincModel(expectedPredicateList, expectedParameterList, expectedPlanningVariables,
                expectedConstraints, expectedSolve);
        FlatZincModel actual =
                OptaPlannerFlatZincParser.parse(OptaPlannerFlatZincParserTest.class.getResourceAsStream("/laplace.fzn"));

        // Need to use recursive comparison since ValueRange do not typically implement hashCode/equals
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
