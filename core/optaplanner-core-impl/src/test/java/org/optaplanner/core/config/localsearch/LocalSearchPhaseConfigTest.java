package org.optaplanner.core.config.localsearch;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.solver.SolverConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalSearchPhaseConfigTest {

  @Test
  void copyConfigWithSolverConfigCopyCtor() {

    final UnionMoveSelectorConfig unionMoveSelectorConfig = new UnionMoveSelectorConfig();
    final LocalSearchPhaseConfig localSearchPhase = new LocalSearchPhaseConfig();
    localSearchPhase.setMoveSelectorConfig(unionMoveSelectorConfig);

    final SolverConfig originalConfig = new SolverConfig();
    originalConfig.setPhaseConfigList(List.of(localSearchPhase));

    final SolverConfig copiedConfig = new SolverConfig(originalConfig);

    assertNotSame(
      originalConfig,
      copiedConfig
    );
    assertNotSame(
      originalConfig.getPhaseConfigList(),
      copiedConfig.getPhaseConfigList()
    );
    assertNotSame(
      originalConfig.getPhaseConfigList().get(0),
      copiedConfig.getPhaseConfigList().get(0)
    );
    assertNotSame(
      ((LocalSearchPhaseConfig)originalConfig.getPhaseConfigList().get(0)).getMoveSelectorConfig(),
      ((LocalSearchPhaseConfig)copiedConfig.getPhaseConfigList().get(0)).getMoveSelectorConfig()
    );
  }

  @Test
  void copyConfigMinimalReproduction() {
    final UnionMoveSelectorConfig unionMoveSelectorConfig = new UnionMoveSelectorConfig();
    final LocalSearchPhaseConfig localSearchPhase = new LocalSearchPhaseConfig();
    localSearchPhase.setMoveSelectorConfig(unionMoveSelectorConfig);

    final LocalSearchPhaseConfig copiedLocalSearchPhase = localSearchPhase.copyConfig();

    assertNotSame(localSearchPhase, copiedLocalSearchPhase);
    assertNotSame(localSearchPhase.getMoveSelectorConfig(), copiedLocalSearchPhase.getMoveSelectorConfig());
  }
}