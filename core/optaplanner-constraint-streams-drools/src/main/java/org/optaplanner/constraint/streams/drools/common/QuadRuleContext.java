package org.optaplanner.constraint.streams.drools.common;

import static org.optaplanner.constraint.streams.common.inliner.JustificationsSupplier.of;

import java.math.BigDecimal;
import java.util.Objects;

import org.drools.model.DSL;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.constraint.streams.common.inliner.JustificationsSupplier;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;

final class QuadRuleContext<A, B, C, D> extends AbstractRuleContext {

    private final Variable<A> variableA;
    private final Variable<B> variableB;
    private final Variable<C> variableC;
    private final Variable<D> variableD;

    public QuadRuleContext(Variable<A> variableA, Variable<B> variableB, Variable<C> variableC,
            Variable<D> variableD, ViewItem<?>... viewItems) {
        super(viewItems);
        this.variableA = Objects.requireNonNull(variableA);
        this.variableB = Objects.requireNonNull(variableB);
        this.variableC = Objects.requireNonNull(variableC);
        this.variableD = Objects.requireNonNull(variableD);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        ConsequenceBuilder<Solution_> consequenceBuilder =
                (constraint, scoreImpacterGlobal) -> DSL.on(scoreImpacterGlobal, variableA, variableB, variableC, variableD)
                        .execute((drools, scoreImpacter, a, b, c, d) -> {
                            JustificationsSupplier justificationsSupplier =
                                    scoreImpacter.getContext().isConstraintMatchEnabled()
                                            ? of(constraint, constraint.getJustificationMapping(),
                                                    constraint.getIndictedObjectsMapping(), a, b, c, d)
                                            : null;
                            runConsequence(constraint, drools, scoreImpacter, matchWeigher.applyAsInt(a, b, c, d),
                                    justificationsSupplier);
                        });
        return assemble(consequenceBuilder);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        ConsequenceBuilder<Solution_> consequenceBuilder =
                (constraint, scoreImpacterGlobal) -> DSL.on(scoreImpacterGlobal, variableA, variableB, variableC, variableD)
                        .execute((drools, scoreImpacter, a, b, c, d) -> {
                            JustificationsSupplier justificationsSupplier =
                                    scoreImpacter.getContext().isConstraintMatchEnabled()
                                            ? of(constraint, constraint.getJustificationMapping(),
                                                    constraint.getIndictedObjectsMapping(), a, b, c, d)
                                            : null;
                            runConsequence(constraint, drools, scoreImpacter, matchWeigher.applyAsLong(a, b, c, d),
                                    justificationsSupplier);
                        });
        return assemble(consequenceBuilder);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        ConsequenceBuilder<Solution_> consequenceBuilder =
                (constraint, scoreImpacterGlobal) -> DSL.on(scoreImpacterGlobal, variableA, variableB, variableC, variableD)
                        .execute((drools, scoreImpacter, a, b, c, d) -> {
                            JustificationsSupplier justificationsSupplier =
                                    scoreImpacter.getContext().isConstraintMatchEnabled()
                                            ? of(constraint, constraint.getJustificationMapping(),
                                                    constraint.getIndictedObjectsMapping(), a, b, c, d)
                                            : null;
                            runConsequence(constraint, drools, scoreImpacter, matchWeigher.apply(a, b, c, d),
                                    justificationsSupplier);
                        });
        return assemble(consequenceBuilder);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder() {
        return newRuleBuilder((ToIntQuadFunction<A, B, C, D>) (a, b, c, d) -> 1);
    }

}
