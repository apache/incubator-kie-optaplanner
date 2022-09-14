package org.optaplanner.core.api.score.stream;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessorFactory;
import org.optaplanner.core.impl.domain.lookup.ClassAndPlanningIdComparator;

/**
 * Default implementation of {@link ConstraintJustification}, returned by {@link ConstraintMatch#getJustification()}
 * unless the user defined a custom justification mapping.
 */
public final class DefaultConstraintJustification
        implements ConstraintJustification, Comparable<DefaultConstraintJustification> {

    private static final DefaultConstraintJustification EMPTY = DefaultConstraintJustification.of(Collections.emptyList());

    public static DefaultConstraintJustification empty() {
        return EMPTY;
    }

    public static DefaultConstraintJustification of(Object fact) {
        return new DefaultConstraintJustification(List.of(fact));
    }

    public static DefaultConstraintJustification of(Object factA, Object factB) {
        return new DefaultConstraintJustification(List.of(factA, factB));
    }

    public static DefaultConstraintJustification of(Object factA, Object factB, Object factC) {
        return new DefaultConstraintJustification(List.of(factA, factB, factC));
    }

    public static DefaultConstraintJustification of(Object factA, Object factB, Object factC, Object factD) {
        return new DefaultConstraintJustification(List.of(factA, factB, factC, factD));
    }

    public static DefaultConstraintJustification of(Object... facts) {
        return new DefaultConstraintJustification(List.of(facts));
    }

    public static DefaultConstraintJustification of(List<Object> facts) {
        return new DefaultConstraintJustification(facts);
    }

    private final List<Object> facts;
    private Comparator<Object> classAndIdPlanningComparator;

    private DefaultConstraintJustification(List<Object> facts) {
        this.facts = facts;
    }

    public List<Object> getFacts() {
        return facts;
    }

    @Override
    public String toString() {
        return facts.toString();
    }

    @Override
    public int compareTo(DefaultConstraintJustification other) {
        List<Object> justificationList = this.getFacts();
        List<Object> otherJustificationList = other.getFacts();
        if (justificationList != otherJustificationList) {
            if (justificationList.size() != otherJustificationList.size()) {
                return Integer.compare(justificationList.size(), otherJustificationList.size());
            } else {
                Comparator<Object> comparator = getClassAndIdPlanningComparator(other);
                for (int i = 0; i < justificationList.size(); i++) {
                    Object left = justificationList.get(i);
                    Object right = otherJustificationList.get(i);
                    int comparison = comparator.compare(left, right);
                    if (comparison != 0) {
                        return comparison;
                    }
                }
            }
        }
        return Integer.compare(System.identityHashCode(this), System.identityHashCode(other));
    }

    private Comparator<Object> getClassAndIdPlanningComparator(DefaultConstraintJustification other) {
        /*
         * The comparator performs some expensive operations, which can be cached.
         * For optimal performance, this cache (MemberAccessFactory) needs to be shared between comparators.
         * In order to prevent the comparator from being shared in a static field creating a de-facto memory leak,
         * we cache the comparator inside this class, and we minimize the number of instances that will be created
         * by creating the comparator when none of the constraint matches already carry it,
         * and we store it in both.
         */
        if (classAndIdPlanningComparator != null) {
            return classAndIdPlanningComparator;
        } else if (other.classAndIdPlanningComparator != null) {
            return other.classAndIdPlanningComparator;
        } else {
            /*
             * FIXME Using reflection will break Quarkus once we don't open up classes for reflection any more.
             * Use cases which need to operate safely within Quarkus should use SolutionDescriptor's MemberAccessorFactory.
             */
            classAndIdPlanningComparator =
                    new ClassAndPlanningIdComparator(new MemberAccessorFactory(), DomainAccessType.REFLECTION, false);
            other.classAndIdPlanningComparator = classAndIdPlanningComparator;
            return classAndIdPlanningComparator;
        }
    }

}
