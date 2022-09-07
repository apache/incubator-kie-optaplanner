package org.optaplanner.constraint.streams.common.bi;

import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.common.ConstraintConstructor;

@FunctionalInterface
public interface BiConstraintConstructor<A, B> extends ConstraintConstructor<BiFunction<A, B, Object>> {

}
