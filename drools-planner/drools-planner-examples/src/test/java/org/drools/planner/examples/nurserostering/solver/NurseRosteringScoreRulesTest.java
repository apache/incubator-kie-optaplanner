package org.drools.planner.examples.nurserostering.solver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.calculator.DefaultHardAndSoftConstraintScoreCalculator;
import org.drools.planner.examples.nurserostering.domain.Assignment;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.persistence.NurseRosteringDaoImpl;
import org.drools.planner.examples.nurserostering.solver.move.NurseRosterMoveHelper;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringScoreRulesTest extends TestCase {

    public void testMoveAndUndoMove() {
        LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope();
        localSearchSolverScope.setRuleBase(buildRuleBase());
        localSearchSolverScope.setWorkingScoreCalculator(new DefaultHardAndSoftConstraintScoreCalculator());
        NurseRoster nurseRoster = (NurseRoster) new NurseRosteringDaoImpl().readSolution(getClass().getResourceAsStream(
                "/org/drools/planner/examples/nurserostering/data/testNurseRosteringScoreRules.xml"));
        localSearchSolverScope.setWorkingSolution(nurseRoster);
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();

        Score firstScore = localSearchSolverScope.calculateScoreFromWorkingMemory();
        // do AssignmentSwitchMove
        Employee leftEmployee = findEmployeeById(nurseRoster, 0L);
        Assignment leftAssignment = findAssignmentById(nurseRoster, 200204001L);
        assertEquals(leftEmployee, leftAssignment.getEmployee());
        Employee rightEmployee = findEmployeeById(nurseRoster, 12L);
        Assignment rightAssignment = findAssignmentById(nurseRoster, 200204002L);
        assertEquals(rightEmployee, rightAssignment.getEmployee());
        NurseRosterMoveHelper.moveEmployee(workingMemory, leftAssignment, rightEmployee);
        NurseRosterMoveHelper.moveEmployee(workingMemory, rightAssignment, leftEmployee);
        localSearchSolverScope.calculateScoreFromWorkingMemory();
        // undo AssignmentSwitchMove;
        NurseRosterMoveHelper.moveEmployee(workingMemory, rightAssignment, rightEmployee);
        NurseRosterMoveHelper.moveEmployee(workingMemory, leftAssignment, leftEmployee);
        Score secondScore = localSearchSolverScope.calculateScoreFromWorkingMemory();
        assertEquals(firstScore, secondScore);
    }

    private RuleBase buildRuleBase() {
        PackageBuilder packageBuilder = new PackageBuilder();
        InputStream scoreDrlIn = getClass().getResourceAsStream("/org/drools/planner/examples/nurserostering/solver/nurseRosteringScoreRules.drl");
        try {
            packageBuilder.addPackageFromDrl(new InputStreamReader(scoreDrlIn, "utf-8"));
        } catch (DroolsParserException e) {
            throw new IllegalArgumentException("scoreDrl could not be loaded.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("scoreDrl could not be loaded.", e);
        } finally {
            IOUtils.closeQuietly(scoreDrlIn);
        }
        RuleBaseConfiguration ruleBaseConfiguration = new RuleBaseConfiguration();
        RuleBase ruleBase = RuleBaseFactory.newRuleBase(ruleBaseConfiguration);
        if (packageBuilder.hasErrors()) {
            throw new IllegalStateException("There are errors in the scoreDrl:"
                    + packageBuilder.getErrors().toString());
        }
        ruleBase.addPackage(packageBuilder.getPackage());
        return ruleBase;
    }

    private Employee findEmployeeById(NurseRoster nurseRoster, long id) {
        for (Employee employee : nurseRoster.getEmployeeList()) {
            if (employee.getId() == id) {
                return employee;
            }
        }
        throw new IllegalArgumentException("Invalid id (" + id + ")");
    }

    private Assignment findAssignmentById(NurseRoster nurseRoster, long id) {
        for (Assignment assignment : nurseRoster.getAssignmentList()) {
            if (assignment.getId() == id) {
                return assignment;
            }
        }
        throw new IllegalArgumentException("Invalid id (" + id + ")");
    }

}
