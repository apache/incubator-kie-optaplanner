package org.drools.solver.examples.nqueens.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.nqueens.domain.NQueens;
import org.drools.solver.examples.nqueens.domain.Queen;

/**
 * @author Geoffrey De Smet
 */
public class NQueensGenerator {

    private static final File outputDir = new File("data/nqueens/unsolved/");

    public static void main(String[] args) {
        String nString = JOptionPane.showInputDialog("For what n?");
        int n = Integer.parseInt(nString.trim());
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        String outputFileName =  "unsolvedNQueens" + n +  ".xml";
        File outputFile = new File(outputDir, outputFileName);
        NQueens nQueens = createNQueens(n);
        solutionDao.writeSolution(nQueens, outputFile);
    }

    private static NQueens createNQueens(int n) {
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
