/*
 * Copyright 2015 JBoss Inc
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

package org.optaplanner.core.api.score.buildin.bendablebigdecimal;

import java.math.BigDecimal;

import org.junit.Test;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolderTest;

import static org.junit.Assert.*;

public class BendableBigDecimalScoreHolderTest extends AbstractScoreHolderTest {

    @Test
    public void addConstraintMatchWithConstraintMatch() {
        addConstraintMatch(true);
    }

    @Test
    public void addConstraintMatchWithoutConstraintMatch() {
        addConstraintMatch(false);
    }

    public void addConstraintMatch(boolean constraintMatchEnabled) {
        BendableBigDecimalScoreHolder scoreHolder = new BendableBigDecimalScoreHolder(constraintMatchEnabled, 1, 2);

        // Rule match added
        scoreHolder.addHardConstraintMatch(createRuleContext("scoreRule1"), 0, BigDecimal.valueOf(-1000));

        RuleContext ruleContext2 = createRuleContext("scoreRule2");
        scoreHolder.addHardConstraintMatch(ruleContext2, 0, BigDecimal.valueOf(-200)); // Rule match added
        callUnMatch(ruleContext2); // Rule match removed

        RuleContext ruleContext3 = createRuleContext("scoreRule3");
        scoreHolder.addSoftConstraintMatch(ruleContext3, 0, BigDecimal.valueOf(-30)); // Rule match added
        scoreHolder.addSoftConstraintMatch(ruleContext3, 0, BigDecimal.valueOf(-3)); // Rule match modified

        scoreHolder.addSoftConstraintMatch(createRuleContext("scoreRule4"), 1, BigDecimal.valueOf(-4)); // Rule match added

        assertEquals(BendableBigDecimalScore.valueOf(new BigDecimal[]{BigDecimal.valueOf(-1000)},
                new BigDecimal[]{BigDecimal.valueOf(-3), BigDecimal.valueOf(-4)}), scoreHolder.extractScore());
        if (constraintMatchEnabled) {
            assertEquals(4, scoreHolder.getConstraintMatchTotals().size());
        }
    }

}
