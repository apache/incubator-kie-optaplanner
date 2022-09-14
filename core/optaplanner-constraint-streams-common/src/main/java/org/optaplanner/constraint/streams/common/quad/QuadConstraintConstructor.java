package org.optaplanner.constraint.streams.common.quad;

import java.util.Collection;

import org.optaplanner.constraint.streams.common.ConstraintConstructor;
import org.optaplanner.core.api.function.QuadFunction;

@FunctionalInterface
public interface QuadConstraintConstructor<A, B, C, D>
        extends ConstraintConstructor<QuadFunction<A, B, C, D, Object>, QuadFunction<A, B, C, D, Collection<?>>> {

}
