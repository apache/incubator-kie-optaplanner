package org.optaplanner.benchmark.quarkus;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.benchmark.config.PlannerBenchmarkConfig;

import io.quarkus.arc.DefaultBean;

public class OptaPlannerBenchmarkBeanProvider {

    @DefaultBean
    @Singleton
    @Produces
    PlannerBenchmarkFactory benchmarkFactory(PlannerBenchmarkConfig plannerBenchmarkConfig) {
        return PlannerBenchmarkFactory.create(plannerBenchmarkConfig);
    }

}
