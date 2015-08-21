/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.score.buildin.bendablelong;

import org.junit.Test;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.api.score.holder.AbstractScoreHolderTest;

import static org.junit.Assert.assertEquals;

public class BendableLongScoreHolderTest extends AbstractScoreHolderTest {

    @Test
    public void addConstraintMatchWithConstraintMatch() {
        addConstraintMatch(true);
    }

    @Test
    public void addConstraintMatchWithoutConstraintMatch() {
        addConstraintMatch(false);
    }

    public void addConstraintMatch(boolean constraintMatchEnabled) {
        BendableLongScoreHolder scoreHolder = new BendableLongScoreHolder(constraintMatchEnabled, 1, 2);

        scoreHolder.addHardConstraintMatch(createRuleContext("scoreRule1"), 0, 1000000000001L); // Rule match added

        RuleContext ruleContext2 = createRuleContext("scoreRule2");
        scoreHolder.addHardConstraintMatch(ruleContext2, 0, 1000000000020L); // Rule match added
        callUnMatch(ruleContext2); // Rule match removed

        RuleContext ruleContext3 = createRuleContext("scoreRule3");
        scoreHolder.addSoftConstraintMatch(ruleContext3, 0, 1000000000300L); // Rule match added
        scoreHolder.addSoftConstraintMatch(ruleContext3, 0, 1000000040000L); // Rule match modified
        scoreHolder.addHardConstraintMatch(ruleContext3, 0, 1000000000300L); // Rule of different level added
        scoreHolder.addHardConstraintMatch(ruleContext3, 0, 1000000000400L); // Rule of different level modified

        scoreHolder.addSoftConstraintMatch(createRuleContext("scoreRule4"), 1, -1000000500000L); // Rule match added

        RuleContext ruleContext5 = createRuleContext("scoreRule5");
        scoreHolder.addHardConstraintMatch(ruleContext5, 0, 1000000000001L);
        scoreHolder.addSoftConstraintMatch(ruleContext5, 0, 1000000000001L);
        callUnMatch(ruleContext5, 1); // Rule match removed - 1st score level (soft)

        assertEquals(BendableLongScore.valueOf(new long[]{3000000000402L},
                new long[]{1000000040000L, -1000000500000L}), scoreHolder.extractScore());
        if (constraintMatchEnabled) {
            assertEquals(7, scoreHolder.getConstraintMatchTotals().size());
        }
    }

}
