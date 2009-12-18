package org.drools.planner.examples.travelingtournament.persistence.smart;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentDaoImpl extends XstreamSolutionDaoImpl {

    public SmartTravelingTournamentDaoImpl() {
        super("travelingtournament/smart", TravelingTournament.class);
    }

}