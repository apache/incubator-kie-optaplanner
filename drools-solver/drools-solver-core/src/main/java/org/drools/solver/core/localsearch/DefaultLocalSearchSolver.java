package org.drools.solver.core.localsearch;

import java.util.Random;

import org.drools.RuleBase;
import org.drools.solver.core.localsearch.bestsolution.BestSolutionRecaller;
import org.drools.solver.core.localsearch.decider.Decider;
import org.drools.solver.core.localsearch.finish.Finish;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.score.calculator.ScoreCalculator;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.HardAndSoftScore;
import org.drools.solver.core.score.definition.ScoreDefinition;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.solution.initializer.StartingSolutionInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link LocalSearchSolver}.
 * @author Geoffrey De Smet
 */
public class DefaultLocalSearchSolver implements LocalSearchSolver, LocalSearchSolverLifecycleListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected long randomSeed; // TODO refactor to AbstractSolver

    protected StartingSolutionInitializer startingSolutionInitializer = null; // TODO refactor to AbstractSolver
    protected BestSolutionRecaller bestSolutionRecaller;
    protected Finish finish;
    protected Decider decider;

    protected LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope(); // TODO remove me

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public void setRuleBase(RuleBase ruleBase) {
        localSearchSolverScope.setRuleBase(ruleBase);
    }

    public void setScoreDefinition(ScoreDefinition scoreDefinition) {
        localSearchSolverScope.setScoreDefinition(scoreDefinition);
    }

    public void setScoreCalculator(ScoreCalculator scoreCalculator) {
        localSearchSolverScope.setWorkingScoreCalculator(scoreCalculator);
    }

    public StartingSolutionInitializer getStartingSolutionInitializer() {
        return startingSolutionInitializer;
    }

    public void setStartingSolutionInitializer(StartingSolutionInitializer startingSolutionInitializer) {
        this.startingSolutionInitializer = startingSolutionInitializer;
    }

    public void setBestSolutionRecaller(BestSolutionRecaller bestSolutionRecaller) {
        this.bestSolutionRecaller = bestSolutionRecaller;
        this.bestSolutionRecaller.setLocalSearchSolver(this);
    }

    public Decider getDecider() {
        return decider;
    }

    public void setDecider(Decider decider) {
        this.decider = decider;
        this.decider.setLocalSearchSolver(this);
    }

    public void setFinish(Finish finish) {
        this.finish = finish;
        this.finish.setLocalSearchSolver(this);
    }

    public void setStartingSolution(Solution startingSolution) {
        localSearchSolverScope.setWorkingSolution(startingSolution);
    }

    public Score getBestScore() {
        return this.localSearchSolverScope.getBestScore();
    }

    public Solution getBestSolution() {
        return this.localSearchSolverScope.getBestSolution();
    }

    public long getTimeMillisSpend() {
        return this.localSearchSolverScope.calculateTimeMillisSpend();
    }

    public LocalSearchSolverScope getLocalSearchSolverScope() {
        return localSearchSolverScope;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solve() {
        LocalSearchSolverScope localSearchSolverScope = this.localSearchSolverScope;
        solvingStarted(localSearchSolverScope);

        StepScope stepScope = createNextStepScope(localSearchSolverScope, null);
        while (!finish.isFinished(stepScope)) {
            stepScope.setTimeGradient(finish.calculateTimeGradient(stepScope));
            beforeDeciding(stepScope);
            decider.decideNextStep(stepScope);
            Move nextStep = stepScope.getStep();
            if (nextStep == null) {
                logger.warn("No move accepted for step index ({}) out of {} accepted moves. Finishing early.",
                        stepScope.getStepIndex(), decider.getForager().getAcceptedMovesSize());
                break;
            }
            logger.info("Step index ({}), time spend ({}) taking step ({}) out of {} accepted moves.",
                    new Object[]{stepScope.getStepIndex(), localSearchSolverScope.calculateTimeMillisSpend(),
                            nextStep, decider.getForager().getAcceptedMovesSize()});
            stepDecided(stepScope);
            nextStep.doMove(stepScope.getWorkingMemory());
            stepTaken(stepScope);
            stepScope = createNextStepScope(localSearchSolverScope, stepScope);
        }
        solvingEnded(localSearchSolverScope);
    }

    private StepScope createNextStepScope(LocalSearchSolverScope localSearchSolverScope, StepScope completedStepScope) {
        if (completedStepScope == null) {
            completedStepScope = new StepScope(localSearchSolverScope);
            completedStepScope.setScore(localSearchSolverScope.getStartingScore());
            completedStepScope.setStepIndex(-1);
            completedStepScope.setTimeGradient(0.0);
        }
        localSearchSolverScope.setLastCompletedStepScope(completedStepScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        stepScope.setStepIndex(completedStepScope.getStepIndex() + 1);
        return stepScope;
    }

    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        localSearchSolverScope.resetTimeMillisSpend();
        logger.info("Solving with random seed ({}).", randomSeed);
        localSearchSolverScope.setWorkingRandom(new Random(randomSeed));
        if (startingSolutionInitializer != null) {
            if (!startingSolutionInitializer.isSolutionInitialized(localSearchSolverScope)) {
                logger.info("Initializing solution.");
                startingSolutionInitializer.initializeSolution(localSearchSolverScope);
            } else {
                logger.debug("Solution is already initialized.");
            }
        }
        localSearchSolverScope.setStartingScore(localSearchSolverScope.calculateScoreFromWorkingMemory());
        bestSolutionRecaller.solvingStarted(localSearchSolverScope);
        finish.solvingStarted(localSearchSolverScope);
        decider.solvingStarted(localSearchSolverScope);
    }

    public void beforeDeciding(StepScope stepScope) {
        bestSolutionRecaller.beforeDeciding(stepScope);
        finish.beforeDeciding(stepScope);
        decider.beforeDeciding(stepScope);
    }

    public void stepDecided(StepScope stepScope) {
        bestSolutionRecaller.stepDecided(stepScope);
        finish.stepDecided(stepScope);
        decider.stepDecided(stepScope);
    }

    public void stepTaken(StepScope stepScope) {
        bestSolutionRecaller.stepTaken(stepScope);
        finish.stepTaken(stepScope);
        decider.stepTaken(stepScope);
    }

    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        bestSolutionRecaller.solvingEnded(localSearchSolverScope);
        finish.solvingEnded(localSearchSolverScope);
        decider.solvingEnded(localSearchSolverScope);
        logger.info("Solved in {} steps and {} time millis spend.",
                localSearchSolverScope.getLastCompletedStepScope().getStepIndex(),
                localSearchSolverScope.calculateTimeMillisSpend());
    }

}
