package org.optaplanner.constraint.streams.common.uni;

import java.util.Collection;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.ConstraintConstructor;

@FunctionalInterface
public interface UniConstraintConstructor<A>
        extends ConstraintConstructor<Function<A, Object>, Function<A, Collection<?>>> {

}
