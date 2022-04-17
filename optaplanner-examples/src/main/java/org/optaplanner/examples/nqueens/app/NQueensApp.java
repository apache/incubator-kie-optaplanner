/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.nqueens.app;

import java.io.File;

import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.business.SolutionBusiness;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.persistence.NQueensXmlSolutionFileIO;
import org.optaplanner.examples.nqueens.swingui.NQueensPanel;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

/**
 * For an easy example, look at {@link NQueensHelloWorld} instead.
 */
public class NQueensApp extends CommonApp<NQueens> {

    public static final String SOLVER_CONFIG = "org/optaplanner/examples/nqueens/nqueensSolverConfig.xml";

    public static final String DATA_DIR_NAME = "nqueens";

    public static void main(String[] args) {
        try (SolutionBusiness<NQueens, ?> solutionBusiness = new NQueensApp().createSolutionBusiness()) {
            NQueens nQueens = solutionBusiness.getSolutionFileIO().read(new File("data/nqueens/unsolved/4queens.xml"));

            // Initialize the simplest known failing problem.
            nQueens.getQueenList().remove(3);
            nQueens.setN(3);
            nQueens.getQueenList().get(0).setRow(nQueens.getRowList().get(0));
            nQueens.getQueenList().get(1).setRow(nQueens.getRowList().get(2));
            nQueens.getQueenList().get(2).setRow(nQueens.getRowList().get(0));

            // Throw the error.
            solutionBusiness.solve(nQueens, solution -> {});
            System.exit(0);
        }
    }

    public NQueensApp() {
        super("N queens",
                "Place queens on a chessboard.\n\n" +
                        "No 2 queens must be able to attack each other.",
                SOLVER_CONFIG, DATA_DIR_NAME,
                NQueensPanel.LOGO_PATH);
    }

    @Override
    protected NQueensPanel createSolutionPanel() {
        return new NQueensPanel();
    }

    @Override
    public SolutionFileIO<NQueens> createSolutionFileIO() {
        return new NQueensXmlSolutionFileIO();
    }

}
