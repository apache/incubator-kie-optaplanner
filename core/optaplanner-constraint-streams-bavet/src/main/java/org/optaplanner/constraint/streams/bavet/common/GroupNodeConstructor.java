package org.optaplanner.constraint.streams.bavet.common;

@FunctionalInterface
public interface GroupNodeConstructor<Tuple_ extends Tuple> {

    AbstractNode apply(int groupStoreIndex, int undoStoreIndex, TupleLifecycle<Tuple_> nextNodesTupleLifecycle, int outputStoreSize);

}
