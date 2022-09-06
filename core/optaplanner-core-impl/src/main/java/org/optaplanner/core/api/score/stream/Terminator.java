package org.optaplanner.core.api.score.stream;

public interface Terminator<Terminator_ extends Terminator<Terminator_>> {

    Constraint as(String constraintName);

    Constraint as(String constraintPackage, String constraintName);

}
