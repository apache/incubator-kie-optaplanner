package org.drools.solver.examples.common.persistence;

import java.io.File;

import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface SolutionDao {

    Solution readSolution(File file);
    void writeSolution(Solution solution, File file);
    
}
