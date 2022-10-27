package org.optaplanner.examples.cloudbalancing.persistence;

import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.persistence.jaxb.impl.domain.solution.JaxbSolutionFileIO;

public class CloudBalanceXmlSolutionFileIO extends JaxbSolutionFileIO<CloudBalance> {

    public CloudBalanceXmlSolutionFileIO() {
        super(CloudBalance.class);
    }
}
