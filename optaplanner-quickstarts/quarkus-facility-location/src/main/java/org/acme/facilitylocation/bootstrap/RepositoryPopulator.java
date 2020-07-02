package org.acme.facilitylocation.bootstrap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RepositoryPopulator {

    private final FacilityLocationProblemRepository repository;

    public RepositoryPopulator(FacilityLocationProblemRepository repository) {
        this.repository = repository;
    }

    public void generateDemoData(@Observes StartupEvent startupEvent) {
        FacilityLocationProblem problem = DemoDataBuilder.builder()
                .setCapacity(4500)
                .setDemand(900)
                .setFacilityCount(30)
                .setConsumerCount(60)
                .setSouthWestCorner(new Location(51.44, -0.16))
                .setNorthEastCorner(new Location(51.56, -0.01))
                .setAverageSetupCost(100)
                .setSetupCostStandardDeviation(10)
                .build();
        repository.update(problem);
    }
}
