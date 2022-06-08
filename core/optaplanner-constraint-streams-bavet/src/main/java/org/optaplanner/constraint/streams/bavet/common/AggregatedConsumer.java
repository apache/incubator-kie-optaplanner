package org.optaplanner.constraint.streams.bavet.common;

import java.util.function.Consumer;

final class AggregatedConsumer<Tuple_ extends Tuple> implements Consumer<Tuple_> {
    private final Consumer<Tuple_>[] consumers;

    public AggregatedConsumer(Consumer<Tuple_>[] consumers) {
        this.consumers = consumers;
    }

    @Override
    public void accept(Tuple_ tuple) {
        for (Consumer<Tuple_> consumer : consumers) {
            consumer.accept(tuple);
        }
    }

}
