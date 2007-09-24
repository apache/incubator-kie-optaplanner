package org.drools.solver.examples.travelingtournament.app.smart;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.travelingtournament.app.AbstractTravelingTournamentApp;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentApp extends AbstractTravelingTournamentApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/travelingtournament/solver/smart/smartTravelingTournamentSolverConfig.xml";


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
