package org.drools.solver.examples.common.persistence;

import java.io.File;
import java.io.InputStream;

import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface SolutionDao {

    Solution readSolution(File file);
    Solution readSolution(InputStream in);
    void writeSolution(Solution solution, File file);
    
}
