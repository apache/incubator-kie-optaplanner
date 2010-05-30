package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("FreeBefore2DaysWithAWorkDayPattern")
public class FreeBefore2DaysWithAWorkDayPattern extends Pattern {

    private DayOfWeek freeDayOfWeek;

    public DayOfWeek getFreeDayOfWeek() {
        return freeDayOfWeek;
    }

    public void setFreeDayOfWeek(DayOfWeek freeDayOfWeek) {
        this.freeDayOfWeek = freeDayOfWeek;
    }

    @Override
    public String toString() {
        return "Free on "  + freeDayOfWeek + " followed by a work day within 2 days";
    }

}
