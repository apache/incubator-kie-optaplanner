/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal;

import java.math.BigDecimal;

import org.junit.Test;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolderTest;

import static org.junit.Assert.*;

public class HardMediumSoftBigDecimalScoreHolderTest extends AbstractScoreHolderTest {

    @Test
    public void addConstraintMatchWithConstraintMatch() {
        addConstraintMatch(true);
    }

    @Test
    public void addConstraintMatchWithoutConstraintMatch() {
        addConstraintMatch(false);
    }

    public void addConstraintMatch(boolean constraintMatchEnabled) {
        HardMediumSoftBigDecimalScoreHolder scoreHolder = new HardMediumSoftBigDecimalScoreHolder(constraintMatchEnabled);

        scoreHolder.addHardConstraintMatch(mockRuleContext("scoreRule1"),
                new BigDecimal("-10.00"));

        RuleContext ruleContext2 = mockRuleContext("scoreRule2");
        scoreHolder.addHardConstraintMatch(ruleContext2, new BigDecimal("-2.00"));
        callUnMatch(ruleContext2);

        RuleContext ruleContext3 = mockRuleContext("scoreRule3");
        scoreHolder.addSoftConstraintMatch(ruleContext3, new BigDecimal("-0.30"));
        scoreHolder.addSoftConstraintMatch(ruleContext3, new BigDecimal("-0.03")); // Overwrite existing
        scoreHolder.addHardConstraintMatch(ruleContext3, new BigDecimal("-3.00")); // Different score level
        scoreHolder.addHardConstraintMatch(ruleContext3, new BigDecimal("-4.00")); // Overwrite existing
        scoreHolder.addMediumConstraintMatch(ruleContext3, new BigDecimal("-80"));
        scoreHolder.addMediumConstraintMatch(ruleContext3, new BigDecimal("7.20")); // Overwrite existing

        RuleContext ruleContext4 = mockRuleContext("scoreRule4");
        scoreHolder.addHardConstraintMatch(ruleContext4, new BigDecimal("-1.00"));
        scoreHolder.addMediumConstraintMatch(ruleContext4, new BigDecimal("1.00"));
        scoreHolder.addSoftConstraintMatch(ruleContext4, new BigDecimal("-1.00"));
        callUnMatch(ruleContext4);

        assertEquals(HardMediumSoftBigDecimalScore.valueOf(0, new BigDecimal("-14.00"), new BigDecimal("7.20"), new BigDecimal("-0.03")),
                scoreHolder.extractScore(0));
        assertEquals(HardMediumSoftBigDecimalScore.valueOf(-7, new BigDecimal("-14.00"), new BigDecimal("7.20"), new BigDecimal("-0.03")),
                scoreHolder.extractScore(-7));
        if (constraintMatchEnabled) {
            assertEquals(8, scoreHolder.getConstraintMatchTotals().size());
        }
    }

}
