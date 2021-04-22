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

package org.optaplanner.quarkus.devui;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.microprofile.config.ConfigProvider;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.io.jaxb.SolverConfigIO;

import io.quarkus.arc.Arc;
import io.quarkus.devconsole.runtime.spi.DevConsolePostHandler;
import io.quarkus.devconsole.runtime.spi.FlashScopeUtil;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class SolverConfigDevUIHandlerRecorder {
    public Handler<RoutingContext> updateSolverPropertiesHandler() {
        return new DevConsolePostHandler() {
            @Override
            protected void handlePost(RoutingContext event, MultiMap form)
                    throws Exception {
                String property = form.get("name");
                String value = form.get("value");
                SolverConfig solverConfig = Arc.container().instance(SolverConfig.class).get();

                switch (property) {
                    case "Environment Mode":
                        try {
                            EnvironmentMode environmentMode = EnvironmentMode.valueOf(value);
                            solverConfig.setEnvironmentMode(environmentMode);
                        } catch (IllegalArgumentException e) {
                            flashMessage(event, "environmentMode (" + value + ") is not a valid Environment Mode.",
                                    FlashScopeUtil.FlashMessageStatus.ERROR);
                            return;
                        }
                        break;

                    default:
                        flashMessage(event, "Property (" + property + ") is not an existing property in SolverConfig",
                                FlashScopeUtil.FlashMessageStatus.ERROR);
                        return;
                }

                Path solverConfigLocation =
                        Paths.get(ConfigProvider.getConfig().getOptionalValue("solverConfigXml", String.class)
                                .orElse("solverConfig.xml"));

                if (!solverConfigLocation.isAbsolute()) {
                    solverConfigLocation = Paths.get("..", "src", "main", "resources").toAbsolutePath().normalize()
                            .resolve(solverConfigLocation);
                }
                FileWriter writer = new FileWriter(solverConfigLocation.toFile());
                SolverConfigIO solverConfigIO = new SolverConfigIO();
                solverConfigIO.write(solverConfig, writer);

                flashMessage(event, "Updated Solver Config");
            }
        };
    }
}
