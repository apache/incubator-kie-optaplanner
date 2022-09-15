package org.optaplanner.constraint.streams.common.uni;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.ConstraintConstructor;
import org.optaplanner.core.api.score.Score;

@FunctionalInterface
public interface UniConstraintConstructor<A>
        extends ConstraintConstructor<BiFunction<A, Score<?>, Object>, Function<A, Collection<?>>> {

}
