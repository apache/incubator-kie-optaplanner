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
import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.score.DefaultSimpleScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.calculator.DefaultHardAndSoftConstraintScoreCalculator;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.nqueens.persistence.NQueensDaoImpl;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
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
        // do EmployeeAssignmentSwitchMove
        Employee leftEmployee = findEmployeeById(nurseRoster, 0L);
        EmployeeAssignment leftEmployeeAssignment = findEmployeeAssignmentById(nurseRoster, 200204001L);
        assertEquals(leftEmployee, leftEmployeeAssignment.getEmployee());
        Employee rightEmployee = findEmployeeById(nurseRoster, 12L);
        EmployeeAssignment rightEmployeeAssignment = findEmployeeAssignmentById(nurseRoster, 200204002L);
        assertEquals(rightEmployee, rightEmployeeAssignment.getEmployee());
        NurseRosterMoveHelper.moveEmployee(workingMemory, leftEmployeeAssignment, rightEmployee);
        NurseRosterMoveHelper.moveEmployee(workingMemory, rightEmployeeAssignment, leftEmployee);
        localSearchSolverScope.calculateScoreFromWorkingMemory();
        // undo EmployeeAssignmentSwitchMove;
        NurseRosterMoveHelper.moveEmployee(workingMemory, rightEmployeeAssignment, rightEmployee);
        NurseRosterMoveHelper.moveEmployee(workingMemory, leftEmployeeAssignment, leftEmployee);
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

    private EmployeeAssignment findEmployeeAssignmentById(NurseRoster nurseRoster, long id) {
        for (EmployeeAssignment employeeAssignment : nurseRoster.getEmployeeAssignmentList()) {
            if (employeeAssignment.getId() == id) {
                return employeeAssignment;
            }
        }
        throw new IllegalArgumentException("Invalid id (" + id + ")");
    }

}
