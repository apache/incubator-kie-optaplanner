package org.optaplanner.core.impl.score.director.drools;

import java.io.File;
import java.util.Map;

import org.drools.core.io.impl.ClassPathResource;
import org.drools.core.io.impl.FileSystemResource;
import org.drools.modelcompiler.ExecutableModelProject;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.KieBaseMutabilityOption;
import org.kie.internal.builder.conf.PropertySpecificOption;
import org.kie.internal.utils.KieHelper;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.util.ConfigUtils;

public final class KieBaseBuilder {

    public static KieBase build(ScoreDirectorFactoryConfig config, ClassLoader classLoader) {
        // Can't put this code in KieBaseExtractor since it reference
        // KieRuntimeBuilder, which is an optional dependency
        KieHelper kieHelper = new KieHelper(PropertySpecificOption.ALLOWED)
                .setClassLoader(classLoader);
        if (!ConfigUtils.isEmptyCollection(config.getScoreDrlList())) {
            for (String scoreDrl : config.getScoreDrlList()) {
                if (scoreDrl == null) {
                    throw new IllegalArgumentException("The scoreDrl (" + scoreDrl + ") cannot be null.");
                }
                kieHelper.addResource(new ClassPathResource(scoreDrl, classLoader));
            }
        }
        if (!ConfigUtils.isEmptyCollection(config.getScoreDrlFileList())) {
            for (File scoreDrlFile : config.getScoreDrlFileList()) {
                kieHelper.addResource(new FileSystemResource(scoreDrlFile));
            }
        }
        KieBaseConfiguration kieBaseConfiguration = buildKieBaseConfiguration(config, KieServices.get());
        kieBaseConfiguration.setOption(KieBaseMutabilityOption.DISABLED); // Performance improvement.
        return kieHelper.build(ExecutableModelProject.class, kieBaseConfiguration);
    }

    private static KieBaseConfiguration buildKieBaseConfiguration(ScoreDirectorFactoryConfig config, KieServices kieServices) {
        KieBaseConfiguration kieBaseConfiguration = kieServices.newKieBaseConfiguration();
        if (config.getKieBaseConfigurationProperties() != null) {
            for (Map.Entry<String, String> entry : config.getKieBaseConfigurationProperties().entrySet()) {
                kieBaseConfiguration.setProperty(entry.getKey(), entry.getValue());
            }
        }
        return kieBaseConfiguration;
    }

}
