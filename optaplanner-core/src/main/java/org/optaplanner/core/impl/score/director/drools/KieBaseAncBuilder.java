package org.optaplanner.core.impl.score.director.drools;

import org.drools.ancompiler.KieBaseUpdaterANC;
import org.kie.api.KieBase;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;

/**
 * Exists so that ANC references can be decoupled from the base functionality,
 * and therefore ANC can be removed from the classpath entirely if necessary.
 */
public final class KieBaseAncBuilder {

    public static KieBase build(ScoreDirectorFactoryConfig config, ClassLoader classLoader) {
        KieBase kieBase = KieBaseBuilder.build(config, classLoader);
        KieBaseUpdaterANC.generateAndSetInMemoryANC(kieBase);
        return kieBase;
    }

}
