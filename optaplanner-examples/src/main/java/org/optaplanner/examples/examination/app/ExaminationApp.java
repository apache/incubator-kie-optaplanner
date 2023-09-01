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

package org.optaplanner.examples.examination.app;

import java.util.Collections;
import java.util.Set;

import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.persistence.AbstractSolutionExporter;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.curriculumcourse.app.CurriculumCourseApp;
import org.optaplanner.examples.examination.domain.Examination;
import org.optaplanner.examples.examination.persistence.ExaminationExporter;
import org.optaplanner.examples.examination.persistence.ExaminationImporter;
import org.optaplanner.examples.examination.persistence.ExaminationSolutionFileIO;
import org.optaplanner.examples.examination.swingui.ExaminationPanel;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

/**
 * Examination is super optimized and a bit complex.
 * {@link CurriculumCourseApp} is arguably a better example to learn from.
 */
public class ExaminationApp extends CommonApp<Examination> {

    public static final String SOLVER_CONFIG = "org/optaplanner/examples/examination/examinationSolverConfig.xml";

    public static final String DATA_DIR_NAME = "examination";

    public static void main(String[] args) {
        prepareSwingEnvironment();
        new ExaminationApp().init();
    }

    public ExaminationApp() {
        super("Exam timetabling",
                "Official competition name: ITC 2007 track1 - Examination timetabling\n\n" +
                        "Assign exams to timeslots and rooms.",
                SOLVER_CONFIG, DATA_DIR_NAME,
                ExaminationPanel.LOGO_PATH);
    }

    @Override
    protected ExaminationPanel createSolutionPanel() {
        return new ExaminationPanel();
    }

    @Override
    public SolutionFileIO<Examination> createSolutionFileIO() {
        return new ExaminationSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<Examination>> createSolutionImporters() {
        return Collections.singleton(new ExaminationImporter());
    }

    @Override
    protected Set<AbstractSolutionExporter<Examination>> createSolutionExporters() {
        return Collections.singleton(new ExaminationExporter());
    }

}
