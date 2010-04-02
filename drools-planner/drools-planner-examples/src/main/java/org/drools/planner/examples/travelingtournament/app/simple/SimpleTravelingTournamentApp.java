package org.drools.planner.examples.travelingtournament.app.simple;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.travelingtournament.app.AbstractTravelingTournamentApp;
import org.drools.planner.examples.travelingtournament.persistence.simple.SimpleTravelingTournamentDaoImpl;
import org.drools.planner.examples.travelingtournament.persistence.smart.SmartTravelingTournamentDaoImpl;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentApp extends AbstractTravelingTournamentApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/travelingtournament/solver/simple/simpleTravelingTournamentSolverConfig.xml";

    public static void main(String[] args) {
        new SimpleTravelingTournamentApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new SimpleTravelingTournamentDaoImpl();
    }

}
