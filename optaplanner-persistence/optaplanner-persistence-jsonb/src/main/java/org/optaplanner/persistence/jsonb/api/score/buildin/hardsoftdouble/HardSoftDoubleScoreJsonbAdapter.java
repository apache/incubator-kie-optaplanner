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

package org.optaplanner.persistence.jsonb.api.score.buildin.hardsoftdouble;

import org.optaplanner.core.api.score.buildin.hardsoftdouble.HardSoftDoubleScore;
import org.optaplanner.persistence.jsonb.api.score.AbstractScoreJsonbAdapter;

@SuppressWarnings("checkstyle:javadocstyle")
/**
 * {@inheritDoc}
 */
public class HardSoftDoubleScoreJsonbAdapter extends AbstractScoreJsonbAdapter<HardSoftDoubleScore> {

    @Override
    public HardSoftDoubleScore adaptFromJson(String scoreString) {
        return HardSoftDoubleScore.parseScore(scoreString);
    }
}
