package org.optaplanner.constraint.streams.common.tri;

import java.util.Collection;

import org.optaplanner.constraint.streams.common.ConstraintConstructor;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;

@FunctionalInterface
public interface TriConstraintConstructor<A, B, C>
        extends ConstraintConstructor<QuadFunction<A, B, C, Score<?>, Object>, TriFunction<A, B, C, Collection<?>>> {

}
