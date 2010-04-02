package org.drools.planner.examples.travelingtournament.app.smart;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.persistence.AbstractSolutionExporter;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.travelingtournament.app.AbstractTravelingTournamentApp;
import org.drools.planner.examples.travelingtournament.persistence.smart.SmartTravelingTournamentDaoImpl;
import org.drools.planner.examples.travelingtournament.persistence.smart.SmartTravelingTournamentSolutionExporter;
import org.drools.planner.examples.travelingtournament.persistence.smart.SmartTravelingTournamentSolutionImporter;

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
    protected SolutionDao createSolutionDao() {
        return new SmartTravelingTournamentDaoImpl();
    }

    @Override
    protected AbstractSolutionImporter createSolutionImporter() {
        return new SmartTravelingTournamentSolutionImporter();
    }

    @Override
    protected AbstractSolutionExporter createSolutionExporter() {
        return new SmartTravelingTournamentSolutionExporter();
    }

}
