/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.solver;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class BestSolutionContainingProblemChanges<Solution_> {
    private final Solution_ bestSolution;
    private final List<CompletableFuture<Void>> containedProblemChanges;

    public BestSolutionContainingProblemChanges(Solution_ bestSolution, List<CompletableFuture<Void>> containedProblemChanges) {
        this.bestSolution = bestSolution;
        this.containedProblemChanges = containedProblemChanges;
    }

    public Solution_ getBestSolution() {
        return bestSolution;
    }

    public void completeProblemChanges() {
        containedProblemChanges.forEach(futureProblemChange -> futureProblemChange.complete(null));
    }

    public void completeProblemChangesExceptionally(Throwable exception) {
        containedProblemChanges.forEach(futureProblemChange -> futureProblemChange.completeExceptionally(exception));
    }
}
