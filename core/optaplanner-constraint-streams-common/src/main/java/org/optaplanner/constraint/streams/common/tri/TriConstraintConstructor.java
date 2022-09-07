package org.optaplanner.constraint.streams.common.tri;

import org.optaplanner.constraint.streams.common.ConstraintConstructor;
import org.optaplanner.core.api.function.TriFunction;

@FunctionalInterface
public interface TriConstraintConstructor<A, B, C> extends ConstraintConstructor<TriFunction<A, B, C, Object>> {

}
