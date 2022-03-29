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

package org.optaplanner.core.api.solver.change;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.solver.Solver;

/**
 * A ProblemChange represents a change in one or more {@link PlanningEntity planning entities} or problem facts
 * of a {@link PlanningSolution}.
 * <p>
 * The {@link Solver} checks the presence of waiting problem changes after every
 * {@link org.optaplanner.core.impl.heuristic.move.Move} evaluation. If there are waiting problem changes,
 * the {@link Solver}:
 * <ol>
 * <li>clones the last {@link PlanningSolution best solution} and sets the clone
 * as the new {@link PlanningSolution working solution}</li>
 * <li>applies every problem change keeping the order in which problem changes have been submitted;
 * if {@link ProblemChange#usesShadowVariables()} returns {@code true},
 * {@link org.optaplanner.core.api.domain.variable.VariableListener variable listeners} are triggered before the
 * problem change is applied</li>
 * <li>triggers {@link org.optaplanner.core.api.domain.variable.VariableListener variable listeners} to guarantee
 * consistency of the {@link PlanningSolution updated working solution}</li>
 * <li>calculates the score and makes the {@link PlanningSolution updated working solution}
 * the new {@link PlanningSolution best solution}; note that this {@link PlanningSolution solution} is not published
 * via the {@link org.optaplanner.core.api.solver.event.BestSolutionChangedEvent}</li>
 * <li>restarts solving to fill potential uninitialized {@link PlanningEntity planning entities}</li>
 * </ol>
 * <p>
 * Note that the {@link Solver} clones a {@link PlanningSolution} at will.
 * Any change must be done on the problem facts and planning entities referenced by the {@link PlanningSolution}.
 * <p>
 * An example implementation, based on the Cloud balancing problem, looks as follows:
 * 
 * <pre>
 * {@code
 * public class DeleteComputerProblemChange implements ProblemChange<CloudBalance> {
 *
 *     private final CloudComputer computer;
 *
 *     public DeleteComputerProblemChange(CloudComputer computer) {
 *         this.computer = computer;
 *     }
 *
 *     {@literal @Override}
 *     public void doChange(CloudBalance cloudBalance, ProblemChangeDirector problemChangeDirector) {
 *         CloudComputer workingComputer = problemChangeDirector.lookUpWorkingObjectOrFail(computer);
 *         // First remove the problem fact from all planning entities that use it
 *         for (CloudProcess process : cloudBalance.getProcessList()) {
 *             if (process.getComputer() == workingComputer) {
 *                 process.setComputer(null);
 *             }
 *         }
 *         // A SolutionCloner does not clone problem fact lists (such as computerList), only entity lists.
 *         // Shallow clone the computerList so only the working solution is affected.
 *         ArrayList<CloudComputer> computerList = new ArrayList<>(cloudBalance.getComputerList());
 *         cloudBalance.setComputerList(computerList);
 *         computerList.remove(workingComputer);
 *     }
 * }
 * }
 * </pre>
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
@FunctionalInterface
public interface ProblemChange<Solution_> {

    /**
     * Informs if this {@link ProblemChange} uses shadow variables. If so, shadow variables are guaranteed to be
     * up-to-date before the {@link ProblemChange#doChange(Object, ProblemChangeDirector)} is invoked.
     * Overriding this method implicates that shadow variables will not be recalculated before this {@link ProblemChange},
     * which brings performance benefits at the expense that shadow variables' values cannot be relied upon in the
     * {@link ProblemChange#doChange(Object, ProblemChangeDirector)} method implementation.
     *
     * @return true if this problem change requires up-to-date shadow variables, otherwise false.
     */
    default boolean usesShadowVariables() {
        return true;
    }

    /**
     * Do the change on the {@link PlanningSolution} that is a clone of the last best solution.
     *
     * @param workingSolution never null; the {@link PlanningSolution working solution} which contains the problem facts
     *        (and {@link PlanningEntity planning entities}) to change
     * @param problemChangeDirector never null; {@link ProblemChangeDirector} to lookup working memory objects
     */
    void doChange(Solution_ workingSolution, ProblemChangeDirector problemChangeDirector);
}
