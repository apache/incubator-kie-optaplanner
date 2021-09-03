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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.placer.QueuedEntityPlacerConfig;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.persistence.minizinc.backend.gizmo.CompiledModelData;
import org.optaplanner.persistence.minizinc.backend.gizmo.ModelBytecodeGenerator;
import org.optaplanner.persistence.minizinc.model.FlatZincArray;
import org.optaplanner.persistence.minizinc.model.FlatZincConstraint;
import org.optaplanner.persistence.minizinc.model.FlatZincExpr;
import org.optaplanner.persistence.minizinc.model.FlatZincPlanningVariable;
import org.optaplanner.persistence.minizinc.model.TypeNameValue;
import org.optaplanner.persistence.minizinc.parser.ParseException;

public class OptaPlannerFlatZincCompiler {

    public static String solve(InputStream flatZincInputStream) throws ParseException {
        FlatZincModel flatZincModel = OptaPlannerFlatZincParser.parse(flatZincInputStream);
        CompiledModelData optaplannerModel = compileModel(flatZincModel);
        SolverConfig solverConfig = new SolverConfig();
        solverConfig.setClassLoader(optaplannerModel.getClassLoader());
        solverConfig.setSolutionClass(optaplannerModel.getPlanningSolutionClass());
        solverConfig.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig()
                .withConstraintProviderClass(optaplannerModel.getConstraintProviderClass()));
        solverConfig.setEntityClassList(optaplannerModel.getPlanningEntityClasses());
        solverConfig.setTerminationConfig(new TerminationConfig().withBestScoreLimit("0hard/0soft"));
        solverConfig.setPhaseConfigList(new ArrayList<>());

        for (Class<?> entityClass : optaplannerModel.getPlanningEntityClasses()) {
            ConstructionHeuristicPhaseConfig chPhaseConfig = new ConstructionHeuristicPhaseConfig();
            QueuedEntityPlacerConfig entityPlacerConfig = new QueuedEntityPlacerConfig();
            EntitySelectorConfig entitySelectorConfig = new EntitySelectorConfig();
            entityPlacerConfig.setEntitySelectorConfig(entitySelectorConfig);
            entitySelectorConfig.setEntityClass(entityClass);
            chPhaseConfig.setEntityPlacerConfig(entityPlacerConfig);
            solverConfig.getPhaseConfigList().add(chPhaseConfig);
        }
        solverConfig.getPhaseConfigList().add(new LocalSearchPhaseConfig());

        SolverFactory<Object> solverFactory = SolverFactory.create(solverConfig);
        Object out = solverFactory.buildSolver().solve(optaplannerModel.getCompiledProblem());
        return out.toString();
    }

    private static CompiledModelData compileModel(FlatZincModel model) {
        FlatZincModel simplified = simplifyModel(model);
        return ModelBytecodeGenerator.createOptaPlannerModelFromFlatZincModel(simplified);
    }

    private static FlatZincModel simplifyModel(FlatZincModel original) {
        List<TypeNameValue> newParameterList = Collections.emptyList();
        Map<String, FlatZincExpr> parameterNameToValue = new HashMap<>();
        for (TypeNameValue typeNameValue : original.getParameterList()) {
            parameterNameToValue.put(typeNameValue.getName(),
                    simplifyExpression(typeNameValue.getValue(), parameterNameToValue));
        }
        List<FlatZincPlanningVariable> newPlanningVariableList = new ArrayList<>(original.getPlanningVariableList().size());
        for (FlatZincPlanningVariable flatZincPlanningVariable : original.getPlanningVariableList()) {
            if (flatZincPlanningVariable.getDefaultValue().isPresent()) {
                newPlanningVariableList.add(new FlatZincPlanningVariable(flatZincPlanningVariable.getValueRange(),
                        flatZincPlanningVariable.getName(),
                        flatZincPlanningVariable.getAnnotationList(),
                        simplifyExpression(flatZincPlanningVariable.getDefaultValue().get(),
                                parameterNameToValue)));
            } else {
                newPlanningVariableList.add(flatZincPlanningVariable);
            }
        }
        List<FlatZincConstraint> newConstraintList = new ArrayList<>(original.getConstraintList().size());
        for (FlatZincConstraint constraint : original.getConstraintList()) {
            List<FlatZincExpr> predicateArgumentList = new ArrayList<>(constraint.getPredicateArguments().size());
            for (FlatZincExpr predicateArgument : constraint.getPredicateArguments()) {
                predicateArgumentList.add(simplifyExpression(predicateArgument, parameterNameToValue));
            }
            newConstraintList.add(new FlatZincConstraint(constraint.getPredicateName(), predicateArgumentList,
                    constraint.getAnnotationList()));
        }
        return new FlatZincModel(original.getPredicateList(), newParameterList, newPlanningVariableList, newConstraintList,
                original.getSolveGoal());
    }

    private static FlatZincExpr simplifyExpression(FlatZincExpr original, Map<String, FlatZincExpr> parameterNameToValue) {
        if (original.isVariable() && parameterNameToValue.containsKey(original.asVariable())) {
            return parameterNameToValue.get(original.asVariable());
        } else if (original instanceof FlatZincArray) {
            FlatZincArray originalArray = (FlatZincArray) original;
            List<FlatZincExpr> simplifiedItemList = new ArrayList<>(originalArray.getItems().size());
            for (FlatZincExpr flatZincExpr : originalArray.getItems()) {
                simplifiedItemList.add(simplifyExpression(flatZincExpr, parameterNameToValue));
            }
            return new FlatZincArray(simplifiedItemList);
        }
        return original;
    }
}
