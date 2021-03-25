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

import org.drools.ancompiler.KieBaseUpdaterANC;
import org.drools.core.base.CoreComponentsBuilder;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KieBaseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieBaseUtil.class);
    private static final String ANC_ENABLED_PROPERTY = "optaplanner.drools.compile_alpha_network";

    private static boolean isAlphaNetworkCompilerEnabled() {
        String ancEnabledValue = System.getProperty(ANC_ENABLED_PROPERTY);
        if (ancEnabledValue == null || ancEnabledValue.equals(Boolean.TRUE.toString())) {
            boolean isNativeImage = CoreComponentsBuilder.isNativeImage();
            if (isNativeImage) { // ANC does not work in native images.
                LOGGER.trace("Drools Alpha Network compiler is disabled in native images.");
                return false;
            } else {
                LOGGER.trace("Drools Alpha Network compiler is enabled.");
                return true;
            }
        } else if (ancEnabledValue.equals(Boolean.FALSE.toString())) {
            LOGGER.trace("Drools Alpha Network compiler is disabled by system property ({}).", ANC_ENABLED_PROPERTY);
            return false;
        } else {
            throw new IllegalArgumentException("System property (" + ANC_ENABLED_PROPERTY + ") has an invalid value ("
                    + ancEnabledValue + ").\n" +
                    "Expected (" + Boolean.TRUE.toString() + ") or (" + Boolean.FALSE.toString() + ").");
        }
    }

    public static KieBase compileAlphaNetworkIfEnabled(KieBase kieBase) {
        if (isAlphaNetworkCompilerEnabled()) {
            KieBaseUpdaterANC.generateAndSetInMemoryANC(kieBase); // Enable Alpha Network Compiler for performance.
        }
        return kieBase;
    }

    private KieBaseUtil() {
        // No external instances.
    }

}
