package org.drools.planner.examples.nqueens.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.drools.planner.examples.common.app.LoggingMain;
import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.nqueens.domain.NQueens;
import org.drools.planner.examples.nqueens.domain.Queen;

/**
 * @author Geoffrey De Smet
 */
public class NQueensGenerator extends LoggingMain {

    private static final File outputDir = new File("data/nqueens/unsolved/");

    public void main(String[] args) {
        new NQueensGenerator().generate();
    }

    public void generate() {
        String nString = JOptionPane.showInputDialog("For what n?");
        int n = Integer.parseInt(nString.trim());
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        String outputFileName =  "unsolvedNQueens" + n +  ".xml";
        File outputFile = new File(outputDir, outputFileName);
        NQueens nQueens = createNQueens(n);
        solutionDao.writeSolution(nQueens, outputFile);
    }

    private NQueens createNQueens(int n) {
        NQueens nQueens = new NQueens();
        nQueens.setId(0L);
        List<Queen> queenList = new ArrayList<Queen>(n);
        for (int i = 0; i < n; i++) {
            Queen queen = new Queen();
            queen.setId((long) i);
            queen.setX(i);
            queen.setY(0);
            queenList.add(queen);
        }
        nQueens.setQueenList(queenList);
        return nQueens;
    }

}
