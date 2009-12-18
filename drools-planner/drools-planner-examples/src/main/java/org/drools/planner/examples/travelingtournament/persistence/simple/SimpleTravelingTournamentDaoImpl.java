package org.drools.planner.examples.travelingtournament.persistence.simple;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentDaoImpl extends XstreamSolutionDaoImpl {

    public SimpleTravelingTournamentDaoImpl() {
        super("travelingtournament/simple", TravelingTournament.class);
    }

}