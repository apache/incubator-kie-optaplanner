package org.optaplanner.core.impl.ruin;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.ruin.RuinPhaseConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.RuinMoveSelectorFactory;
import org.optaplanner.core.impl.phase.AbstractPhaseFactory;
import org.optaplanner.core.impl.phase.custom.DefaultCustomPhase;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecaller;
import org.optaplanner.core.impl.solver.termination.Termination;

public class RuinPhaseFactory<Solution_> extends AbstractPhaseFactory<Solution_, RuinPhaseConfig> {

    public RuinPhaseFactory(RuinPhaseConfig phaseConfig) {
        super(phaseConfig);
    }

    @Override
    public DefaultCustomPhase<Solution_> buildPhase(int phaseIndex, HeuristicConfigPolicy<Solution_> solverConfigPolicy,
            BestSolutionRecaller<Solution_> bestSolutionRecaller, Termination<Solution_> solverTermination) {
        HeuristicConfigPolicy<Solution_> phaseConfigPolicy = solverConfigPolicy.createPhaseConfigPolicy();
        Termination<Solution_> phaseTermination = buildPhaseTermination(phaseConfigPolicy, solverTermination);
        MoveSelector<Solution_> moveSelector = buildMoveSelector(phaseConfigPolicy);
        RuinPhaseCommand<Solution_> ruinPhaseCommand = new RuinPhaseCommand<>(moveSelector);
        RuinCustomPhase.Builder<Solution_> builder =
                new RuinCustomPhase.Builder<>(phaseIndex, solverConfigPolicy.getLogIndentation(), phaseTermination,
                        ruinPhaseCommand, moveSelector);

        EnvironmentMode environmentMode = phaseConfigPolicy.getEnvironmentMode();
        if (environmentMode.isNonIntrusiveFullAsserted()) {
            builder.setAssertStepScoreFromScratch(true);
        }
        if (environmentMode.isIntrusiveFastAsserted()) {
            builder.setAssertExpectedStepScore(true);
            builder.setAssertShadowVariablesAreNotStaleAfterStep(true);
        }
        return builder.build();
    }

    private MoveSelector<Solution_> buildMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy) {
        return new RuinMoveSelectorFactory<Solution_>(phaseConfig)
                .buildRuinMoveSelector(configPolicy, SelectionCacheType.JUST_IN_TIME, true);
    }
}
