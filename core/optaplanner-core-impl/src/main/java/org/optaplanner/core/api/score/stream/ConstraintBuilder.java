package org.optaplanner.core.api.score.stream;

public interface ConstraintBuilder<ConstraintBuilder_ extends ConstraintBuilder<ConstraintBuilder_>> {

    Constraint asConstraint(String constraintName);

    Constraint asConstraint(String constraintPackage, String constraintName);

}
