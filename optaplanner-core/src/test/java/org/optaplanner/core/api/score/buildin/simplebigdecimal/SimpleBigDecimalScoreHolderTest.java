/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.buildin.simplebigdecimal;

import java.math.BigDecimal;

import org.junit.Test;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolderTest;

import static org.junit.Assert.*;

public class SimpleBigDecimalScoreHolderTest extends AbstractScoreHolderTest {

    @Test
    public void addConstraintMatchWithConstraintMatch() {
        addConstraintMatch(true);
    }

    @Test
    public void addConstraintMatchWithoutConstraintMatch() {
        addConstraintMatch(false);
    }

    public void addConstraintMatch(boolean constraintMatchEnabled) {
        SimpleBigDecimalScoreHolder scoreHolder = new SimpleBigDecimalScoreHolder(constraintMatchEnabled);

        RuleContext scoreRule1 = mockRuleContext("scoreRule1");
        scoreHolder.addConstraintMatch(scoreRule1, new BigDecimal("-10.00"));

        RuleContext scoreRule2 = mockRuleContext("scoreRule2");
        scoreHolder.addConstraintMatch(scoreRule2, new BigDecimal("-2.00"));
        callUnMatch(scoreRule2);

        RuleContext scoreRule3 = mockRuleContext("scoreRule3");
        scoreHolder.addConstraintMatch(scoreRule3, new BigDecimal("-0.30"));
        scoreHolder.addConstraintMatch(scoreRule3, new BigDecimal("-0.03")); // Overwrite existing

        assertEquals(SimpleBigDecimalScore.valueOfUninitialized(0, new BigDecimal("-10.03")), scoreHolder.extractScore(0));
        assertEquals(SimpleBigDecimalScore.valueOfUninitialized(-7, new BigDecimal("-10.03")), scoreHolder.extractScore(-7));
        if (constraintMatchEnabled) {
            assertEquals(SimpleBigDecimalScore.valueOf(new BigDecimal("-10.00")), findConstraintMatchTotal(scoreHolder, "scoreRule1").getScoreTotal());
        }
    }

}
