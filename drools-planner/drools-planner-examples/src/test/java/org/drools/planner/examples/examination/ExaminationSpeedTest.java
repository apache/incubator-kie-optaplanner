/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.examples.examination;

import java.io.File;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.examples.common.app.SolverSpeedTest;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.examination.persistence.ExaminationDaoImpl;
import org.junit.Test;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationSpeedTest extends SolverSpeedTest {

    @Override
    protected String createSolverConfigResource() {
        return "/org/drools/planner/examples/examination/solver/examinationSolverConfig.xml";
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new ExaminationDaoImpl();
    }

    // ************************************************************************
    // Tests
    // ************************************************************************

    @Test(timeout = 60000)
    public void solveComp_set1_initialized() {
        runSpeedTest(new File("data/examination/unsolved/exam_comp_set1_initialized.xml"), "0hard/-7925soft");
    }

}
