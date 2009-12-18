package org.drools.planner.examples.nqueens.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.nqueens.domain.NQueens;

/**
 * @author Geoffrey De Smet
 */
public class NQueensDaoImpl extends XstreamSolutionDaoImpl {

    public NQueensDaoImpl() {
        super("nqueens", NQueens.class);
    }

}
