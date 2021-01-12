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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import org.drools.model.*;
import org.drools.model.consequences.ConsequenceBuilder;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.FactTuple;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNodeType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.drools.model.PatternDSL.rule;

abstract class AbstractRuleAssembler<LeftHandSide_ extends AbstractLeftHandSide> implements RuleAssembler {

    protected final LeftHandSide_ leftHandSide;

    protected AbstractRuleAssembler(LeftHandSide_ leftHandSide) {
        this.leftHandSide = leftHandSide;
    }

    protected static void impactScore(Drools drools, AbstractScoreHolder scoreHolder) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            int impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            long impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            BigDecimal impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    @Override
    public final AbstractRuleAssembler andThen(ConstraintGraphNode node) {
        switch (node.getType()) {
            case FILTER:
                return andThenFilter(node);
            case IF_EXISTS:
            case IF_NOT_EXISTS:
                AbstractConstraintModelJoiningNode joiningNode = (AbstractConstraintModelJoiningNode) node;
                boolean shouldExist = joiningNode.getType() == ConstraintGraphNodeType.IF_EXISTS;
                return andThenExists(joiningNode, shouldExist);
            case GROUPBY_MAPPING_ONLY:
            case GROUPBY_COLLECTING_ONLY:
            case GROUPBY_MAPPING_AND_COLLECTING:
                AbstractConstraintModelGroupingNode groupingNode = (AbstractConstraintModelGroupingNode) node;
                return andThenGroupBy(groupingNode);
            default:
                throw new UnsupportedOperationException(node.getType().toString());
        }
    }

    @Override
    public final RuleAssembler join(RuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        if (!(ruleAssembler instanceof UniRuleAssembler)) {
            throw new IllegalStateException("Impossible state: Rule assembler (" + ruleAssembler + ") not instance of "
                    + UniRuleAssembler.class + ".");
        }
        return join((UniRuleAssembler) ruleAssembler, joinNode);
    }

    protected abstract AbstractRuleAssembler join(UniRuleAssembler ruleAssembler, ConstraintGraphNode joinNode);

    protected abstract AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode);

    protected abstract AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode,
            boolean shouldExist);

    protected final AbstractRuleAssembler andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode) {
        List<Function> mappings = groupingNode.getMappings();
        int mappingCount = mappings.size();
        List<UniConstraintCollector> collectors = groupingNode.getCollectors();
        int collectorCount = collectors.size();
        switch (groupingNode.getType()) {
            case GROUPBY_MAPPING_ONLY:
                switch (mappingCount) {
                    case 1:
                        return andThenGroupBy1Map0Collect(mappings.get(0));
                    case 2:
                        return andThenGroupBy2Map0Collect(mappings.get(0), mappings.get(1));
                    default:
                        throw new UnsupportedOperationException("Impossible state: Mapping count (" + mappingCount + ").");
                }
            case GROUPBY_COLLECTING_ONLY:
                if (collectorCount == 1) {
                    return andThenGroupBy0Map1Collect(collectors.get(0));
                }
                throw new UnsupportedOperationException("Impossible state: Collector count (" + collectorCount + ").");
            case GROUPBY_MAPPING_AND_COLLECTING:
                if (mappingCount == 1 && collectorCount == 1) {
                    return andThenGroupBy1Map1Collect(mappings.get(0), collectors.get(0));
                } else if (mappingCount == 2 && collectorCount == 1) {
                    return andThenGroupBy2Map1Collect(mappings.get(0), mappings.get(1), collectors.get(0));
                } else if (mappingCount == 2 && collectorCount == 2) {
                    return andThenGroupBy2Map2Collect(mappings.get(0), mappings.get(1), collectors.get(0),
                            collectors.get(1));
                } else {
                    throw new UnsupportedOperationException("Impossible state: Mapping count (" + mappingCount + "), " +
                            "collector count (" + collectorCount + ").");
                }
            default:
                throw new UnsupportedOperationException(groupingNode.getType().toString());
        }
    }

    protected abstract UniRuleAssembler andThenGroupBy0Map1Collect(Object collector);

    protected abstract UniRuleAssembler andThenGroupBy1Map0Collect(Object mapping);

    protected abstract BiRuleAssembler andThenGroupBy1Map1Collect(Object mapping, Object collector);

    protected abstract BiRuleAssembler andThenGroupBy2Map0Collect(Object mappingA, Object mappingB);

    protected abstract TriRuleAssembler andThenGroupBy2Map1Collect(Object mappingA, Object mappingB,
                                                                 Object collectorC);

    protected abstract QuadRuleAssembler andThenGroupBy2Map2Collect(Object mappingA, Object mappingB,
                                                                 Object collectorC, Object collectorD);

    protected abstract ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables);

    public RuleAssembly assemble(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, DroolsConstraint constraint) {
        List<RuleItemBuilder<?>> ruleItemBuilderList = new ArrayList<>(leftHandSide.get());
        ConsequenceBuilder.ValidBuilder consequence = buildConsequence(constraint, scoreHolderGlobal,
                leftHandSide.getVariables());
        ruleItemBuilderList.add(consequence);
        Rule rule = rule(constraint.getConstraintPackage(), constraint.getConstraintName())
                .build(ruleItemBuilderList.toArray(new RuleItemBuilder[0]));
        return new RuleAssembly(rule, getExpectedJustificationTypes().toArray(Class[]::new));
    }

    private Stream<Class> getExpectedJustificationTypes() {
        Variable<?>[] variables = leftHandSide.getVariables();
        Variable<?> lastVariable = variables[variables.length - 1];
        Class<?> type = lastVariable.getType();
        if (FactTuple.class.isAssignableFrom(type)) {
            // There is one expected constraint justification, and that is of the tuple type.
            return Stream.of(type);
        }
        // There are plenty expected constraint justifications, one for each variable.
        return Arrays.stream(variables)
                .map(Argument::getType);
    }

}
