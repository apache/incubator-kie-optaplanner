package org.optaplanner.examples.tsp.persistence;

import org.optaplanner.examples.tsp.domain.TspSolution;
import org.optaplanner.persistence.jackson.impl.domain.solution.JacksonSolutionFileIO;

public class TspSolutionFileIO extends JacksonSolutionFileIO<TspSolution> {

    public TspSolutionFileIO() {
        super(TspSolution.class);
    }
}
