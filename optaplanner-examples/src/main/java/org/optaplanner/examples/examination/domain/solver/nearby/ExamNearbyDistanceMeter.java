package org.optaplanner.examples.examination.domain.solver.nearby;

import static java.lang.Math.abs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.examination.domain.Exam;

public class ExamNearbyDistanceMeter implements NearbyDistanceMeter<Exam, Exam> {
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");

    @Override
    public double getNearbyDistance(Exam origin, Exam destination) {
        LocalDateTime originTime = LocalDateTime.parse(origin.getPeriod().getStartDateTimeString(), formatter);
        LocalDateTime destinationTime = LocalDateTime.parse(destination.getPeriod().getStartDateTimeString(), formatter);
        return abs(originTime.until(destinationTime, ChronoUnit.MINUTES));
    }

}
