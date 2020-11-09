package org.optaplanner.core.impl.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ConsecutiveSetTreeTest {

    @Test
    public void testNonconsecutiveNumbers() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(1);
        tree.add(3);
        tree.add(7);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(3);
        assertThat(consecutiveData.getConsecutiveLengths()).allMatch(i -> i.equals(1));
        assertThat(consecutiveData.getBreakLengths()).hasSize(2);
        assertThat(consecutiveData.getBreakLengths().get(0)).isEqualTo(1);
        assertThat(consecutiveData.getBreakLengths().get(1)).isEqualTo(3);
    }

    @Test
    public void testConsecutiveNumbers() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(1);
        tree.add(2);
        tree.add(3);

        tree.add(5);
        tree.add(6);
        tree.add(7);
        tree.add(8);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(2);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(3);
        assertThat(consecutiveData.getConsecutiveLengths().get(1)).isEqualTo(4);
        assertThat(consecutiveData.getBreakLengths()).hasSize(1);
        assertThat(consecutiveData.getBreakLengths().get(0)).isEqualTo(1);
    }

    @Test
    public void testDuplicateNumbers() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(3);
        tree.add(3);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(3);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);

        tree.remove(3);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(3);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);

        tree.remove(3);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(3);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);

        tree.remove(3);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(2);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);
    }

    @Test
    public void testConsecutiveReverseNumbers() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(3);
        tree.add(2);
        tree.add(1);

        tree.add(8);
        tree.add(7);
        tree.add(6);
        tree.add(5);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(2);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(3);
        assertThat(consecutiveData.getConsecutiveLengths().get(1)).isEqualTo(4);
        assertThat(consecutiveData.getBreakLengths()).hasSize(1);
        assertThat(consecutiveData.getBreakLengths().get(0)).isEqualTo(1);
    }

    @Test
    public void testJoinOfTwoChains() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(1);
        tree.add(2);
        tree.add(3);

        tree.add(5);
        tree.add(6);
        tree.add(7);
        tree.add(8);

        tree.add(4);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(8);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);
    }

    @Test
    public void testBreakOfChain() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(4);
        tree.add(5);
        tree.add(6);
        tree.add(7);

        tree.remove(4);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(2);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(3);
        assertThat(consecutiveData.getConsecutiveLengths().get(1)).isEqualTo(3);
        assertThat(consecutiveData.getBreakLengths()).hasSize(1);
        assertThat(consecutiveData.getBreakLengths().get(0)).isEqualTo(1);
    }

    @Test
    public void testChainRemoval() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(1);
        tree.add(2);
        tree.add(3);

        tree.add(5);
        tree.add(6);
        tree.add(7);

        tree.remove(2);
        tree.remove(1);
        tree.remove(3);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(3);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);
    }

    @Test
    public void testShorteningOfChain() {
        ConsecutiveSetTree<Integer> tree = new ConsecutiveSetTree<>(Integer.class, i -> i);
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(4);
        tree.add(5);
        tree.add(6);
        tree.add(7);

        tree.remove(7);

        ConsecutiveData<Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(6);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);

        tree.remove(1);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveLengths()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveLengths().get(0)).isEqualTo(5);
        assertThat(consecutiveData.getBreakLengths()).hasSize(0);
    }
}
