package org.drools.planner.benchmark.statistic;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * @author Geoffrey De Smet
 */
class MillisecondsSpendNumberFormat extends NumberFormat {

    private final static long DAY_MILLIS = 3600000L * 24L;
    private final static long HOUR_MILLIS = 3600000L;
    private final static long MINUTE_MILLIS = 60000L;
    private final static long SECOND_MILLIS = 1000L; 

    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return format((long) number, toAppendTo, pos);
    }

    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number == 0L) {
            toAppendTo.append("0");
        }
        long rest = number;
        long days = rest / DAY_MILLIS;
        if (days > 0) {
            toAppendTo.append(days).append("d");
            rest %= DAY_MILLIS;
        }
        long hours = rest / HOUR_MILLIS;
        if (hours > 0) {
            toAppendTo.append(hours).append("h");
            rest %= HOUR_MILLIS;
        }
        long minutes = rest / MINUTE_MILLIS;
        if (minutes > 0) {
            toAppendTo.append(minutes).append("m");
            rest %= MINUTE_MILLIS;
        }
        long seconds = rest / SECOND_MILLIS;
        if (seconds > 0) {
            toAppendTo.append(seconds).append("s");
            rest %= SECOND_MILLIS;
        }
        if (rest > 0) {
            toAppendTo.append(rest).append("ms");
        }
        return toAppendTo;
    }

    public Number parse(String source, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

}
