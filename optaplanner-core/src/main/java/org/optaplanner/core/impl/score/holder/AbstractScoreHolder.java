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

package org.optaplanner.core.impl.score.holder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.drools.core.common.AgendaItem;
import org.drools.core.spi.Activation;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.AbstractScore;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.score.holder.ScoreHolder;
import org.optaplanner.core.impl.score.constraint.DefaultConstraintMatchTotal;
import org.optaplanner.core.impl.score.constraint.DefaultIndictment;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;
import org.optaplanner.core.impl.score.director.drools.OptaPlannerRuleEventListener;

/**
 * Abstract superclass for {@link ScoreHolder}.
 * Instances of this class are used both in DRL and in CS-D.
 * CS-D uses the {@code impactScore(..., Object... justifications)} overloads, passing in the justifications that CS-D
 * is already aware of.
 * DRL uses the overloads that do not allow to pass justifications from the outside, therefore inferring them from the
 * Drools working memory in {@link #registerConstraintMatch(RuleContext, Runnable, Supplier, Object...)}.
 *
 * @param <Score_> the {@link Score} type
 */
public abstract class AbstractScoreHolder<Score_ extends Score<Score_>> implements ScoreHolder<Score_> {

    /**
     * Exists to improve performance.
     * Otherwise a call from method() to method(Object...) would always create a new array.
     */
    protected static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    protected final boolean constraintMatchEnabled;
    protected final Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotalMap;
    protected final Map<Object, Indictment<Score_>> indictmentMap;
    protected final Score_ zeroScore;

    protected AbstractScoreHolder(boolean constraintMatchEnabled, Score_ zeroScore) {
        this.constraintMatchEnabled = constraintMatchEnabled;
        // TODO Can we set the initial capacity of this map more accurately? For example: number of rules
        constraintMatchTotalMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
        // TODO Can we set the initial capacity of this map more accurately by using entitySize?
        indictmentMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
        this.zeroScore = zeroScore;
    }

    public boolean isConstraintMatchEnabled() {
        return constraintMatchEnabled;
    }

    public Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        if (!isConstraintMatchEnabled()) {
            throw new IllegalStateException("When constraintMatchEnabled (" + isConstraintMatchEnabled()
                    + ") is disabled in the constructor, this method should not be called.");
        }
        return constraintMatchTotalMap;
    }

    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        if (!isConstraintMatchEnabled()) {
            throw new IllegalStateException("When constraintMatchEnabled (" + isConstraintMatchEnabled()
                    + ") is disabled in the constructor, this method should not be called.");
        }
        return indictmentMap;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void configureConstraintWeight(Rule rule, Score_ constraintWeight) {
        if (constraintWeight.getInitScore() != 0) {
            throw new IllegalStateException("The initScore (" + constraintWeight.getInitScore() + ") must be 0.");
        }
        if (constraintMatchEnabled) {
            String constraintPackage = rule.getPackageName();
            String constraintName = rule.getName();
            String constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
            constraintMatchTotalMap.put(constraintId,
                    new DefaultConstraintMatchTotal<>(constraintPackage, constraintName, constraintWeight, zeroScore));
        }
    }

    /**
     * Requires @{@link OptaPlannerRuleEventListener} to be added as event listener on {@link KieSession}, otherwise the
     * score changes caused by the constraint matches would not be undone. See
     * {@link DroolsScoreDirector#resetKieSession()} for an example.
     *
     * @param kcontext The rule for which to register the match.
     * @param constraintUndoListener The operation to run to undo the match.
     * @param scoreSupplier The score change to be undone when constraint justification enabled.
     * @param justifications the primary arguments(s) that the CS penalizes/rewards, empty when not called by CS-D
     */
    protected void registerConstraintMatch(RuleContext kcontext, Runnable constraintUndoListener,
            Supplier<Score_> scoreSupplier, Object... justifications) {
        AgendaItem<?> agendaItem = (AgendaItem<?>) kcontext.getMatch();
        ConstraintActivationUnMatchListener constraintActivationUnMatchListener = new ConstraintActivationUnMatchListener(
                constraintUndoListener);
        agendaItem.setCallback(constraintActivationUnMatchListener);
        if (constraintMatchEnabled) {
            // If we have justifications coming from CS-D directly, use them exclusively.
            List<Object> completeJustificationList =
                    justifications.length == 0 ? extractJustificationList(kcontext) : Arrays.asList(justifications);
            // Not needed in fast code: Add ConstraintMatch
            constraintActivationUnMatchListener.constraintMatchTotal = findConstraintMatchTotal(kcontext);
            ConstraintMatch<Score_> constraintMatch = constraintActivationUnMatchListener.constraintMatchTotal
                    .addConstraintMatch(completeJustificationList, scoreSupplier.get());
            List<DefaultIndictment<Score_>> indictmentList = completeJustificationList.stream()
                    .distinct() // One match might have the same justification twice
                    .map(justification -> {
                        DefaultIndictment<Score_> indictment =
                                (DefaultIndictment<Score_>) indictmentMap.computeIfAbsent(justification,
                                        k -> new DefaultIndictment<>(justification, zeroScore));
                        indictment.addConstraintMatch(constraintMatch);
                        return indictment;
                    }).collect(Collectors.toList());
            constraintActivationUnMatchListener.constraintMatch = constraintMatch;
            constraintActivationUnMatchListener.indictmentList = indictmentList;
        }
    }

    private DefaultConstraintMatchTotal<Score_> findConstraintMatchTotal(RuleContext kcontext) {
        Rule rule = kcontext.getRule();
        String constraintPackage = rule.getPackageName();
        String constraintName = rule.getName();
        String constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
        return (DefaultConstraintMatchTotal<Score_>) constraintMatchTotalMap.computeIfAbsent(constraintId,
                k -> new DefaultConstraintMatchTotal<>(constraintPackage, constraintName, null, zeroScore));
    }

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     */
    public final void impactScore(RuleContext kcontext) {
        impactScore(kcontext, EMPTY_OBJECT_ARRAY);
    }

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     * @param justifications the primary arguments(s) that the CS penalizes/rewards, not used outside of CS-D
     */
    public void impactScore(RuleContext kcontext, Object... justifications) {
        throw new UnsupportedOperationException("In the rule (" + kcontext.getRule().getName()
                + "), the scoreHolder class (" + getClass()
                + ") requires a weightMultiplier.");
    }

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     * @param weightMultiplier any
     */
    public final void impactScore(RuleContext kcontext, int weightMultiplier) {
        impactScore(kcontext, weightMultiplier, EMPTY_OBJECT_ARRAY);
    }

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     * @param weightMultiplier any
     * @param justifications the primary arguments(s) that the CS penalizes/rewards, not used outside of CS-D
     */
    public abstract void impactScore(RuleContext kcontext, int weightMultiplier, Object... justifications);

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     * @param weightMultiplier any
     */
    public final void impactScore(RuleContext kcontext, long weightMultiplier) {
        impactScore(kcontext, weightMultiplier, EMPTY_OBJECT_ARRAY);
    }

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     * @param weightMultiplier any
     * @param justifications the primary arguments(s) that the CS penalizes/rewards, not used outside of CS-D
     */
    public abstract void impactScore(RuleContext kcontext, long weightMultiplier, Object... justifications);

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     * @param weightMultiplier any
     */
    public final void impactScore(RuleContext kcontext, BigDecimal weightMultiplier) {
        impactScore(kcontext, weightMultiplier, EMPTY_OBJECT_ARRAY);
    }

    /**
     * For internal use only, use penalize() or reward() instead.
     *
     * @param kcontext never null
     * @param weightMultiplier any
     * @param justifications the primary arguments(s) that the CS penalizes/rewards, not used outside of CS-D
     */
    public abstract void impactScore(RuleContext kcontext, BigDecimal weightMultiplier, Object... justifications);

    public abstract Score_ extractScore(int initScore);

    protected List<Object> extractJustificationList(RuleContext kcontext) {
        // Unlike kcontext.getMatch().getObjects(), this includes the matches of accumulate and exists
        Activation activation = (Activation) kcontext.getMatch();
        return new ArrayList<>(activation.getObjectsDeep());
    }

    public class ConstraintActivationUnMatchListener implements Runnable {

        private final Runnable constraintUndoListener;

        private DefaultConstraintMatchTotal<Score_> constraintMatchTotal;
        private List<DefaultIndictment<Score_>> indictmentList;
        private ConstraintMatch<Score_> constraintMatch;

        public ConstraintActivationUnMatchListener(Runnable constraintUndoListener) {
            this.constraintUndoListener = constraintUndoListener;
        }

        @Override
        public final void run() {
            constraintUndoListener.run();
            if (constraintMatchEnabled) {
                // Not needed in fast code: Remove ConstraintMatch
                constraintMatchTotal.removeConstraintMatch(constraintMatch);
                for (DefaultIndictment<Score_> indictment : indictmentList) {
                    indictment.removeConstraintMatch(constraintMatch);
                    if (indictment.getConstraintMatchSet().isEmpty()) {
                        indictmentMap.remove(indictment.getJustification());
                    }
                }
            }
        }
    }

    @FunctionalInterface
    protected interface IntMatchExecutor {

        void accept(RuleContext kcontext, int matchWeight, Object... justifications);

    }

    @FunctionalInterface
    protected interface LongMatchExecutor {

        void accept(RuleContext kcontext, long matchWeight, Object... justifications);

    }

    @FunctionalInterface
    protected interface BigDecimalMatchExecutor {

        void accept(RuleContext kcontext, BigDecimal matchWeight, Object... justifications);

    }

    /**
     * Unlike {@link IntMatchExecutor} and its counterparts, this is not being used on CS-D code paths.
     * Therefore it does not require justifications, as DRL will always infer them from the Drools working memory.
     * 
     * @param <Score_> the {@link Score} type
     */
    @FunctionalInterface
    protected interface ScoreMatchExecutor<Score_ extends AbstractScore<Score_>> {

        void accept(RuleContext kcontext, Score_ matchWeight);

    }

}
