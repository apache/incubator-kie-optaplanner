package org.drools.planner.examples.examination.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("PeriodHardConstraintType")
public enum PeriodHardConstraintType {
    EXAM_COINCIDENCE, // TODO rename to coincidence?
    EXCLUSION,
    AFTER
}
