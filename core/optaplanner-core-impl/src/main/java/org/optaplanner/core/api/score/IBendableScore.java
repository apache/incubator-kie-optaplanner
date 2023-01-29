package org.optaplanner.core.api.score;

import java.io.Serializable;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;

/**
 * Bendable score is a {@link Score} whose {@link #getHardLevelsSize()} and {@link #getSoftLevelsSize()}
 * are only known at runtime.
 * <p>
 * Implementations must be immutable.
 *
 * @param <Score_> the actual score type to allow addition, subtraction and other arithmetic
 * @see BendableScore
 */
public interface IBendableScore<Score_ extends IBendableScore<Score_>>
        extends Score<Score_>, Serializable {

    /**
     * The sum of this and {@link #getSoftLevelsSize()} equals {@link #getLevelsSize()}.
     *
     * @return {@code >= 0} and {@code <} {@link #getLevelsSize()}
     */
    int getHardLevelsSize();

    /**
     * The sum of {@link #getHardLevelsSize()} and this equals {@link #getLevelsSize()}.
     *
     * @return {@code >= 0} and {@code <} {@link #getLevelsSize()}
     */
    int getSoftLevelsSize();

    /**
     * @return {@link #getHardLevelsSize()} + {@link #getSoftLevelsSize()}
     */
    default int getLevelsSize() {
        return getHardLevelsSize() + getSoftLevelsSize();
    }

}
