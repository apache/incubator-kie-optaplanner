package org.drools.planner.examples.nurserostering.domain;

import java.util.EnumSet;

/**
 * @author Geoffrey De Smet
 */
public enum WeekendDefinition {
    SATURDAY_SUNDAY("SaturdaySunday",
            EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
    FRIDAY_SATURDAY_SUNDAY("FridaySaturdaySunday",
            EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
    FRIDAY_SATURDAY_SUNDAY_MONDAY("FridaySaturdaySundayMonday",
            EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY)),
    SATURDAY_SUNDAY_MONDAY("SaturdaySundayMonday",
            EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY));

    private EnumSet<DayOfWeek> dayOfWeekSet;

    public static WeekendDefinition valueOfCode(String code) {
        for (WeekendDefinition weekendDefinition : values()) {
            if (code.equalsIgnoreCase(weekendDefinition.getCode())) {
                return weekendDefinition;
            }
        }
        return null;
    }

    private String code;

    private WeekendDefinition(String code, EnumSet<DayOfWeek> dayOfWeekSet) {
        this.code = code;
        this.dayOfWeekSet = dayOfWeekSet;
    }

    public String getCode() {
        return code;
    }

    private boolean isWeekend(DayOfWeek dayOfWeek) {
        return dayOfWeekSet.contains(dayOfWeek);
    }

    public String toString() {
        return code;
    }

}
