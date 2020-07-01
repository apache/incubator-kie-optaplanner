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

package org.optaplanner.persistence.jaxb.api.score.buildin.hardsoftbigdecimal;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.persistence.jaxb.api.score.AbstractScoreJaxbAdapter;

@SuppressWarnings("checkstyle:javadocstyle")
/**
 * {@inheritDoc}
 */
public class HardSoftBigDecimalScoreJaxbAdapter extends AbstractScoreJaxbAdapter<HardSoftBigDecimalScore> {

    @Override
    public HardSoftBigDecimalScore unmarshal(String scoreString) {
        return HardSoftBigDecimalScore.parseScore(scoreString);
    }

}
