package org.optaplanner.constraint.streams.common.quad;

import org.optaplanner.constraint.streams.common.ConstraintConstructor;
import org.optaplanner.core.api.function.QuadFunction;

@FunctionalInterface
public interface QuadConstraintConstructor<A, B, C, D> extends ConstraintConstructor<QuadFunction<A, B, C, D, Object>> {

}
