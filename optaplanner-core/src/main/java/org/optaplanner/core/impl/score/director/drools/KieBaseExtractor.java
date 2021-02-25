/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.director.drools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.drools.core.io.impl.ClassPathResource;
import org.drools.core.io.impl.FileSystemResource;
import org.drools.modelcompiler.ExecutableModelProject;
import org.kie.api.KieBase;
import org.kie.api.conf.KieBaseMutabilityOption;
import org.kie.internal.builder.conf.PropertySpecificOption;
import org.kie.internal.utils.KieHelper;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.util.ConfigUtils;

public class KieBaseExtractor {

    private static Map<String, KieBase> scoreDrlListToKieBaseMap = new HashMap<>();

    public static void useScoreDrlListToKieBaseMap(Map<String, KieBase> map) {
        scoreDrlListToKieBaseMap = map;
    }

    public static String getKieBaseKey(ScoreDirectorFactoryConfig config) {
        List<String> kieBaseKey =
                new ArrayList<>(Optional.ofNullable(config.getScoreDrlList()).map(Collection::size).orElse(0) +
                        Optional.ofNullable(config.getScoreDrlFileList()).map(Collection::size).orElse(0));
        kieBaseKey.addAll(Optional.ofNullable(config.getScoreDrlList()).orElse(Collections.emptyList()));
        for (File resource : Optional.ofNullable(config.getScoreDrlFileList()).orElse(Collections.emptyList())) {
            kieBaseKey.add(resource.getAbsolutePath());
        }

        return String.join(":", kieBaseKey);
    }

    public static KieBase extractKieBase(ScoreDirectorFactoryConfig config, ClassLoader classLoader) {
        return scoreDrlListToKieBaseMap.computeIfAbsent(getKieBaseKey(config), (key) -> {
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

            try {
                return kieHelper.build(ExecutableModelProject.class, KieBaseMutabilityOption.DISABLED);
            } catch (Exception ex) {
                throw new IllegalStateException("There is an error in a scoreDrl or scoreDrlFile.", ex);
            }
        });
    }

    private KieBaseExtractor() {
    }
}
