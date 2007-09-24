package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.List;

/**
 * @author Geoffrey De Smet
 */
public interface TabuPropertyEnabled {

    List<? extends Object> getTabuPropertyList();

}
