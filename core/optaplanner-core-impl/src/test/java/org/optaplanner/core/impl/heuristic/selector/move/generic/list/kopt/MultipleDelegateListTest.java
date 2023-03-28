package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class MultipleDelegateListTest {
    @Test
    void testMoveElementsOfDelegates() {
        List<String> delegate1 = new ArrayList<>(List.of("a", "b", "c"));
        List<String> delegate2 = new ArrayList<>(List.of("d", "e"));
        List<String> delegate3 = new ArrayList<>(List.of("f"));
        MultipleDelegateList<String> combined = new MultipleDelegateList<>(delegate1, delegate2, delegate3);

        combined.moveElementsOfDelegates(new int[] { 1, 3, 5 });

        assertThat(delegate1).containsExactly("a", "b");
        assertThat(delegate2).containsExactly("c", "d");
        assertThat(delegate3).containsExactly("e", "f");

        combined.moveElementsOfDelegates(new int[] { 2, 4, 5 });

        assertThat(delegate1).containsExactly("a", "b", "c");
        assertThat(delegate2).containsExactly("d", "e");
        assertThat(delegate3).containsExactly("f");
    }

    @Test
    void testComplexMoveElementsOfDelegates() {
        List<String> delegate1 = new ArrayList<>(List.of("a", "b", "c"));
        List<String> delegate2 = new ArrayList<>(List.of("d", "e", "f", "g", "h"));
        List<String> delegate3 = new ArrayList<>(List.of("i", "j", "k", "l", "m"));
        List<String> delegate4 = new ArrayList<>(List.of("n", "o", "p", "q", "r", "s", "t", "u", "v"));
        MultipleDelegateList<String> combined = new MultipleDelegateList<>(delegate1, delegate2, delegate3, delegate4);

        combined.moveElementsOfDelegates(new int[] { 3, 13, 18, 21 });

        assertThat(delegate1).containsExactly("a", "b", "c", "d");
        assertThat(delegate2).containsExactly("e", "f", "g", "h", "i", "j", "k", "l", "m", "n");
        assertThat(delegate3).containsExactly("o", "p", "q", "r", "s");
        assertThat(delegate4).containsExactly("t", "u", "v");
        assertThat(combined.offsets).containsExactly(0, 4, 14, 19);
        assertThat(combined.delegateSizes).containsExactly(4, 10, 5, 3);

        combined.moveElementsOfDelegates(new int[] { 2, 7, 12, 21 });

        assertThat(delegate1).containsExactly("a", "b", "c");
        assertThat(delegate2).containsExactly("d", "e", "f", "g", "h");
        assertThat(delegate3).containsExactly("i", "j", "k", "l", "m");
        assertThat(delegate4).containsExactly("n", "o", "p", "q", "r", "s", "t", "u", "v");
        assertThat(combined.offsets).containsExactly(0, 3, 8, 13);
        assertThat(combined.delegateSizes).containsExactly(3, 5, 5, 9);
    }
}
