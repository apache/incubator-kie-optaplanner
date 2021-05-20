package org.optaplanner.core.impl.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class ConsecutiveSetTreeTest {

    private ConsecutiveSetTree<Integer, Integer, Integer> getIntegerConsecutiveSetTree() {
        return new ConsecutiveSetTree<>(i -> i, (a, b) -> b - a, 1, 0);
    }

    @Test
    public void testNonconsecutiveNumbers() {
        ConsecutiveSetTree<Integer, Integer, Integer> tree = getIntegerConsecutiveSetTree();
        tree.add(1);
        tree.add(3);
        tree.add(7);

        ConsecutiveData<Integer, Integer> consecutiveData = tree.getConsecutiveData();

        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        assertThat(sequenceList).hasSize(3);
        Iterable<Break<Integer, Integer>> breakList = consecutiveData.getBreaks();
        Iterator<Break<Integer, Integer>> breakIterator = breakList.iterator();
        Break<Integer, Integer> theBreak = breakIterator.next();
        assertThat(breakList).hasSize(2);

        assertThat(consecutiveData.getConsecutiveSequences()).allMatch(seq -> seq.getLength() == 1);
        assertThat(theBreak).isEqualTo(new Break<>(3, 1, 2));

        theBreak = breakIterator.next();
        assertThat(theBreak).isEqualTo(new Break<>(7, 3, 4));
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
        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        assertThat(sequenceList).hasSize(2);
        Iterator<Sequence<Integer>> sequenceIterator = sequenceList.iterator();
        Iterable<Break<Integer, Integer>> breakList = consecutiveData.getBreaks();
        Iterator<Break<Integer, Integer>> breakIterator = breakList.iterator();
        assertThat(breakList).hasSize(1);

        assertThat(sequenceIterator.next().getLength()).isEqualTo(3);
        assertThat(sequenceIterator.next().getLength()).isEqualTo(4);
        assertThat(breakIterator.next()).isEqualTo(new Break<>(5, 3, 2));
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
        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        assertThat(sequenceList).hasSize(1);
        Iterator<Sequence<Integer>> sequenceIterator = sequenceList.iterator();
        Sequence<Integer> sequence = sequenceIterator.next();
        Iterable<Break<Integer, Integer>> breakList = consecutiveData.getBreaks();
        assertThat(breakList).hasSize(0);

        assertThat(sequence.getLength()).isEqualTo(3);
        assertThat(consecutiveData.getBreaks()).hasSize(0);

        tree.remove(3);
        assertThat(sequenceList).hasSize(1);
        assertThat(sequenceList.iterator().next().getLength()).isEqualTo(3);
        assertThat(breakList).hasSize(0);

        tree.remove(3);
        assertThat(sequenceList).hasSize(1);
        assertThat(sequenceList.iterator().next().getLength()).isEqualTo(3);
        assertThat(breakList).hasSize(0);

        tree.remove(3);
        assertThat(sequenceList).hasSize(1);
        assertThat(sequenceList.iterator().next().getLength()).isEqualTo(2);
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
        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        assertThat(sequenceList).hasSize(2);
        Iterator<Sequence<Integer>> sequenceIterator = sequenceList.iterator();
        Iterable<Break<Integer, Integer>> breakList = consecutiveData.getBreaks();
        Iterator<Break<Integer, Integer>> breakIterator = breakList.iterator();
        assertThat(breakList).hasSize(1);

        assertThat(sequenceIterator.next().getLength()).isEqualTo(3);
        assertThat(sequenceIterator.next().getLength()).isEqualTo(4);
        assertThat(breakIterator.next()).isEqualTo(new Break<>(5, 3, 2));
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
        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        Iterator<Sequence<Integer>> sequenceIterator = sequenceList.iterator();

        assertThat(sequenceList).hasSize(1);
        assertThat(sequenceIterator.next().getLength()).isEqualTo(8);
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
        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        assertThat(sequenceList).hasSize(2);
        Iterator<Sequence<Integer>> sequenceIterator = sequenceList.iterator();
        Iterable<Break<Integer, Integer>> breakList = consecutiveData.getBreaks();
        Iterator<Break<Integer, Integer>> breakIterator = breakList.iterator();
        assertThat(breakList).hasSize(1);

        assertThat(sequenceList).hasSize(2);
        assertThat(sequenceIterator.next().getLength()).isEqualTo(3);
        assertThat(sequenceIterator.next().getLength()).isEqualTo(3);
        assertThat(breakList).hasSize(1);
        assertThat(breakIterator.next()).isEqualTo(new Break<>(5, 3, 2));
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
        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        assertThat(sequenceList).hasSize(1);
        Iterator<Sequence<Integer>> sequenceIterator = sequenceList.iterator();

        assertThat(sequenceIterator.next().getLength()).isEqualTo(3);
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
        Iterable<Sequence<Integer>> sequenceList = consecutiveData.getConsecutiveSequences();
        Iterator<Sequence<Integer>> sequenceIterator = sequenceList.iterator();

        assertThat(sequenceList).hasSize(1);
        assertThat(sequenceIterator.next().getLength()).isEqualTo(6);
        assertThat(consecutiveData.getBreaks()).hasSize(0);

        tree.remove(1);
        assertThat(sequenceList).hasSize(1);
        assertThat(sequenceList.iterator().next().getLength()).isEqualTo(5);
        assertThat(consecutiveData.getBreaks()).hasSize(0);
    }

    @Test
    public void testRandomSequences() {
        Random random = new Random(1);
        TreeMap<Integer, Integer> valueToCountMap = new TreeMap<>();

        // Tree we are testing is at most difference 2
        ConsecutiveSetTree<Integer, Integer, Integer> tree = new ConsecutiveSetTree<>(i -> i, (a, b) -> b - a, 2, 0);
        ;

        for (int i = 0; i < 10000; i++) {
            int value = random.nextInt(64);
            String op;
            if (valueToCountMap.containsKey(value) && random.nextDouble() < 0.75) {
                op = valueToCountMap.keySet().stream().map(Object::toString)
                        .collect(Collectors.joining(", ", "Removing " + value + " from [", "]"));
                valueToCountMap.computeIfPresent(value, (key, count) -> (count == 1) ? null : count - 1);
                tree.remove(value);
            } else {
                op = valueToCountMap.keySet().stream().map(Object::toString)
                        .collect(Collectors.joining(", ", "Adding " + value + " to [", "]"));
                valueToCountMap.merge(value, 1, Integer::sum);
                tree.add(value);
            }

            ConsecutiveSetTree<Integer, Integer, Integer> freshTree =
                    new ConsecutiveSetTree<>(val -> val, (a, b) -> b - a, 2, 0);
            ;
            for (Map.Entry<Integer, Integer> entry : valueToCountMap.entrySet()) {
                IntStream.range(0, entry.getValue()).map(index -> entry.getKey()).forEach(freshTree::add);
            }
            assertThat(tree.getConsecutiveSequences()).as("Mismatched Sequence: " + op)
                    .isEqualTo(freshTree.getConsecutiveSequences());
            assertThat(tree.getBreaks()).as("Mismatched Break: " + op).isEqualTo(freshTree.getBreaks());
        }
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
        ConsecutiveSetTree<Timeslot, OffsetDateTime, Duration> tree = new ConsecutiveSetTree<>(
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
        Iterable<Sequence<Timeslot>> sequenceList = consecutiveData.getConsecutiveSequences();
        assertThat(sequenceList).hasSize(2);
        Iterator<Sequence<Timeslot>> sequenceIterator = sequenceList.iterator();
        Iterable<Break<Timeslot, Duration>> breakList = consecutiveData.getBreaks();
        Iterator<Break<Timeslot, Duration>> breakIterator = breakList.iterator();
        assertThat(breakList).hasSize(1);

        assertThat(sequenceList).hasSize(2);
        assertThat(sequenceIterator.next().getItems()).containsExactly(t1, t2);
        assertThat(sequenceIterator.next().getItems()).containsExactly(t3, t4, t5);

        assertThat(breakList).hasSize(1);
        assertThat(breakIterator.next()).isEqualTo(new Break<>(t3, t2, Duration.ofDays(2)));
    }
}
