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

package org.optaplanner.examples.flightcrewscheduling.swingui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightCrewSolution;
import org.optaplanner.examples.flightcrewscheduling.persistence.FlightCrewSchedulingXlsxFileIO;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class FlightCrewSchedulingPanel extends SolutionPanel<FlightCrewSolution> {

    public static final String LOGO_PATH =
            "/org/optaplanner/examples/flightcrewscheduling/swingui/flightCrewSchedulingLogo.png";

    private FlightCrewSchedulingWorldPanel flightCrewSchedulingWorldPanel;

    public FlightCrewSchedulingPanel() {
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton button = new JButton("Show in LibreOffice or Excel");
        button.addActionListener(event -> {
            SolutionFileIO<FlightCrewSolution> solutionFileIO = new FlightCrewSchedulingXlsxFileIO();
            File tempFile;
            try {
                tempFile = File.createTempFile(solutionBusiness.getSolutionFileName(),
                        "." + solutionFileIO.getOutputFileExtension());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create temp file.", e);
            }
            solutionFileIO.write(solutionBusiness.getSolution(), tempFile);
            try {
                Desktop.getDesktop().open(tempFile);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to show temp file (" + tempFile + ") in LibreOffice or Excel.", e);
            }
        });
        buttonPanel.add(button);
        buttonPanel.add(new JLabel("Changes to that file are ignored unless you explicitly save it there and open it here."));
        add(buttonPanel, BorderLayout.NORTH);
        flightCrewSchedulingWorldPanel = new FlightCrewSchedulingWorldPanel(this);
        add(flightCrewSchedulingWorldPanel, BorderLayout.CENTER);
    }

    @Override
    public boolean isWrapInScrollPane() {
        return false;
    }

    @Override
    public void resetPanel(FlightCrewSolution solution) {
        flightCrewSchedulingWorldPanel.resetPanel(solution);
    }

    @Override
    public void updatePanel(FlightCrewSolution solution) {
        flightCrewSchedulingWorldPanel.updatePanel(solution);
    }

}
