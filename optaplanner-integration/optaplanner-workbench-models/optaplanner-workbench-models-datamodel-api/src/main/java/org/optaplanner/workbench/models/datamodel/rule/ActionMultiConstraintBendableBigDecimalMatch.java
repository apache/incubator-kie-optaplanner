/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.workbench.models.datamodel.rule;

import java.util.List;
import java.util.stream.Collectors;

import org.drools.workbench.models.datamodel.rule.TemplateAware;

public class ActionMultiConstraintBendableBigDecimalMatch extends AbstractActionMultiConstraintBendableMatch {

    public ActionMultiConstraintBendableBigDecimalMatch() {
    }

    public ActionMultiConstraintBendableBigDecimalMatch(final List<ActionBendableHardConstraintMatch> actionBendableHardConstraintMatches,
                                                        final List<ActionBendableSoftConstraintMatch> actionBendableSoftConstraintMatches) {
        super(actionBendableHardConstraintMatches,
              actionBendableSoftConstraintMatches);
    }

    @Override
    public TemplateAware cloneTemplateAware() {
        return new ActionMultiConstraintBendableBigDecimalMatch(getActionBendableHardConstraintMatches().stream().map(m -> (ActionBendableHardConstraintMatch) m.cloneTemplateAware()).collect(Collectors.toList()),
                                                                getActionBendableSoftConstraintMatches().stream().map(m -> (ActionBendableSoftConstraintMatch) m.cloneTemplateAware()).collect(Collectors.toList()));
    }

    @Override
    public String getStringRepresentation() {
        return new StringBuilder()
                .append("scoreHolder.addMultiConstraintMatch(kcontext, new java.math.BigDecimal[] {")
                .append(getActionBendableHardConstraintMatches().stream().map(m -> m.getConstraintMatch()).collect(Collectors.joining(", ")))
                .append("}, new java.math.BigDecimal[] {")
                .append(getActionBendableSoftConstraintMatches().stream().map(m -> m.getConstraintMatch()).collect(Collectors.joining(", ")))
                .append("})").toString();
    }
}
