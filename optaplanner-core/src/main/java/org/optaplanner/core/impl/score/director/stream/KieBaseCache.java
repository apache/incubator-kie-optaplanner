package org.optaplanner.core.impl.score.director.stream;

import java.util.Map;
import java.util.Objects;

import org.drools.model.Global;
import org.kie.api.KieBase;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.inliner.WeightedScoreImpacter;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;

public final class KieBaseCache<Solution_, Score_ extends Score<Score_>> {

    private final Map<DroolsConstraint<Solution_>, Score_> constraintToWeightMap;
    private final Map<DroolsConstraint<Solution_>, Global<WeightedScoreImpacter>> constraintToGlobalMap;
    private final KieBase kieBase;

    public KieBaseCache(Map<DroolsConstraint<Solution_>, Score_> constraintToWeightMap,
            Map<DroolsConstraint<Solution_>, Global<WeightedScoreImpacter>> constraintToGlobalMap,

            KieBase kieBase) {
        this.constraintToWeightMap = Objects.requireNonNull(constraintToWeightMap);
        this.constraintToGlobalMap = Objects.requireNonNull(constraintToGlobalMap);
        this.kieBase = Objects.requireNonNull(kieBase);
    }

    public Map<DroolsConstraint<Solution_>, Score_> getConstraintToWeightMap() {
        return constraintToWeightMap;
    }

    public Map<DroolsConstraint<Solution_>, Global<WeightedScoreImpacter>> getConstraintToGlobalMap() {
        return constraintToGlobalMap;
    }

    public KieBase getKieBase() {
        return kieBase;
    }

}
