package org.acme.facilitylocation.bootstrap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RepositoryPopulator {

    @Inject
    FacilityLocationProblemRepository repository;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        FacilityLocationProblem problem = DemoDataBuilder.builder()
                .setCapacity(1500)
                .setDemand(900)
                .setFacilityCount(10)
                .setConsumerCount(60)
                .setSouthWestCorner(new Location(51.44, -0.16))
                .setNorthEastCorner(new Location(51.56, -0.01))
                .setAverageSetupCost(100)
                .setSetupCostStandardDeviation(10)
                .build();
        repository.update(problem);
    }
}
