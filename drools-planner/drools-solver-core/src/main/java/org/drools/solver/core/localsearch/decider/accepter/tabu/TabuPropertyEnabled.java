package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collection;

/**
 * @author Geoffrey De Smet
 */
public interface TabuPropertyEnabled {

    Collection<? extends Object> getTabuProperties();

}
