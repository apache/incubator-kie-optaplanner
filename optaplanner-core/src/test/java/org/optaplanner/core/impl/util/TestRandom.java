package org.optaplanner.core.impl.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * On the alter JDKs, it is no longer possible to mock {@link Random} to return custom sequences.
 * Therefore we introduce this class to allow for that use case.
 *
 * It allows to provide a sequence of pre-defined "random" values.
 * It maintains an internal count of values already returned and if it larger than the sequence provided,
 * the last element in the sequence is returned.
 */
public final class TestRandom extends Random {

    private final BigDecimal[] toReturn;
    private int returnCount = 0;

    public TestRandom(int... toReturn) {
        super(0);
        this.toReturn = Arrays.stream(toReturn)
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
    }

    public TestRandom(long... toReturn) {
        super(0);
        this.toReturn = Arrays.stream(toReturn)
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
    }

    public TestRandom(double... toReturn) {
        super(0);
        this.toReturn = Arrays.stream(toReturn)
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
    }

    public TestRandom(boolean... toReturn) {
        super(0);
        this.toReturn = new BigDecimal[toReturn.length];
        for (int i = 0; i < toReturn.length; i++) {
            this.toReturn[i] = toReturn[i] ? BigDecimal.ONE : BigDecimal.ZERO;
        }
    }

    @Override
    public int nextInt(int bound) {
        returnCount++;
        if (returnCount > toReturn.length) {
            return toReturn[toReturn.length - 1].intValue();
        } else {
            return toReturn[returnCount - 1].intValue();
        }
    }

    @Override
    protected int next(int bits) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public void nextBytes(byte[] bytes) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public int nextInt() {
        return nextInt(0);
    }

    @Override
    public long nextLong() {
        returnCount++;
        if (returnCount > toReturn.length) {
            return toReturn[toReturn.length - 1].longValue();
        } else {
            return toReturn[returnCount - 1].longValue();
        }
    }

    @Override
    public boolean nextBoolean() {
        return nextInt() > 0;
    }

    @Override
    public float nextFloat() {
        returnCount++;
        if (returnCount > toReturn.length) {
            return toReturn[toReturn.length - 1].floatValue();
        } else {
            return toReturn[returnCount - 1].floatValue();
        }
    }

    @Override
    public double nextDouble() {
        returnCount++;
        if (returnCount > toReturn.length) {
            return toReturn[toReturn.length - 1].doubleValue();
        } else {
            return toReturn[returnCount - 1].doubleValue();
        }
    }

    @Override
    public synchronized double nextGaussian() {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public IntStream ints(long streamSize) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public IntStream ints() {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public LongStream longs(long streamSize) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public LongStream longs() {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public DoubleStream doubles() {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        throw new UnsupportedOperationException(getClass().getCanonicalName() + " does not support this method.");
    }

}
