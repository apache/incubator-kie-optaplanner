package org.optaplanner.quarkus.benchmark.it;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Test various OptaPlanner operations running in native mode
 */
@QuarkusIntegrationTest
@Disabled("optaplanner-quarkus-benchmark cannot compile to native")
public class OptaPlannerBenchmarkTestResourceIT extends OptaPlannerBenchmarkTestResourceTest {

}
