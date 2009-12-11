package org.drools.planner.core.localsearch.stepstatistic;

/**
 * @author Geoffrey De Smet
 */
public interface StepStatistic { // TODO This isn't used anywhere

    /**
     * How much of all the selectable moves should be evaluated for the current step.
     * @return a number > 0 and <= 1.0
     */
    double getSelectorThoroughness(); // TODO this is a new feature to implement somewhere 

}
