package org.drools.planner.examples.travelingtournament.app;

import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.AbstractSolutionExporter;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.travelingtournament.swingui.TravelingTournamentPanel;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractTravelingTournamentApp extends CommonApp {

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new TravelingTournamentPanel();
    }

}
