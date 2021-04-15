/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.director.drools;

import org.drools.core.common.AgendaItem;
import org.kie.api.runtime.rule.Match;
import org.kie.internal.event.rule.RuleEventListener;

public final class OptaPlannerRuleEventListener implements RuleEventListener {

    @Override
    public void onUpdateMatch(Match match) {
        undoPreviousMatch(match);
    }

    @Override
    public void onDeleteMatch(Match match) {
        undoPreviousMatch(match);
    }

    public void undoPreviousMatch(Match match) {
        AgendaItem<?> agendaItem = (AgendaItem) match;
        Runnable callback = agendaItem.getCallback();
        /*
         * In DRL, it is possible that RHS would not call addConstraintMatch() and do some insertLogical() instead,
         * and therefore the callback would be null.
         * In CS-D, that is not possible, as all the rules we generate will create a match.
         * But null callbacks can still happen.
         *
         * If we insert a fact and then immediately delete it without firing any rules inbetween,
         * a dummy match will be created by Drools and that match will not have our callback in it.
         * Although this is inefficient, it was decided that the cure would have been worse than the disease.
         *
         * In both of these situations, it is safe to ignore the null callback.
         */
        if (callback != null) {
            callback.run();
            agendaItem.setCallback(null);
        }
    }

}
