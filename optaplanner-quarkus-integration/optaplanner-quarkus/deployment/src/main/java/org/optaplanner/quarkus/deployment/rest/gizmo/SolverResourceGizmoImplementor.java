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

package org.optaplanner.quarkus.deployment.rest.gizmo;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.core.MediaType;

import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.quarkus.deployment.rest.SolverResourceImplementor;
import org.optaplanner.quarkus.deployment.rest.SolverResourceInfo;
import org.optaplanner.quarkus.remote.repository.SolutionRepository;
import org.optaplanner.quarkus.remote.rest.SolverResource;

import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FunctionCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

public class SolverResourceGizmoImplementor extends AbstractResourceGizmoImplementor implements SolverResourceImplementor {

    private final static String PROBLEM_ID_PATH_PARAM = "id";

    private final ClassOutput classOutput;

    public SolverResourceGizmoImplementor(ClassOutput classOutput) {
        this.classOutput = classOutput;
    }

    @Override
    public void implementSolverResource(SolverResourceInfo solverResourceInfo) {
        ClassCreator classCreator = ClassCreator
                .builder()
                .className(getResourceName(solverResourceInfo))
                .interfaces(SolverResource.class)
                .classOutput(classOutput)
                .build();

        addClassAnnotations(classCreator, solverResourceInfo);

        injectBean(classCreator, SolverManager.class);
        injectBean(classCreator, ScoreManager.class);
        injectBean(classCreator, SolutionRepository.class);

        solve(classCreator, solverResourceInfo);
        getSolverStatus(classCreator, solverResourceInfo);
        stopSolving(classCreator, solverResourceInfo);
        getSolution(classCreator, solverResourceInfo);

        classCreator.close();
    }

    /**
     * <pre>
     * &#64;Path("solutionType")
     * &#64;Produces(MediaType.APPLICATION_JSON)
     * &#64;Consumes(MediaType.APPLICATION_JSON)
     * public class SolverResource {...}
     * </pre>
     */
    private void addClassAnnotations(ClassCreator classCreator, SolverResourceInfo solverResourceInfo) {
        addPathAnnotation(classCreator, solverResourceInfo.getResourcePath());
        addConsumesAnnotation(classCreator, MediaType.APPLICATION_JSON);
        addProducesAnnotation(classCreator, MediaType.APPLICATION_JSON);
    }

    /**
     * <pre>
     * &#64;POST
     * &#64;Path("solve/{id}")
     * public void solve(&#64;PathParam("id") ProblemId problemId, Solution solution) {
     *     solverManager.solveAndListen(problemId,
     *             problemId -> solution,
     *             solution -> solutionRepository.put(problemId, solution));
     * }
     * </pre>
     */
    private void solve(ClassCreator classCreator, SolverResourceInfo solverResourceInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "solve", void.class, solverResourceInfo.getProblemIdType(),
                solverResourceInfo.getSolutionType());
        addPOSTAnnotation(methodCreator);
        addPathAnnotation(methodCreator, "solve/{" + PROBLEM_ID_PATH_PARAM + "}");
        addPathParamAnnotation(methodCreator.getParameterAnnotations(0), PROBLEM_ID_PATH_PARAM);

        ResultHandle problemId = methodCreator.getMethodParam(0);
        ResultHandle solution = methodCreator.getMethodParam(1);

        ResultHandle problemFinder = problemFinder(methodCreator, solution);
        ResultHandle bestSolutionConsumer = bestSolutionConsumer(methodCreator, problemId);
        MethodDescriptor solveAndLister = SolverManagerDescriptor.solveAndListen();
        ResultHandle solverManager = getSolveManagerInstance(methodCreator);
        methodCreator.invokeInterfaceMethod(solveAndLister, solverManager, problemId, problemFinder,
                bestSolutionConsumer);

        methodCreator.returnValue(methodCreator.loadNull());
    }

    /**
     * <pre>
     * problemId -> solution
     * </pre>
     */
    private ResultHandle problemFinder(MethodCreator methodCreator, ResultHandle solution) {
        FunctionCreator problemFinderCreator = methodCreator.createFunction(Function.class);
        BytecodeCreator problemFinderByteCode = problemFinderCreator.getBytecode();
        problemFinderByteCode.returnValue(solution);
        return problemFinderCreator.getInstance();
    }

    /**
     * <pre>
     * solution -> solutionRepository.put(problemId, solution)
     * </pre>
     */
    private ResultHandle bestSolutionConsumer(MethodCreator methodCreator, ResultHandle problemId) {
        FunctionCreator bestSolutionConsumerCreator = methodCreator.createFunction(Consumer.class);
        BytecodeCreator bestSolutionConsumerBytecodeCreator = bestSolutionConsumerCreator.getBytecode();

        ResultHandle solution = bestSolutionConsumerBytecodeCreator.getMethodParam(0);
        ResultHandle solutionRepository = getImplementedFieldInstanceByType(bestSolutionConsumerBytecodeCreator,
                SolutionRepository.class, methodCreator.getThis());

        bestSolutionConsumerBytecodeCreator.invokeInterfaceMethod(SolutionRepositoryDescriptor.save(), solutionRepository,
                problemId, solution);
        bestSolutionConsumerBytecodeCreator.returnValue(bestSolutionConsumerBytecodeCreator.loadNull());
        return bestSolutionConsumerCreator.getInstance();
    }

    /**
     * <pre>
     * &#64;GET
     * &#64;Path("status/{id}")
     * public SolverStatus getSolverStatus(&#64;PathParam("id") ProblemId problemId) {
     *     return solverManager.getSolverStatus(problemId);
     * }
     * </pre>
     */
    private void getSolverStatus(ClassCreator classCreator, SolverResourceInfo solverResourceInfo) {
        MethodCreator methodCreator =
                getMethodCreator(classCreator, "getSolverStatus", SolverStatus.class, solverResourceInfo.getProblemIdType());
        addGETAnnotation(methodCreator);
        addPathAnnotation(methodCreator, "status/{" + PROBLEM_ID_PATH_PARAM + "}");
        addPathParamAnnotation(methodCreator.getParameterAnnotations(0), PROBLEM_ID_PATH_PARAM);

        ResultHandle problemId = methodCreator.getMethodParam(0);
        ResultHandle solverManager = getSolveManagerInstance(methodCreator);
        ResultHandle solverStatus =
                methodCreator.invokeInterfaceMethod(SolverManagerDescriptor.getSolverStatus(), solverManager, problemId);
        methodCreator.returnValue(solverStatus);
    }

    /**
     * <pre>
     * &#64;POST
     * &#64;Path("stopSolving/{id}")
     * public void stopSolving(ProblemId problemId) {
     *     solverManager.terminateEarly(problemId);
     * }
     * </pre>
     */
    private void stopSolving(ClassCreator classCreator, SolverResourceInfo solverResourceInfo) {
        MethodCreator methodCreator =
                getMethodCreator(classCreator, "stopSolving", void.class, solverResourceInfo.getProblemIdType());
        addPOSTAnnotation(methodCreator);
        addPathAnnotation(methodCreator, "stopSolving/{" + PROBLEM_ID_PATH_PARAM + "}");
        addPathParamAnnotation(methodCreator.getParameterAnnotations(0), PROBLEM_ID_PATH_PARAM);

        ResultHandle problemId = methodCreator.getMethodParam(0);
        ResultHandle solverManager = getSolveManagerInstance(methodCreator);
        ResultHandle stopSolving =
                methodCreator.invokeInterfaceMethod(SolverManagerDescriptor.terminateEarly(), solverManager, problemId);
        methodCreator.returnValue(stopSolving);
    }

    /**
     * <pre>
     * &#64;GET
     * &#64;Path("{id}")
     * public Solution getSolution(&#64;PathParam("id") ProblemId problemId) {
     *     Solution solution = solutionRepository.load(problemId);
     *     scoreManager.updateScore(solution);
     *     return solution;
     * }
     * </pre>
     */
    private void getSolution(ClassCreator classCreator, SolverResourceInfo solverResourceInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "getSolution", solverResourceInfo.getSolutionType(),
                solverResourceInfo.getProblemIdType());
        addGETAnnotation(methodCreator);
        addPathAnnotation(methodCreator, "{" + PROBLEM_ID_PATH_PARAM + "}");
        addPathParamAnnotation(methodCreator.getParameterAnnotations(0), PROBLEM_ID_PATH_PARAM);

        ResultHandle problemId = methodCreator.getMethodParam(0);
        ResultHandle solutionRepository = getSolutionRepositoryInstance(methodCreator);
        ResultHandle solution =
                methodCreator.invokeInterfaceMethod(SolutionRepositoryDescriptor.load(), solutionRepository, problemId);

        ResultHandle scoreManager = getScoreManagerInstance(methodCreator);
        methodCreator.invokeInterfaceMethod(ScoreManagerDescriptor.updateScore(), scoreManager, solution);

        methodCreator.returnValue(solution);
    }

    private String getResourceName(SolverResourceInfo solverResourceInfo) {
        return solverResourceInfo.getSolutionType() + "Resource";
    }

    private ResultHandle getSolveManagerInstance(MethodCreator methodCreator) {
        return getImplementedFieldInstanceByType(methodCreator, SolverManager.class);
    }

    private ResultHandle getSolutionRepositoryInstance(MethodCreator methodCreator) {
        return getImplementedFieldInstanceByType(methodCreator, SolutionRepository.class);
    }

    private ResultHandle getScoreManagerInstance(MethodCreator methodCreator) {
        return getImplementedFieldInstanceByType(methodCreator, ScoreManager.class);
    }

    private MethodCreator getMethodCreator(ClassCreator classCreator, String methodName, Class<?> returnType,
            Class<?>... parameters) {
        return classCreator
                .getMethodCreator(MethodDescriptor.ofMethod(SolverResource.class, methodName, returnType, parameters));
    }
}
