package org.optaplanner.core.impl.heuristic.selector.common.nearby;

/**
 * It is recommended that instances be {@link Object#equals equal} if they represent the same meter,
 * in order to support nearby entity selector equality.
 *
 * @param <O>
 * @param <D>
 */
public interface NearbyDistanceMeter<O, D> {

    /**
     * Measures the distance from the origin to the destination.
     * The distance can be in any unit, such a meters, foot, seconds or milliseconds.
     * For example, vehicle routing often uses driving time in seconds.
     * <p>
     * Distances can be asymmetrical: the distance from an origin to a destination
     * often differs from the distance from that destination to that origin.
     *
     * @param origin never null
     * @param destination never null
     * @return Preferably always {@code >= 0.0}. If origin == destination, it usually returns 0.0.
     */
    double getNearbyDistance(O origin, D destination);

}
