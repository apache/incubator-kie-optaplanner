package org.drools.planner.examples.itc2007.curriculumcourse.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.planner.examples.itc2007.examination.domain.Examination;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseDaoImpl extends XstreamSolutionDaoImpl {

    public CurriculumCourseDaoImpl() {
        super("itc2007/curriculumcourse", CurriculumCourseSchedule.class);
    }

}