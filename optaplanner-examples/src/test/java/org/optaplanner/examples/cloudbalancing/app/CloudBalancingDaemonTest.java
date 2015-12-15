/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.cloudbalancing.app;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;
import org.optaplanner.examples.cloudbalancing.persistence.CloudBalancingGenerator;
import org.optaplanner.examples.common.app.LoggingTest;

import static org.junit.Assert.assertEquals;

public class CloudBalancingDaemonTest extends LoggingTest {

    private Object stageLock = new Object();
    private AtomicInteger stageNumber = new AtomicInteger(0);
    private CountDownLatch stage1Latch = new CountDownLatch(1);
    private CountDownLatch stage2Latch = new CountDownLatch(1);
    private CountDownLatch stage3Latch = new CountDownLatch(1);

    private Queue<CloudProcess> notYetAddedProcessQueue = new LinkedList<CloudProcess>();

    @Test(timeout = 600000)
    public void daemon() throws InterruptedException {
        // In main thread
        Solver solver = buildSolver();
        CloudBalance cloudBalance = buildPlanningProblem(4, 12);
        SolverThread solverThread = new SolverThread(solver, cloudBalance);
        solverThread.start();
        // Wait for the solver thread to start up
        waitForNextStage();

        // Give the solver thread a chance to terminate and get into the daemon waiting state
        Thread.sleep(500);
        for (int i = 0; i < 8; i++) {
            CloudProcess process = notYetAddedProcessQueue.poll();
            solver.addProblemFactChange(new AddProcessChange(process));
        }
        // Wait until those AddProcessChanges are processed
        waitForNextStage();
        assertEquals(8, ((CloudBalance) solver.getBestSolution()).getProcessList().size());

        // Give the solver thread some time to solve, terminate and get into the daemon waiting state
        Thread.sleep(1000);
        while (!notYetAddedProcessQueue.isEmpty()) {
            CloudProcess process = notYetAddedProcessQueue.poll();
            solver.addProblemFactChange(new AddProcessChange(process));
        }
        // Wait until those AddProcessChanges are processed
        waitForNextStage();

        solver.terminateEarly();
        try {
            // Wait until the solver thread dies.
            solverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SolverThread did not die yet due to an interruption.", e);
        }
        assertEquals(true, solver.isEveryProblemFactChangeProcessed());
        assertEquals(12, ((CloudBalance) solver.getBestSolution()).getProcessList().size());
    }

    private class SolverThread extends Thread implements SolverEventListener<CloudBalance> {

        private final Solver solver;
        private final CloudBalance cloudBalance;

        private SolverThread(Solver solver, CloudBalance cloudBalance) {
            this.solver = solver;
            this.cloudBalance = cloudBalance;
        }

        @Override
        public void run() { // In solver thread
            solver.addEventListener(this);
            nextStage(); // For an empty entity list, there is no bestSolutionChanged() event currently
            solver.solve(cloudBalance);
        }

        @Override
        public void bestSolutionChanged(BestSolutionChangedEvent<CloudBalance> event) { // In solver thread
            if (event.isEveryProblemFactChangeProcessed() && event.isNewBestSolutionInitialized()
                    && event.getNewBestSolution().getScore().isFeasible()) {
                // TODO bestSolutionChanged() is not the most reliable way to control this test's execution:
                // With another termination, a Solver can terminate before a feasible best solution event is fired
                nextStage();
            }
        }

    }

    private static class AddProcessChange implements ProblemFactChange {

        private final CloudProcess process;

        private AddProcessChange(CloudProcess process) {
            this.process = process;
        }

        @Override
        public void doChange(ScoreDirector scoreDirector) { // In solver thread
            CloudBalance cloudBalance = (CloudBalance) scoreDirector.getWorkingSolution();
            // No need to clone the processList because planning cloning already does that
            scoreDirector.beforeEntityAdded(process);
            cloudBalance.getProcessList().add(process);
            scoreDirector.afterEntityAdded(process);
            scoreDirector.triggerVariableListeners();
        }

    }

    protected Solver buildSolver() {
        SolverFactory solverFactory = SolverFactory.createFromXmlResource(
                "org/optaplanner/examples/cloudbalancing/solver/cloudBalancingSolverConfig.xml");
        solverFactory.getSolverConfig().setDaemon(true);
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setBestScoreFeasible(true);
        solverFactory.getSolverConfig().setTerminationConfig(terminationConfig);
        return solverFactory.buildSolver();
    }

    private CloudBalance buildPlanningProblem(int computerListSize, int processListSize) {
        CloudBalance cloudBalance = new CloudBalancingGenerator().createCloudBalance(computerListSize, processListSize);
        notYetAddedProcessQueue.addAll(cloudBalance.getProcessList());
        cloudBalance.setProcessList(new ArrayList<CloudProcess>(notYetAddedProcessQueue.size()));
        return cloudBalance;
    }

    private void waitForNextStage() throws InterruptedException {
        CountDownLatch latch;
        synchronized (stageLock) {
            switch (stageNumber.get()) {
                case 0:
                    latch = stage1Latch;
                    break;
                case 1:
                    latch = stage2Latch;
                    break;
                case 2:
                    latch = stage3Latch;
                    break;
                default:
                    throw new IllegalStateException("Unsupported phaseNumber (" + stageNumber.get() + ").");
            }
        }
        latch.await();
        // TODO Unlikely race condition: all bestSolutionChanged() could be processed before stageNumber is incremented
        int stage;
        synchronized (stageLock) {
            stage = stageNumber.incrementAndGet();
        }
        logger.info("==== New testing stage ({}) started. ====", stage);
    }

    private void nextStage() {
        synchronized (stageLock) {
            switch (stageNumber.get()) {
                case 0:
                    stage1Latch.countDown();
                    break;
                case 1:
                    stage2Latch.countDown();
                    break;
                case 2:
                    stage3Latch.countDown();
                    break;
            }
        }
    }

}
