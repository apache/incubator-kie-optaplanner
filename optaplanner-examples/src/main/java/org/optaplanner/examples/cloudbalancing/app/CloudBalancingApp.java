/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.examples.cloudbalancing.app;

import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.persistence.CloudBalanceSolutionFileIO;
import org.optaplanner.examples.cloudbalancing.swingui.CloudBalancingPanel;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

/**
 * For an easy example, look at {@link CloudBalancingHelloWorld} instead.
 */
public class CloudBalancingApp extends CommonApp<CloudBalance> {

    public static final String SOLVER_CONFIG = "org/optaplanner/examples/cloudbalancing/cloudBalancingSolverConfig.xml";

    public static final String DATA_DIR_NAME = "cloudbalancing";

    public static void main(String[] args) {
        prepareSwingEnvironment();
        new CloudBalancingApp().init();
    }

    public CloudBalancingApp() {
        super("Cloud balancing",
                "Assign processes to computers.\n\n" +
                        "Each computer must have enough hardware to run all of its processes.\n" +
                        "Each used computer inflicts a maintenance cost.",
                SOLVER_CONFIG, DATA_DIR_NAME,
                CloudBalancingPanel.LOGO_PATH);
    }

    @Override
    protected CloudBalancingPanel createSolutionPanel() {
        return new CloudBalancingPanel();
    }

    @Override
    public SolutionFileIO<CloudBalance> createSolutionFileIO() {
        return new CloudBalanceSolutionFileIO();
    }

}
