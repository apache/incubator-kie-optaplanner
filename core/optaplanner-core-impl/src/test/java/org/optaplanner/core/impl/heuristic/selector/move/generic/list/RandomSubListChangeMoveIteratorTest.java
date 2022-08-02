package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.RandomSubListChangeMoveIterator.TriangleElement;

class RandomSubListChangeMoveIteratorTest {

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                arguments(1, 1, 1),
                arguments(2, 2, 1),
                arguments(3, 2, 2),
                arguments(4, 3, 1),
                arguments(5, 3, 2),
                arguments(6, 3, 3),
                arguments(7, 4, 1),
                arguments(8, 4, 2),
                arguments(9, 4, 3),
                arguments(10, 4, 4));
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    void coords(int index, int level, int indexOnLevel) {
        TriangleElement triangleElement = TriangleElement.valueOf(index);
        assertThat(triangleElement.getIndex()).isEqualTo(index);
        assertThat(triangleElement.getNthTriangle()).isEqualTo(level);
        assertThat(triangleElement.getRemainder()).isEqualTo(indexOnLevel);
    }
}
