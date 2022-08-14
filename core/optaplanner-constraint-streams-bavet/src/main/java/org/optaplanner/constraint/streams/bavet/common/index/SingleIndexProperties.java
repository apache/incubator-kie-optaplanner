package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.Objects;

final class SingleIndexProperties implements IndexProperties {

    private final Object property;

    SingleIndexProperties(Object property) {
        this.property = property;
    }

    @Override
    public <Type_> Type_ toKey(int index) {
        return toKey(1, index);
    }

    @Override
    public <Type_> Type_ toKey(int length, int startingPosition) {
        if (length != 1 || startingPosition != 0) {
            throw new IllegalArgumentException(
                    "Impossible state: length (" + length + ") and start (" + startingPosition + ").");
        }
        return (Type_) property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SingleIndexProperties)) {
            return false;
        }
        SingleIndexProperties other = (SingleIndexProperties) o;
        return Objects.equals(property, other.property);
    }

    @Override
    public int hashCode() { // Not using Objects.hash(Object...) as that would create an array on the hot path.
        return Objects.hashCode(property);
    }

    @Override
    public String toString() {
        return "[" + property + "]";
    }

}
