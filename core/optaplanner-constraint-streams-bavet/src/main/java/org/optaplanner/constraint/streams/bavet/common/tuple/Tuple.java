package org.optaplanner.constraint.streams.bavet.common.tuple;

import java.util.function.Function;

import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;

/**
 * A tuple is an <i>out tuple</i> in exactly one node ("origin node") and an <i>in tuple</i> in one or more nodes.
 *
 * @apiNote Tuples are mutable.
 *          However, only origin nodes should mutate them.
 *          Other nodes are generally expected to treat them as read-only.
 * @implSpec A tuple must not implement equals()/hashCode() for fact equality,
 *           because some stream operations ({@link UniConstraintStream#map(Function)}, ...)
 *           might create 2 different tuple instances to contain the same facts
 *           and because a tuple's origin node may replace a tuple's fact.
 */
public interface Tuple {

    TupleState getState();

    void setState(TupleState state);

    <Value_> Value_ getStore(int index);

    void setStore(int index, Object value);

    <Value_> Value_ removeStore(int index);

}
