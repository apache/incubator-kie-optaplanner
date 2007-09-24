package org.drools.solver.examples.travelingtournament.app.simple;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.travelingtournament.app.AbstractTravelingTournamentApp;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentApp extends AbstractTravelingTournamentApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/travelingtournament/solver/simple/simpleTravelingTournamentSolverConfig.xml";

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
    protected String getExampleDirName() {
        return "travelingtournament/simple";
    }
}
