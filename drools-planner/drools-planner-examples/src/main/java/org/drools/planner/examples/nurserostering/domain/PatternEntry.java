package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("PatternEntry")
public class PatternEntry extends AbstractPersistable implements Comparable<PatternEntry> {

    private Pattern pattern;
    private int entryIndex;

    private PatternEntryPropertyWildcard dayOfWeekWildcard;
    private DayOfWeek dayOfWeek;
    private PatternEntryPropertyWildcard shiftTypeWildcard;
    private ShiftType shiftType;

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public int getEntryIndex() {
        return entryIndex;
    }

    public void setEntryIndex(int entryIndex) {
        this.entryIndex = entryIndex;
    }

    public PatternEntryPropertyWildcard getDayOfWeekWildcard() {
        return dayOfWeekWildcard;
    }

    public void setDayOfWeekWildcard(PatternEntryPropertyWildcard dayOfWeekWildcard) {
        this.dayOfWeekWildcard = dayOfWeekWildcard;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public PatternEntryPropertyWildcard getShiftTypeWildcard() {
        return shiftTypeWildcard;
    }

    public void setShiftTypeWildcard(PatternEntryPropertyWildcard shiftTypeWildcard) {
        this.shiftTypeWildcard = shiftTypeWildcard;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public int compareTo(PatternEntry other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return pattern + "-" + entryIndex + ": "
                + dayOfWeekWildcard + "(" + dayOfWeek + ") " + shiftTypeWildcard + "(" + shiftType + ")";
    }

}
