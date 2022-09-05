package org.optaplanner.constraint.streams.common.uni;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.optaplanner.constraint.streams.common.RetrievalSemantics;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.constraint.streams.common.bi.BiJoinerComber;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;

public interface InnerUniConstraintStream<A> extends UniConstraintStream<A> {

    RetrievalSemantics getRetrievalSemantics();

    /**
     * This method returns true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default <B> BiConstraintStream<A, B> join(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        if (getRetrievalSemantics() == RetrievalSemantics.STANDARD) {
            return join(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            return join(getConstraintFactory().from(otherClass), joiners);
        }
    }

    /**
     * Allows {@link ConstraintFactory#forEachUniquePair(Class)} to reuse the joiner combing logic.
     * 
     * @param otherStream never null
     * @param joinerComber never null
     * @param <B>
     * @return never null
     */
    <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream, BiJoinerComber<A, B> joinerComber);

    @Override
    default UniConstraintStream<A> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy(Function.identity());
        }
    }

}
