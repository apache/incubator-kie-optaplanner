package org.drools.planner.examples.common.persistence;

import java.io.File;
import java.io.InputStream;

import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface SolutionDao {

    String getDirName();
    File getDataDir();

    Solution readSolution(File file);
    Solution readSolution(InputStream in);
    void writeSolution(Solution solution, File file);

}
