package org.drools.solver.examples.itc2007.examination.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class InstitutionalWeighting extends AbstractPersistable implements Comparable<InstitutionalWeighting> {

    private int twoInARowPenality;
    private int twoInADayPenality;
    private int periodSpreadLength;
    private int periodSpreadPenality;
    private int mixedDurationPenality;
    private int frontLoadLargeTopicSize;
    private int frontLoadLastPeriodSize;
    private int frontLoadPenality;

    public int getTwoInARowPenality() {
        return twoInARowPenality;
    }

    public void setTwoInARowPenality(int twoInARowPenality) {
        this.twoInARowPenality = twoInARowPenality;
    }

    public int getTwoInADayPenality() {
        return twoInADayPenality;
    }

    public void setTwoInADayPenality(int twoInADayPenality) {
        this.twoInADayPenality = twoInADayPenality;
    }

    public int getPeriodSpreadLength() {
        return periodSpreadLength;
    }

    public void setPeriodSpreadLength(int periodSpreadLength) {
        this.periodSpreadLength = periodSpreadLength;
    }

    public int getPeriodSpreadPenality() {
        return periodSpreadPenality;
    }

    public void setPeriodSpreadPenality(int periodSpreadPenality) {
        this.periodSpreadPenality = periodSpreadPenality;
    }

    public int getMixedDurationPenality() {
        return mixedDurationPenality;
    }

    public void setMixedDurationPenality(int mixedDurationPenality) {
        this.mixedDurationPenality = mixedDurationPenality;
    }

    public int getFrontLoadLargeTopicSize() {
        return frontLoadLargeTopicSize;
    }

    public void setFrontLoadLargeTopicSize(int frontLoadLargeTopicSize) {
        this.frontLoadLargeTopicSize = frontLoadLargeTopicSize;
    }

    public int getFrontLoadLastPeriodSize() {
        return frontLoadLastPeriodSize;
    }

    public void setFrontLoadLastPeriodSize(int frontLoadLastPeriodSize) {
        this.frontLoadLastPeriodSize = frontLoadLastPeriodSize;
    }

    public int getFrontLoadPenality() {
        return frontLoadPenality;
    }

    public void setFrontLoadPenality(int frontLoadPenality) {
        this.frontLoadPenality = frontLoadPenality;
    }

    public int compareTo(InstitutionalWeighting other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

}
