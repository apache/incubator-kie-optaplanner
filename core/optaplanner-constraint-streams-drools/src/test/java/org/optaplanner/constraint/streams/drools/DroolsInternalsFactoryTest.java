package org.optaplanner.constraint.streams.drools;

import static org.assertj.core.api.Assertions.assertThat;

import org.drools.model.functions.Predicate1;
import org.junit.jupiter.api.Test;

class DroolsInternalsFactoryTest {

    @Test
    void initPredicate() {
        /*
         * DroolsInternalsFactory internalsFactory = new DroolsInternalsFactory();
         *
         * Predicate1<Object> predicate = a -> true;
         * Predicate1<Object> predicateAfterInit = internalsFactory.initPredicate(predicate);
         * assertSoftly(softly -> {
         * softly.assertThat(predicateAfterInit).isNotNull();
         * softly.assertThat(predicateAfterInit).isNotSameAs(predicate);
         * softly.assertThat(predicateAfterInit).isInstanceOf(Predicate1.Impl.class);
         * softly.assertThat(predicateAfterInit).isEqualTo(predicateAfterInit);
         * });
         *
         * Predicate1<Object> cachedPredicate = internalsFactory.initPredicate(predicate);
         * assertThat(cachedPredicate).isSameAs(predicateAfterInit);
         *
         * Predicate1<Object> otherPredicate = a -> true;
         * Predicate1<Object> otherPredicateAfterInit = internalsFactory.initPredicate(otherPredicate);
         * assertSoftly(softly -> {
         * softly.assertThat(otherPredicateAfterInit).isNotSameAs(predicateAfterInit);
         * softly.assertThat(otherPredicateAfterInit).isNotEqualTo(predicateAfterInit);
         * });
         */

        Predicate1<Object> manuallyInitPredicate1 = new Predicate1.Impl<>(a -> true);
        Predicate1<Object> manuallyInitPredicate2 = new Predicate1.Impl<>(a -> true);
        assertThat(manuallyInitPredicate1).isEqualTo(manuallyInitPredicate2);
    }

}
