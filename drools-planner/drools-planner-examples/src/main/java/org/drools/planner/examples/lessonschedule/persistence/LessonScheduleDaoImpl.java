package org.drools.planner.examples.lessonschedule.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.lessonschedule.domain.LessonSchedule;
import org.drools.planner.examples.manners2009.domain.Manners2009;

/**
 * @author Geoffrey De Smet
 */
public class LessonScheduleDaoImpl extends XstreamSolutionDaoImpl {

    public LessonScheduleDaoImpl() {
        super("lessonschedule", LessonSchedule.class);
    }

}