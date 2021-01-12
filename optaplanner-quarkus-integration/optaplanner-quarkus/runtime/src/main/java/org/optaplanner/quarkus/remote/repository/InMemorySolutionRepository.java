/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.quarkus.remote.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySolutionRepository<Solution, ProblemId> implements SolutionRepository<Solution, ProblemId> {

    private final Map<ProblemId, Solution> repository = new ConcurrentHashMap<>();

    @Override
    public void save(ProblemId problemId, Solution solution) {
        repository.put(problemId, solution);
    }

    @Override
    public Solution load(ProblemId problemId) {
        return repository.get(problemId);
    }
}
