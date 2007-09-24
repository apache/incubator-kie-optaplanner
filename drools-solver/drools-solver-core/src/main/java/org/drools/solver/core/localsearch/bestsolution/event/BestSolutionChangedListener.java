package org.drools.solver.core.localsearch.bestsolution.event;

/**
 * @author Geoffrey De Smet
 */
public interface BestSolutionChangedListener extends java.util.EventListener {

    void bestSolutionChanged(BestSolutionChangedEvent event);

}
