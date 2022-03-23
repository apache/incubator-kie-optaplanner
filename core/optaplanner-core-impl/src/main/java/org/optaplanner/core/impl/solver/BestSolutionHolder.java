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

package org.optaplanner.core.impl.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.change.ProblemChange;

final class BestSolutionHolder<Solution_> {

    private final Lock problemChangesLock = new ReentrantLock();
    private final AtomicReference<VersionedBestSolution> versionedBestSolutionRef = new AtomicReference<>();
    private final SortedMap<BigInteger, List<CompletableFuture<Void>>> problemChangesPerVersion =
            new TreeMap<>();
    private BigInteger currentVersion = BigInteger.ZERO;

    boolean isEmpty() {
        return versionedBestSolutionRef.get() == null;
    }

    /**
     * @return the last best solution together with problem changes the solution contains.
     */
    BestSolutionContainingProblemChanges<Solution_> take() {
        VersionedBestSolution versionedBestSolution = versionedBestSolutionRef.getAndSet(null);
        if (versionedBestSolution == null) {
            return null;
        }
        SortedMap<BigInteger, List<CompletableFuture<Void>>> containedProblemChangesPerVersion =
                problemChangesPerVersion.headMap(versionedBestSolution.getVersion().add(BigInteger.ONE));

        List<CompletableFuture<Void>> containedProblemChanges = containedProblemChangesPerVersion.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        containedProblemChangesPerVersion.keySet().forEach(problemChangesPerVersion::remove);

        return new BestSolutionContainingProblemChanges<>(versionedBestSolution.getBestSolution(),
                containedProblemChanges);
    }

    /**
     * Sets the new best solution if all known problem changes have been processed and thus are contained in this
     * best solution.
     * 
     * @param bestSolution the new best solution that replaces the previous one if there is any
     * @param isEveryProblemChangeProcessed a supplier that tells if all problem changes have been processed
     */
    void set(Solution_ bestSolution, Supplier<Boolean> isEveryProblemChangeProcessed) {
        problemChangesLock.lock();
        try {
            /*
             * The new best solution can be accepted only if there are no pending problem changes nor any additional
             * changes may come during this operation. Otherwise, a race condition might occur that leads to associating
             * problem changes with a solution that was created later, but does not contain them yet.
             * As a result, CompletableFutures representing these changes would be completed too early.
             */
            if (isEveryProblemChangeProcessed.get()) {
                versionedBestSolutionRef.set(new VersionedBestSolution(bestSolution, currentVersion));
                currentVersion = currentVersion.add(BigInteger.ONE);
            }
        } finally {
            problemChangesLock.unlock();
        }
    }

    /**
     * Adds a new problem change to a solver and registers the problem change to be later retrieved together with
     * a relevant best solution by the {@link #take()} method.
     * 
     * @return CompletableFuture that will be completed after the best solution containing this change is passed to
     *         a user-defined Consumer.
     */
    CompletableFuture<Void> addProblemChange(Solver<Solution_> solver, ProblemChange<Solution_> problemChange) {
        CompletableFuture<Void> futureProblemChange;
        problemChangesLock.lock();
        try {
            futureProblemChange = new CompletableFuture<>();
            problemChangesPerVersion.compute(currentVersion, (version, futureProblemChangeList) -> {
                if (futureProblemChangeList == null) {
                    futureProblemChangeList = new ArrayList<>();
                }
                futureProblemChangeList.add(futureProblemChange);
                return futureProblemChangeList;
            });
            solver.addProblemChange(problemChange);
        } finally {
            problemChangesLock.unlock();
        }

        return futureProblemChange;
    }

    void cancelPendingChanges() {
        problemChangesLock.lock();
        try {
            problemChangesPerVersion.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .forEach(pendingProblemChange -> pendingProblemChange.cancel(false));
            problemChangesPerVersion.clear();
        } finally {
            problemChangesLock.unlock();
        }
    }

    private final class VersionedBestSolution {
        final Solution_ bestSolution;
        final BigInteger version;

        public VersionedBestSolution(Solution_ bestSolution, BigInteger version) {
            this.bestSolution = bestSolution;
            this.version = version;
        }

        public Solution_ getBestSolution() {
            return bestSolution;
        }

        public BigInteger getVersion() {
            return version;
        }
    }
}
