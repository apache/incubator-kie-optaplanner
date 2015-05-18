package org.optaplanner.core.impl.domain.valuerange.custom;

import org.optaplanner.core.impl.domain.valuerange.AbstractUncountableValueRange;
import org.optaplanner.core.impl.domain.valuerange.util.ValueRangeIterator;

import java.util.Iterator;
import java.util.Random;

public class TwoDoubleIntervalValueRange extends AbstractUncountableValueRange<Double> {

    private Double lowFirstInterval;
    private Double highFirstInterval;
    private Double lowSecondInterval;
    private Double highSecondInterval;

    public TwoDoubleIntervalValueRange(Double lowFirstInterval, Double highFirstInterval, Double lowSecondInterval,
                                       Double highSecondInterval) {
        if((lowFirstInterval >= highFirstInterval) || (lowSecondInterval >= highSecondInterval)) {
            throw new IllegalArgumentException("Illegal intervals bounds");
        }
        this.lowFirstInterval = lowFirstInterval;
        this.highFirstInterval = highFirstInterval;
        this.lowSecondInterval = lowSecondInterval;
        this.highSecondInterval = highSecondInterval;
    }

    @Override
    public boolean contains(Double value) {
        if (value == null) {
            return false;
        }
        return ((value >= lowFirstInterval && value < highFirstInterval) ||
                (value >= lowSecondInterval && value < highSecondInterval));
    }

    @Override
    public Iterator<Double> createRandomIterator(Random workingRandom) {
        return new RandomDoubleValueRangeIterator(workingRandom);
    }

    private class RandomDoubleValueRangeIterator extends ValueRangeIterator<Double> {

        private final Random workingRandom;

        public RandomDoubleValueRangeIterator(Random workingRandom) {
            this.workingRandom = workingRandom;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Double next() {
            if(workingRandom.nextBoolean()) {

                return pickNext(lowFirstInterval, highFirstInterval);
            } else {
                return pickNext(lowSecondInterval, highSecondInterval);
            }
        }

        private Double pickNext(Double from, Double to) {
            double diff = to - from;
            double next = from + diff * workingRandom.nextDouble();
            if (next >= to) {
                // Rounding error occurred
                next = Math.nextAfter(next, Double.NEGATIVE_INFINITY);
            }
            return next;
        }
    }
}
