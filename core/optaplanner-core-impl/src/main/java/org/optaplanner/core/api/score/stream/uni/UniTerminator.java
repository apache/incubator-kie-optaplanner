package org.optaplanner.core.api.score.stream.uni;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;

public interface UniTerminator<A> {

    UniTerminator<A> withWeight(Score<?> constraintWeight);

    Constraint as(String constraintName);

    Constraint as(String constraintPackage, String constraintName);

}
