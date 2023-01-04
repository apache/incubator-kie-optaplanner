package org.optaplanner.core.impl.heuristic.selector.entity;

import java.util.Objects;

import org.optaplanner.core.config.heuristic.selector.entity.pillar.SubPillarConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.entity.nearby.NearEntityNearbyEntitySelector;

/**
 * Exists so that {@link NearEntityNearbyEntitySelector} can rely on the meter in its
 * {@link SubPillarConfigPolicy#equals(Object)}.
 * If two instances share the same meter class, they are considered the same comparator.
 * This is safe, as meters are instantiated by us and therefore can not bring their own state.
 */
final class WrappedNearbyDistanceMeter<O, D> implements NearbyDistanceMeter<O, D> {

    private final NearbyDistanceMeter<O, D> nearbyDistanceMeter;

    WrappedNearbyDistanceMeter(NearbyDistanceMeter<O, D> nearbyDistanceMeter) {
        this.nearbyDistanceMeter = nearbyDistanceMeter;
    }

    @Override
    public double getNearbyDistance(O origin, D destination) {
        return nearbyDistanceMeter.getNearbyDistance(origin, destination);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        WrappedNearbyDistanceMeter<?, ?> that = (WrappedNearbyDistanceMeter<?, ?>) other;
        return Objects.equals(nearbyDistanceMeter.getClass(), that.nearbyDistanceMeter.getClass());
    }

    @Override
    public int hashCode() {
        return nearbyDistanceMeter.getClass().hashCode();
    }
}
