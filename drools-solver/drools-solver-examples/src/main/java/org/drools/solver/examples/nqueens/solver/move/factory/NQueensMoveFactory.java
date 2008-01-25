package org.drools.solver.examples.nqueens.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.nqueens.domain.NQueens;
import org.drools.solver.examples.nqueens.domain.Queen;
import org.drools.solver.examples.nqueens.solver.move.YChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class NQueensMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        NQueens nQueens = (NQueens) solution;
        List<Move> moveList = new ArrayList<Move>();
        for (Queen queen : nQueens.getQueenList()) {
            for (int n : nQueens.createNList()) {
                moveList.add(new YChangeMove(queen, n));
            }
        }
        return moveList;
    }

}
