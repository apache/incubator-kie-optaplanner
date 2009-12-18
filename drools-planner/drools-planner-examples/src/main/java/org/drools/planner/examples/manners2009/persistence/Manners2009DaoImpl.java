package org.drools.planner.examples.manners2009.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.manners2009.domain.Manners2009;
import org.drools.planner.examples.nqueens.domain.NQueens;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009DaoImpl extends XstreamSolutionDaoImpl {

    public Manners2009DaoImpl() {
        super("manners2009", Manners2009.class);
    }

}