package org.drools.planner.examples.travelingtournament.app.smart;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.travelingtournament.app.AbstractTravelingTournamentApp;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentApp extends AbstractTravelingTournamentApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/travelingtournament/solver/smart/smartTravelingTournamentSolverConfig.xml";


    public static void main(String[] args) {
        new SmartTravelingTournamentApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected String getExampleDirName() {
        return "travelingtournament/smart";
    }

}
