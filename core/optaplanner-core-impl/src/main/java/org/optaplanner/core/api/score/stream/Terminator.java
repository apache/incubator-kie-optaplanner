package org.optaplanner.core.api.score.stream;

import org.optaplanner.core.api.score.Score;

public interface Terminator<Terminator_ extends Terminator<Terminator_>> {

    Terminator_ withWeight(Score<?> constraintWeight);

    Constraint as(String constraintName);

    Constraint as(String constraintPackage, String constraintName);

}
