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

package org.optaplanner.core.impl.score.buildin.simplebigdecimal;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SimpleBigDecimalScoreDefinitionTest {

    @Test
    public void getZeroScore() {
        SimpleBigDecimalScore score = new SimpleBigDecimalScoreDefinition().getZeroScore();
        assertThat(score).isEqualTo(SimpleBigDecimalScore.ZERO);
    }

    @Test
    public void getSoftestOneScore() {
        SimpleBigDecimalScore score = new SimpleBigDecimalScoreDefinition().getOneSoftestScore();
        assertThat(score).isEqualTo(SimpleBigDecimalScore.ONE);
    }

    @Test
    public void getLevelsSize() {
        assertEquals(1, new SimpleBigDecimalScoreDefinition().getLevelsSize());
    }

    @Test
    public void getLevelLabels() {
        assertArrayEquals(new String[]{"score"}, new SimpleBigDecimalScoreDefinition().getLevelLabels());
    }

    // Optimistic and pessimistic bounds are currently not supported for this score definition

}
