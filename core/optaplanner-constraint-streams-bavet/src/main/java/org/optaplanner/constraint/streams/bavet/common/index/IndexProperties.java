package org.optaplanner.constraint.streams.bavet.common.index;

/**
 * Index properties are cached in tuples and each tuple carries its unique instance.
 * <p>
 * Index properties are shallow immutable and implement {@link Object#equals(Object)} and {@link Object#hashCode()}.
 */
public interface IndexProperties {

    /**
     * Retrieves index property at a given position.
     * 
     * @param index
     * @return never null
     * @param <Type_> {@link ComparisonIndexer} will expect this to implement {@link Comparable}.
     */
    <Type_> Type_ toKey(int index);

    /**
     * Retrieves an object to serve as a key in an index.
     * Instances retrieved using the same arguments must be {@link #equals(Object) equal}.
     *
     * @param length how many of the available properties to use
     * @param startingPosition index of the first property to use
     * @return never null
     * @param <Type_> expected type of the key
     */
    <Type_> Type_ toKey(int length, int startingPosition);

}
