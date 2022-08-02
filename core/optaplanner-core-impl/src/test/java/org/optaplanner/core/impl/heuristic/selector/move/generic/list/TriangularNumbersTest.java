package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TriangularNumbersTest {

    static Stream<Arguments> nthProvider() {
        return Stream.of(
                arguments(0, 0),
                arguments(1, 1),
                arguments(2, 3),
                arguments(3, 6),
                arguments(4, 10),
                arguments(5, 15));
    }

    @ParameterizedTest
    @MethodSource("nthProvider")
    void nth(int n, int nthTriangularNumber) {
        assertThat(TriangularNumbers.nth(n)).isEqualTo(nthTriangularNumber);
    }

    @ParameterizedTest
    @MethodSource("nthProvider")
    void root(int n, int nthTriangularNumber) {
        assertThat(TriangularNumbers.root(nthTriangularNumber)).isEqualTo(n);
    }

    @ParameterizedTest
    @MethodSource("nthProvider")
    void rootFloorExact(int n, int nthTriangularNumber) {
        assertThat(TriangularNumbers.rootFloor(nthTriangularNumber)).isEqualTo(n);
    }

    @ParameterizedTest
    @MethodSource("nthProvider")
    void rootCeilExact(int n, int nthTriangularNumber) {
        assertThat(TriangularNumbers.rootCeil(nthTriangularNumber)).isEqualTo(n);
    }

    static Stream<Arguments> rootFloorProvider() {
        return Stream.of(
                arguments(2, 1),
                arguments(4, 2),
                arguments(5, 2),
                arguments(7, 3),
                arguments(8, 3),
                arguments(9, 3));
    }

    @ParameterizedTest
    @MethodSource("rootFloorProvider")
    void rootFloor(int x, int lesserTriangle) {
        assertThat(TriangularNumbers.rootFloor(x)).isEqualTo(lesserTriangle);
    }

    @ParameterizedTest
    @MethodSource("rootFloorProvider")
    void rootCeil(int x, int lesserTriangle) {
        assertThat(TriangularNumbers.rootCeil(x)).isEqualTo(lesserTriangle + 1);
    }
}
