package org.drools.planner.examples.nurserostering.domain;

import java.util.Calendar;

/**
 * @author Geoffrey De Smet
 */
public enum DayOfWeek {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    public static DayOfWeek valueOfCalendar(int calendarDayInWeek) {
        switch (calendarDayInWeek) {
            case Calendar.SUNDAY:
                return SUNDAY;
            case Calendar.MONDAY:
                return MONDAY;
            case Calendar.TUESDAY:
                return TUESDAY;
            case Calendar.WEDNESDAY:
                return WEDNESDAY;
            case Calendar.THURSDAY:
                return THURSDAY;
            case Calendar.FRIDAY:
                return FRIDAY;
            case Calendar.SATURDAY:
                return SATURDAY;
            default:
                throw new IllegalArgumentException("The calendarDayInWeek (" + calendarDayInWeek + ") is not valid.");
        }
    }

    public static DayOfWeek valueOfCode(String code) {
        for (DayOfWeek dayOfWeek : values()) {
            if (code.equalsIgnoreCase(dayOfWeek.getCode())) {
                return dayOfWeek;
            }
        }
        return null;
    }

    private String code;

    private DayOfWeek(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String toString() {
        return code.substring(0, 3);
    }
}
