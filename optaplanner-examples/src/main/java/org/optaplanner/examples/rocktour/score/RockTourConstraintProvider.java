package org.optaplanner.examples.rocktour.score;

import static org.optaplanner.examples.rocktour.domain.RockTourConstraintConfiguration.DELAY_SHOW_COST_PER_DAY;
import static org.optaplanner.examples.rocktour.domain.RockTourConstraintConfiguration.DRIVING_TIME_TO_BUS_ARRIVAL_PER_SECOND;
import static org.optaplanner.examples.rocktour.domain.RockTourConstraintConfiguration.DRIVING_TIME_TO_SHOW_PER_SECOND;
import static org.optaplanner.examples.rocktour.domain.RockTourConstraintConfiguration.REQUIRED_SHOW;
import static org.optaplanner.examples.rocktour.domain.RockTourConstraintConfiguration.REVENUE_OPPORTUNITY;
import static org.optaplanner.examples.rocktour.domain.RockTourConstraintConfiguration.SHORTEN_DRIVING_TIME_PER_MILLISECOND_SQUARED;
import static org.optaplanner.examples.rocktour.domain.RockTourConstraintConfiguration.UNASSIGNED_SHOW;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.examples.rocktour.domain.RockShow;

public class RockTourConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredShow(constraintFactory),
                unassignedShow(constraintFactory),
                revenueOpportunity(constraintFactory),
                drivingTimeToShowPerSecond(constraintFactory),
                drivingTimeToBusArrivalPerSecond(constraintFactory),
                delayShowCostPerDay(constraintFactory),
                shortenDrivingTimePerMillisecondSquared(constraintFactory)
        };
    }

    private UniConstraintStream<RockShow> getShowWithoutDate(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(RockShow.class)
                .filter(rockShow -> rockShow.getDate() == null);
    }

    private Constraint requiredShow(ConstraintFactory constraintFactory) {
        return getShowWithoutDate(constraintFactory)
                .filter(RockShow::isRequired)
                .penalizeConfigurable(REQUIRED_SHOW);
    }

    private Constraint unassignedShow(ConstraintFactory constraintFactory) {
        return getShowWithoutDate(constraintFactory)
                .filter(rockShow -> rockShow.getBus() != null)
                .penalizeConfigurable(UNASSIGNED_SHOW);
    }

    private Constraint revenueOpportunity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RockShow.class)
                .filter(rockShow -> rockShow.getDate() != null)
                .rewardLong(RockShow::getRevenueOpportunity)
                .as(REVENUE_OPPORTUNITY);
    }

    private Constraint delayShowCostPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RockShow.class)
                .filter(rockShow -> rockShow.getDate() != null)
                .penalizeLong(RockShow::getDaysAfterBusDeparture)
                .as(DELAY_SHOW_COST_PER_DAY);
    }

    private Constraint drivingTimeToShowPerSecond(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RockShow.class)
                .filter(rockShow -> rockShow.getPreviousStandstill() != null)
                .penalizeLong(RockShow::getDrivingTimeFromPreviousStandstill)
                .as(DRIVING_TIME_TO_SHOW_PER_SECOND);
    }

    private Constraint shortenDrivingTimePerMillisecondSquared(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RockShow.class)
                .filter(rockShow -> rockShow.getPreviousStandstill() != null)
                .penalizeLong(rockShow -> rockShow.getDrivingTimeFromPreviousStandstill() *
                        rockShow.getDrivingTimeFromPreviousStandstill())
                .as(SHORTEN_DRIVING_TIME_PER_MILLISECOND_SQUARED);
    }

    private Constraint drivingTimeToBusArrivalPerSecond(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RockShow.class)
                .filter(rockShow -> rockShow.getBus() != null)
                .filter(rockShow -> rockShow.getNextShow() == null)
                .penalizeLong(RockShow::getDrivingTimeToBusArrivalLocation)
                .as(DRIVING_TIME_TO_BUS_ARRIVAL_PER_SECOND);
    }
}
