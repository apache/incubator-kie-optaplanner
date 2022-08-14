package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.Arrays;

import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.core.impl.util.Quadruple;
import org.optaplanner.core.impl.util.Triple;

final class ManyIndexProperties implements IndexProperties {

    private final Object[] properties;

    ManyIndexProperties(Object... properties) {
        this.properties = properties;
    }

    @Override
    public <Type_> Type_ toKey(int index) {
        return (Type_) properties[index];
    }

    @Override
    public <Type_> Type_ toKey(int length, int startingPosition) {
        switch (length) {
            case 1:
                return toKey(startingPosition);
            case 2:
                return (Type_) Pair.of(toKey(startingPosition), toKey(startingPosition + 1));
            case 3:
                return (Type_) Triple.of(toKey(startingPosition), toKey(startingPosition + 1),
                        toKey(startingPosition + 2));
            case 4:
                return (Type_) Quadruple.of(toKey(startingPosition), toKey(startingPosition + 1),
                        toKey(startingPosition + 2), toKey(startingPosition + 3));
            default:
                return (Type_) new IndexerKey(this, startingPosition, startingPosition + length);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ManyIndexProperties)) {
            return false;
        }
        ManyIndexProperties other = (ManyIndexProperties) o;
        return Arrays.equals(properties, other.properties);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(properties);
    }

    @Override
    public String toString() {
        return Arrays.toString(properties);
    }

}
