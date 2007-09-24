package org.drools.solver.examples.travelingtournament.app;

import org.drools.solver.examples.common.app.CommonApp;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.travelingtournament.swingui.TravelingTournamentPanel;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractTravelingTournamentApp extends CommonApp {

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new TravelingTournamentPanel();
    }

}
