/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.director.easy;

import org.optaplanner.core.impl.score.director.AbstractScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;

/**
 * Easy implementation of {@link ScoreDirectorFactory}.
 * @see EasyScoreDirector
 * @see ScoreDirectorFactory
 */
public class EasyScoreDirectorFactory extends AbstractScoreDirectorFactory {

    private final EasyScoreCalculator easyScoreCalculator;

    public EasyScoreDirectorFactory(EasyScoreCalculator easyScoreCalculator) {
        this.easyScoreCalculator = easyScoreCalculator;
    }

    public EasyScoreCalculator getEasyScoreCalculator() {
        return easyScoreCalculator;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public EasyScoreDirector buildScoreDirector(boolean constraintMatchEnabledPreference) {
        return new EasyScoreDirector(this, constraintMatchEnabledPreference, easyScoreCalculator);
    }

}
