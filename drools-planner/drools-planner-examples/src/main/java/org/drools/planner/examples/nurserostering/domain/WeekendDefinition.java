package org.drools.planner.examples.nurserostering.domain;

/**
 * @author Geoffrey De Smet
 */
public enum WeekendDefinition {
    SATURDAY_SUNDAY("SaturdaySunday"),
    FRIDAY_SATURDAY_SUNDAY("FridaySaturdaySunday"),
    FRIDAY_SATURDAY_SUNDAY_MONDAY("FridaySaturdaySundayMonday"),
    SATURDAY_SUNDAY_MONDAY("SaturdaySundayMonday");


    public static WeekendDefinition valueOfCode(String code) {
        for (WeekendDefinition weekendDefinition : values()) {
            if (code.equalsIgnoreCase(weekendDefinition.getCode())) {
                return weekendDefinition;
            }
        }
        return null;
    }

    private String code;

    private WeekendDefinition(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String toString() {
        return code;
    }
}
