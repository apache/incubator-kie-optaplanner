package org.optaplanner.examples.taskassigning.solver.score;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.examples.taskassigning.domain.Priority;
import org.optaplanner.examples.taskassigning.domain.Task;

public final class TaskAssigningConstraintProvider implements ConstraintProvider {

    private static final int BENDABLE_SCORE_HARD_LEVELS_SIZE = 1;
    private static final int BENDABLE_SCORE_SOFT_LEVELS_SIZE = 4;

    private static UniConstraintStream<Task> getInitializedTaskUniStream(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Task.class)
                .filter(task -> task.getEmployee() != null);
    }

    private static Constraint noMissingSkills(ConstraintFactory constraintFactory) {
        return getInitializedTaskUniStream(constraintFactory)
                .filter(task -> task.getMissingSkillCount() > 0)
                .penalize("No missing skills",
                        BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1),
                        Task::getMissingSkillCount);
    }

    private static Constraint criticalPriorityBasedTaskEndTime(ConstraintFactory constraintFactory) {
        return getInitializedTaskUniStream(constraintFactory)
                .filter(task -> task.getPriority() == Priority.CRITICAL)
                .penalize("Critical priority task end time",
                        BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1),
                        Task::getEndTime);
    }

    private static Constraint minimizeMakespan(ConstraintFactory constraintFactory) {
        return getInitializedTaskUniStream(constraintFactory)
                .filter(task -> task.getNextTask() == null)
                .penalize("Minimize makespan, latest ending employee first",
                        BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 1, 1),
                        task -> task.getEndTime() * task.getEndTime());
        /*
         * TODO ^^^ Some constraints need exponential weighting. perhaps we should have dynamic
         *  constraint weights, inferred from the entity?
         */
    }

    private static Constraint majorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getInitializedTaskUniStream(constraintFactory)
                .filter(task -> task.getPriority() == Priority.MAJOR)
                .penalize("Major priority task end time",
                        BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        Task::getEndTime);
    }

    private static Constraint minorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getInitializedTaskUniStream(constraintFactory)
                .filter(task -> task.getPriority() == Priority.MINOR)
                .penalize("Minor priority task end time",
                        BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 3, 1),
                        Task::getEndTime);
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                noMissingSkills(constraintFactory),
                minimizeMakespan(constraintFactory),
                /*
                 * TODO if we had a way of determining constraint weight from the entity, different weight for different
                 *  priority, then the following 3 constraints could have been one constraint, with associated
                 *  performance benefits.
                 */
                criticalPriorityBasedTaskEndTime(constraintFactory),
                majorPriorityTaskEndTime(constraintFactory),
                minorPriorityTaskEndTime(constraintFactory)
        };
    }
}
