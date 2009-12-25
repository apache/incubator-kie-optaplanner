package org.drools.planner.core.localsearch.decider.acceptor.tabu;

import java.util.Collection;

/**
 * @author Geoffrey De Smet
 */
public interface TabuPropertyEnabled {

    Collection<? extends Object> getTabuProperties();

}
