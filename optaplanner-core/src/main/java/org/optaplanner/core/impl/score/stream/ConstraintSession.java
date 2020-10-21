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

package org.optaplanner.core.impl.score.stream;

import java.util.Map;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * An instance of this class must be used in only one thread.
 */
public interface ConstraintSession<Solution_, Score_ extends Score<Score_>> extends AutoCloseable {

    void insert(Object fact);

    void update(Object fact);

    void retract(Object fact);

    Score_ calculateScore(int initScore);

    /**
     * As defined by {@link InnerScoreDirector#getConstraintMatchTotalMap()}.
     *
     * @return never null
     */
    Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap();

    /**
     * As defined by {@link InnerScoreDirector#getIndictmentMap()}.
     *
     * @return never null
     */
    Map<Object, Indictment<Score_>> getIndictmentMap();

    @Override
    void close();

}
