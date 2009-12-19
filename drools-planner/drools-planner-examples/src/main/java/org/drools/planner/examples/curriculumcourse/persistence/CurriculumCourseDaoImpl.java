package org.drools.planner.examples.curriculumcourse.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.curriculumcourse.domain.CurriculumCourseSchedule;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseDaoImpl extends XstreamSolutionDaoImpl {

    public CurriculumCourseDaoImpl() {
        super("curriculumcourse", CurriculumCourseSchedule.class);
    }

}