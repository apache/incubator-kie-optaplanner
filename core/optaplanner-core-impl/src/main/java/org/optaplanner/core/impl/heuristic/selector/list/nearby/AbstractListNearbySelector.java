package org.optaplanner.core.impl.heuristic.selector.list.nearby;

import java.util.Objects;

import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbyDistanceMatrixDemand;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListener;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.util.MemoizingSupply;

abstract class AbstractListNearbySelector<Solution_, ChildSelector_ extends PhaseLifecycleListener<Solution_>, ReplayingSelector_ extends PhaseLifecycleListener<Solution_>>
        extends AbstractSelector<Solution_> {

    protected final ChildSelector_ childSelector;
    protected final ReplayingSelector_ replayingSelector;
    protected final NearbyDistanceMeter<?, ?> nearbyDistanceMeter;
    protected final NearbyRandom nearbyRandom;
    protected final AbstractNearbyDistanceMatrixDemand<?, ?, ChildSelector_, ReplayingSelector_> nearbyDistanceMatrixDemand;

    protected MemoizingSupply<NearbyDistanceMatrix<Object, Object>> nearbyDistanceMatrixSupply = null;

    protected AbstractListNearbySelector(
            ChildSelector_ childSelector,
            Object replayingSelector,
            NearbyDistanceMeter<?, ?> nearbyDistanceMeter,
            NearbyRandom nearbyRandom) {
        this.childSelector = childSelector;
        this.replayingSelector = castReplayingSelector(replayingSelector);
        this.nearbyDistanceMeter = nearbyDistanceMeter;
        this.nearbyRandom = nearbyRandom;
        this.nearbyDistanceMatrixDemand = createDemand();
        this.phaseLifecycleSupport.addEventListener(childSelector);
        this.phaseLifecycleSupport.addEventListener(this.replayingSelector);
    }

    protected abstract ReplayingSelector_ castReplayingSelector(Object uncastReplayingSelector);

    protected abstract AbstractNearbyDistanceMatrixDemand<?, ?, ChildSelector_, ReplayingSelector_> createDemand();

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
        /*
         * Supply will ask questions of the child selector.
         * However, child selector will only be initialized during phase start.
         * Yet we still want the very expensive nearby distance matrix to be reused across phases.
         * Therefore we request the supply here, but actually lazily initialize it during phase start.
         */
        nearbyDistanceMatrixSupply = (MemoizingSupply) supplyManager.demand(nearbyDistanceMatrixDemand);
    }

    @Override
    public final void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        // Lazily initialize the supply, so that steps can then have uniform performance.
        nearbyDistanceMatrixSupply.read();
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        solverScope.getScoreDirector().getSupplyManager().cancel(nearbyDistanceMatrixDemand);
        nearbyDistanceMatrixSupply = null;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractListNearbySelector<?, ?, ?> that = (AbstractListNearbySelector<?, ?, ?>) other;
        return Objects.equals(childSelector, that.childSelector)
                && Objects.equals(replayingSelector, that.replayingSelector)
                && Objects.equals(nearbyDistanceMeter, that.nearbyDistanceMeter)
                && Objects.equals(nearbyRandom, that.nearbyRandom);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(childSelector, replayingSelector, nearbyDistanceMeter, nearbyRandom);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(" + replayingSelector + ", " + childSelector + ")";
    }
}
