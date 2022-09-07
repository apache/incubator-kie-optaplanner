package org.optaplanner.core.api.score.constraint;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessorFactory;
import org.optaplanner.core.impl.domain.lookup.ClassAndPlanningIdComparator;

/**
 * Retrievable from {@link ConstraintMatchTotal#getConstraintMatchSet()}
 * and {@link Indictment#getConstraintMatchSet()}.
 *
 * <p>
 * This class implements {@link Comparable} for consistent ordering of constraint matches in visualizations.
 * The details of this ordering are unspecified and are subject to change.
 * 
 * @param <Score_> the actual score type
 */
public final class ConstraintMatch<Score_ extends Score<Score_>> implements Comparable<ConstraintMatch<Score_>> {

    private final String constraintPackage;
    private final String constraintName;
    private final String constraintId;

    private final Object justification;
    private final Score_ score;

    private Comparator<Object> classAndIdPlanningComparator;

    /**
     * @param constraintPackage never null
     * @param constraintName never null
     * @param justificationList never null, sometimes empty
     * @param score never null
     */
    public ConstraintMatch(String constraintPackage, String constraintName, List<Object> justificationList,
            Score_ score) {
        this.constraintPackage = requireNonNull(constraintPackage);
        this.constraintName = requireNonNull(constraintName);
        this.constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
        this.justification = requireNonNull(justificationList);
        this.score = requireNonNull(score);
    }

    public ConstraintMatch(String constraintPackage, String constraintName, Object justification,
            Score_ score) {
        this.constraintPackage = requireNonNull(constraintPackage);
        this.constraintName = requireNonNull(constraintName);
        this.constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
        this.justification = requireNonNull(justification);
        this.score = requireNonNull(score);
    }

    public String getConstraintPackage() {
        return constraintPackage;
    }

    public String getConstraintName() {
        return constraintName;
    }

    /**
     * Return a list of justifications for the constraint.
     * <p>
     * This method has a different meaning based on which score director the constraint comes from.
     * <ul>
     * <li>For Score DRL, it returns every object that Drools considers to be part of the match.
     * This is largely undefined.</li>
     * <li>For incremental score calculation, it returns what the calculator is implemented to return.</li>
     * <li>For constraint streams, it returns either a list of facts from the matching tuple
     * (eg. [A, B] for a bi stream), unless a custom justification function was provided,
     * in which case it returns a list with one value, which is the return value of the function.</li>
     * </ul>
     *
     * @deprecated Prefer {@link #getJustification()}.
     * @return never null
     */
    @Deprecated(forRemoval = true)
    public List<Object> getJustificationList() {
        return (List<Object>) justification;
    }

    /**
     * Return a singular justification for the constraint.
     * <p>
     * This method has a different meaning based on which score director the constraint comes from.
     * <ul>
     * <li>For Score DRL, it returns a list of all objects that Drools considers to be part of the match.
     * This is largely undefined.</li>
     * <li>For incremental score calculation, it returns what the calculator is implemented to return.</li>
     * <li>For constraint streams, it returns either a list of facts from the matching tuple
     * (eg. [A, B] for a bi stream), unless a custom justification function was provided,
     * in which case it returns the return value of that function.</li>
     * </ul>
     *
     * @return never null
     */
    public <Justification_> Justification_ getJustification() {
        return (Justification_) justification;
    }

    public Score_ getScore() {
        return score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public String getConstraintId() {
        return ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
    }

    public String getIdentificationString() {
        return getConstraintId() + "/" + justification;
    }

    @Override
    public int compareTo(ConstraintMatch<Score_> other) {
        if (!constraintId.equals(other.constraintId)) {
            return constraintId.compareTo(other.constraintId);
        } else if (!score.equals(other.score)) {
            return score.compareTo(other.score);
        } else if (justification instanceof Comparable) {
            return ((Comparable) justification).compareTo(other.justification);
        } else if (justification instanceof Collection) {
            Collection<Object> justificationCollection = (Collection<Object>) justification;
            Collection<Object> otherJustificationCollection = (Collection<Object>) other.justification;
            if (justificationCollection.size() != otherJustificationCollection.size()) {
                return Integer.compare(justificationCollection.size(), otherJustificationCollection.size());
            } else if (justificationCollection instanceof List) {
                List<Object> justificationList = (List<Object>) justificationCollection;
                List<Object> otherJustificationList = (List<Object>) otherJustificationCollection;
                Comparator<Object> comparator = getClassAndIdPlanningComparator(other);
                for (int i = 0; i < justificationCollection.size(); i++) {
                    Object left = justificationList.get(i);
                    Object right = otherJustificationList.get(i);
                    int comparison = comparator.compare(left, right);
                    if (comparison != 0) {
                        return comparison;
                    }
                }
            }
        }
        return Integer.compare(System.identityHashCode(justification),
                System.identityHashCode(other.justification));
    }

    private Comparator<Object> getClassAndIdPlanningComparator(ConstraintMatch<Score_> other) {
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

    @Override
    public String toString() {
        return getIdentificationString() + "=" + score;
    }

}
