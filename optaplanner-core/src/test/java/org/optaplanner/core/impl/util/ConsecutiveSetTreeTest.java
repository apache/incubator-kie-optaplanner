package org.optaplanner.core.impl.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

public class ConsecutiveSetTreeTest {

    private ConsecutiveSetTree<Integer, Integer, Integer> getIntegerConsecutiveSetTree() {
        return new ConsecutiveSetTree<>(Integer.class, i -> i, (a, b) -> b - a, 1, 0);
    }

    @Test
    public void testNonconsecutiveNumbers() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(3);
        tree.add(7);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(3);
        assertThat(consecutiveData.getConsecutiveSequences()).allMatch(seq -> seq.getLength() == 1);
        assertThat(consecutiveData.getBreaks()).hasSize(2);
        assertThat(consecutiveData.getBreaks().get(0)).isEqualTo(2);
        assertThat(consecutiveData.getBreaks().get(1)).isEqualTo(4);
    }

    @Test
    public void testConsecutiveNumbers() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(2);
        tree.add(3);

        tree.add(5);
        tree.add(6);
        tree.add(7);
        tree.add(8);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(2);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getConsecutiveSequences().get(1).getLength()).isEqualTo(4);
        assertThat(consecutiveData.getBreaks()).hasSize(1);
        assertThat(consecutiveData.getBreaks().get(0)).isEqualTo(2);
    }

    @Test
    public void testDuplicateNumbers() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(3);
        tree.add(3);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getBreaks()).hasSize(0);

        tree.remove(3);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getBreaks()).hasSize(0);

        tree.remove(3);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getBreaks()).hasSize(0);

        tree.remove(3);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(2);
        assertThat(consecutiveData.getBreaks()).hasSize(0);
    }

    @Test
    public void testConsecutiveReverseNumbers() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(3);
        tree.add(2);
        tree.add(1);

        tree.add(8);
        tree.add(7);
        tree.add(6);
        tree.add(5);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(2);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getConsecutiveSequences().get(1).getLength()).isEqualTo(4);
        assertThat(consecutiveData.getBreaks()).hasSize(1);
        assertThat(consecutiveData.getBreaks().get(0)).isEqualTo(2);
    }

    @Test
    public void testJoinOfTwoChains() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(2);
        tree.add(3);

        tree.add(5);
        tree.add(6);
        tree.add(7);
        tree.add(8);

        tree.add(4);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(8);
        assertThat(consecutiveData.getBreaks()).hasSize(0);
    }

    @Test
    public void testBreakOfChain() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(4);
        tree.add(5);
        tree.add(6);
        tree.add(7);

        tree.remove(4);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(2);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getConsecutiveSequences().get(1).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getBreaks()).hasSize(1);
        assertThat(consecutiveData.getBreaks().get(0)).isEqualTo(2);
    }

    @Test
    public void testChainRemoval() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(2);
        tree.add(3);

        tree.add(5);
        tree.add(6);
        tree.add(7);

        tree.remove(2);
        tree.remove(1);
        tree.remove(3);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(3);
        assertThat(consecutiveData.getBreaks()).hasSize(0);
    }

    @Test
    public void testShorteningOfChain() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(4);
        tree.add(5);
        tree.add(6);
        tree.add(7);

        tree.remove(7);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(6);
        assertThat(consecutiveData.getBreaks()).hasSize(0);

        tree.remove(1);
        consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences()).hasSize(1);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getLength()).isEqualTo(5);
        assertThat(consecutiveData.getBreaks()).hasSize(0);
    }

    private static class Timeslot {
        OffsetDateTime from;
        OffsetDateTime to;

        public Timeslot(int fromIndex, int toIndex) {
            from = OffsetDateTime.of(2000, 1, fromIndex + 1, 0, 0, 0, 0, ZoneOffset.UTC);
            to = OffsetDateTime.of(2000, 1, toIndex + 1, 0, 0, 0, 0, ZoneOffset.UTC);
        }
    }

    @Test
    public void testTimeslotConsecutive() {
        ConsecutiveSetTree<Timeslot, OffsetDateTime, Duration> tree = new ConsecutiveSetTree<>(Timeslot.class,
                ts -> ts.from, Duration::between, Duration.ofDays(1), Duration.ZERO);

        Timeslot t1 = new Timeslot(0, 1);
        Timeslot t2 = new Timeslot(1, 2);

        Timeslot t3 = new Timeslot(3, 4);
        Timeslot t4 = new Timeslot(4, 5);
        Timeslot t5 = new Timeslot(5, 6);

        tree.add(t4);
        tree.add(t2);
        tree.add(t4);
        tree.add(t3);
        tree.add(t1);
        tree.add(t5);

        ConsecutiveData<Timeslot, Duration> consecutiveData = tree.getConsecutiveData();
        assertThat(consecutiveData.getConsecutiveSequences().size()).isEqualTo(2);
        assertThat(consecutiveData.getConsecutiveSequences().get(0).getItems()).containsExactly(t1, t2);
        assertThat(consecutiveData.getConsecutiveSequences().get(1).getItems()).containsExactly(t3, t4, t5);

        assertThat(consecutiveData.getBreaks().size()).isEqualTo(1);
        assertThat(consecutiveData.getBreaks().get(0)).isEqualTo(Duration.ofDays(2));
    }
}
